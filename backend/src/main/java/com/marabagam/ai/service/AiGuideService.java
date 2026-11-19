package com.marabagam.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.ai.domain.AiConversation;
import com.marabagam.ai.domain.AiMessage;
import com.marabagam.ai.dto.*;
import com.marabagam.ai.repository.AiConversationRepository;
import com.marabagam.ai.repository.AiMessageRepository;
import com.marabagam.common.exception.InvalidInputException;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiGuideService {

    private static final Logger log = LoggerFactory.getLogger(AiGuideService.class);

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.url:http://ai-service:8000}")
    private String aiServiceUrl;

    public AiGuideService(
            AiConversationRepository conversationRepository,
            AiMessageRepository messageRepository,
            UserRepository userRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiGuideResponseDto askCulturalGuide(AskCulturalGuideRequestDto req, UUID currentUserId) {
        if (req.getQuery() == null || req.getQuery().trim().isEmpty()) {
            throw new InvalidInputException("Query text cannot be empty");
        }
        if (req.getQuery().length() > 1000) {
            throw new InvalidInputException("Query text exceeds maximum allowed length of 1000 characters");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        AiConversation conversation;
        if (req.getConversationId() != null) {
            conversation = conversationRepository.findByIdAndUserId(req.getConversationId(), currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found or access denied"));
        } else {
            conversation = new AiConversation();
            conversation.setUser(user);
            String title = req.getQuery().length() > 40 ? req.getQuery().substring(0, 37) + "..." : req.getQuery();
            conversation.setTitle(title);
            conversation = conversationRepository.save(conversation);
        }

        // Save User Message
        AiMessage userMsg = new AiMessage();
        userMsg.setConversation(conversation);
        userMsg.setSender("USER");
        userMsg.setContent(req.getQuery());
        messageRepository.save(userMsg);

        // Forward to FastAPI RAG endpoint
        String answer = "I do not have sufficient verified cultural information in Marabagam's database to answer this question accurately.";
        String language = "en";
        List<SourceAttributionDto> sources = new ArrayList<>();
        Double relevanceScore = null;

        try {
            Map<String, Object> ragRequest = new HashMap<>();
            ragRequest.put("query", req.getQuery());
            ragRequest.put("conversationId", conversation.getId().toString());

            // Pass previous conversation turns for multi-turn conversational awareness
            List<AiMessage> previousMessages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
            List<Map<String, String>> historyList = new ArrayList<>();
            int startIdx = Math.max(0, previousMessages.size() - 7);
            for (int i = startIdx; i < previousMessages.size() - 1; i++) {
                AiMessage m = previousMessages.get(i);
                Map<String, String> hm = new HashMap<>();
                hm.put("sender", m.getSender());
                hm.put("content", m.getContent());
                historyList.add(hm);
            }
            ragRequest.put("history", historyList);

            String url = aiServiceUrl + "/internal/v1/rag/query";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, ragRequest, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map data = response.getBody();
                if (data.containsKey("answer") && data.get("answer") != null) {
                    answer = (String) data.get("answer");
                }
                if (data.containsKey("language") && data.get("language") != null) {
                    language = (String) data.get("language");
                }
                if (data.containsKey("relevanceScore") && data.get("relevanceScore") != null) {
                    relevanceScore = ((Number) data.get("relevanceScore")).doubleValue();
                }
                if (data.containsKey("sources") && data.get("sources") instanceof List) {
                    List<?> rawSources = (List<?>) data.get("sources");
                    for (Object item : rawSources) {
                        SourceAttributionDto src = objectMapper.convertValue(item, SourceAttributionDto.class);
                        sources.add(src);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to query FastAPI RAG service: {}", e.getMessage());
        }

        // Save Assistant Message
        AiMessage assistantMsg = new AiMessage();
        assistantMsg.setConversation(conversation);
        assistantMsg.setSender("ASSISTANT");
        assistantMsg.setContent(answer);
        assistantMsg.setRelevanceScore(relevanceScore);
        try {
            assistantMsg.setSourcesJson(objectMapper.writeValueAsString(sources));
        } catch (JsonProcessingException e) {
            assistantMsg.setSourcesJson("[]");
        }
        AiMessage savedAssistantMsg = messageRepository.save(assistantMsg);

        // Update conversation timestamp
        conversationRepository.save(conversation);

        AiGuideResponseDto res = new AiGuideResponseDto();
        res.setConversationId(conversation.getId());
        res.setMessageId(savedAssistantMsg.getId());
        res.setAnswer(answer);
        res.setLanguage(language);
        res.setSources(sources);
        res.setRelevanceScore(relevanceScore);
        res.setCreatedAt(savedAssistantMsg.getCreatedAt());
        return res;
    }

    @Transactional(readOnly = true)
    public Page<AiConversationDto> getUserConversations(UUID currentUserId, Pageable pageable) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(currentUserId, pageable)
                .map(this::mapConversationToDto);
    }

    @Transactional(readOnly = true)
    public AiConversationDto getConversationById(UUID conversationId, UUID currentUserId) {
        AiConversation conv = conversationRepository.findByIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found or access denied"));
        return mapConversationToDto(conv);
    }

    @Transactional
    public void deleteConversation(UUID conversationId, UUID currentUserId) {
        AiConversation conv = conversationRepository.findByIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found or access denied"));
        // Hard Delete
        conversationRepository.delete(conv);
    }

    private AiConversationDto mapConversationToDto(AiConversation conv) {
        AiConversationDto dto = new AiConversationDto();
        dto.setId(conv.getId());
        dto.setTitle(conv.getTitle());
        dto.setCreatedAt(conv.getCreatedAt());
        dto.setUpdatedAt(conv.getUpdatedAt());

        List<AiMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId());
        List<AiMessageDto> messageDtos = new ArrayList<>();
        for (AiMessage msg : messages) {
            AiMessageDto mDto = new AiMessageDto();
            mDto.setId(msg.getId());
            mDto.setSender(msg.getSender());
            mDto.setContent(msg.getContent());
            mDto.setRelevanceScore(msg.getRelevanceScore());
            mDto.setCreatedAt(msg.getCreatedAt());
            if (msg.getSourcesJson() != null) {
                try {
                    List<SourceAttributionDto> srcs = objectMapper.readValue(msg.getSourcesJson(), new TypeReference<List<SourceAttributionDto>>() {});
                    mDto.setSources(srcs);
                } catch (Exception e) {
                    mDto.setSources(Collections.emptyList());
                }
            }
            messageDtos.add(mDto);
        }
        dto.setMessages(messageDtos);
        return dto;
    }
}
