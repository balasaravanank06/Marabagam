package com.marabagam.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.auth.security.JwtTokenProvider;
import com.marabagam.quiz.domain.*;
import com.marabagam.quiz.dto.QuizSubmissionDto;
import com.marabagam.quiz.dto.SubmitAnswerDto;
import com.marabagam.quiz.repository.*;
import com.marabagam.user.domain.AccountStatus;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private User learner;
    private User artist;
    private String learnerToken;
    private String artistToken;
    private Quiz publishedQuiz;
    private QuizQuestion question1;
    private QuestionOption correctOption;
    private QuestionOption wrongOption;

    @BeforeEach
    void setUp() {
        learner = new User();
        learner.setEmail("quiz_learner_" + UUID.randomUUID() + "@marabagam.org");
        learner.setUsername("quiz_learner_" + UUID.randomUUID());
        learner.setPasswordHash("$2a$12$TestHashPasswordValueHere");
        learner.setDisplayName("Quiz Learner");
        learner.setRole(Role.LEARNER);
        learner.setAccountStatus(AccountStatus.ACTIVE);
        learner = userRepository.save(learner);

        artist = new User();
        artist.setEmail("quiz_artist_" + UUID.randomUUID() + "@marabagam.org");
        artist.setUsername("quiz_artist_" + UUID.randomUUID());
        artist.setPasswordHash("$2a$12$TestHashPasswordValueHere");
        artist.setDisplayName("Quiz Artist");
        artist.setRole(Role.ARTIST);
        artist.setAccountStatus(AccountStatus.ACTIVE);
        artist = userRepository.save(artist);

        learnerToken = tokenProvider.generateAccessToken(learner);
        artistToken = tokenProvider.generateAccessToken(artist);

        publishedQuiz = new Quiz();
        publishedQuiz.setScope(QuizScope.COURSE);
        publishedQuiz.setTitle("Heritage Assessment");
        publishedQuiz.setTitleTa("பாரம்பரிய மதிப்பீடு");
        publishedQuiz.setPassPercentage(80.0);
        publishedQuiz.setMaxAttempts(3);
        publishedQuiz.setTimeLimitMinutes(15);
        publishedQuiz.setStatus(QuizStatus.PUBLISHED);
        publishedQuiz.setCreatedBy(artist);
        publishedQuiz = quizRepository.save(publishedQuiz);

        question1 = new QuizQuestion();
        question1.setQuiz(publishedQuiz);
        question1.setQuestionText("What is the primary staff material in Silambam?");
        question1.setQuestionTextTa("சிலம்பத்தில் பயன்படுத்தப்படும் முதன்மையான பிரம்பு எது?");
        question1.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        question1.setPoints(10);
        question1.setDisplayOrder(1);
        question1 = quizQuestionRepository.save(question1);

        correctOption = new QuestionOption();
        correctOption.setQuestion(question1);
        correctOption.setOptionText("Kishi Bamboo / Mungil");
        correctOption.setOptionTextTa("கிஷி மூங்கில்");
        correctOption.setCorrect(true);
        correctOption.setDisplayOrder(1);
        correctOption = questionOptionRepository.save(correctOption);

        wrongOption = new QuestionOption();
        wrongOption.setQuestion(question1);
        wrongOption.setOptionText("Oak Wood");
        wrongOption.setOptionTextTa("ஓக் மரம்");
        wrongOption.setCorrect(false);
        wrongOption.setDisplayOrder(2);
        wrongOption = questionOptionRepository.save(wrongOption);
    }

    @Test
    void getQuiz_Success_ExcludesCorrectKey() throws Exception {
        mockMvc.perform(get("/api/v1/quizzes/" + publishedQuiz.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Heritage Assessment"))
                .andExpect(jsonPath("$.data.questions[0].options[0].isCorrect").doesNotExist());
    }

    @Test
    void startAttempt_Success() throws Exception {
        mockMvc.perform(post("/api/v1/quizzes/" + publishedQuiz.getId() + "/start")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attemptNumber").value(1))
                .andExpect(jsonPath("$.data.status").value("STARTED"));
    }

    @Test
    void submitAttempt_Success_ScoresServerSide() throws Exception {
        // Start attempt
        String responseStr = mockMvc.perform(post("/api/v1/quizzes/" + publishedQuiz.getId() + "/start")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + learnerToken))
                .andReturn().getResponse().getContentAsString();

        String attemptIdStr = objectMapper.readTree(responseStr).path("data").path("attemptId").asText();
        UUID attemptId = UUID.fromString(attemptIdStr);

        SubmitAnswerDto answer = new SubmitAnswerDto();
        answer.setQuestionId(question1.getId());
        answer.setSelectedOptionId(correctOption.getId());

        QuizSubmissionDto submission = new QuizSubmissionDto();
        submission.setAnswers(Collections.singletonList(answer));

        mockMvc.perform(post("/api/v1/quizzes/attempts/" + attemptId + "/submit")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.percentage").value(100.0))
                .andExpect(jsonPath("$.data.passed").value(true));
    }

    @Test
    void approveQuiz_BlockedSelfApproval() throws Exception {
        Quiz draftQuiz = new Quiz();
        draftQuiz.setScope(QuizScope.COURSE);
        draftQuiz.setTitle("Draft Quiz");
        draftQuiz.setTitleTa("வரைவு வினாடி வினா");
        draftQuiz.setStatus(QuizStatus.PENDING_REVIEW);
        draftQuiz.setCreatedBy(artist);
        draftQuiz = quizRepository.save(draftQuiz);

        mockMvc.perform(put("/api/v1/admin/quizzes/" + draftQuiz.getId() + "/approve")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + artistToken))
                .andExpect(status().isForbidden());
    }
}
