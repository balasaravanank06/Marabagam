package com.marabagam.art.service;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.domain.Category;
import com.marabagam.art.domain.District;
import com.marabagam.art.dto.*;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.CategoryRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.common.exception.AuthException;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ArtFormService {

    @Autowired
    private ArtFormRepository artFormRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private CategoryDistrictService categoryDistrictService;

    // Public API: Return ONLY PUBLISHED art forms
    @Transactional(readOnly = true)
    public Page<ArtFormSummaryDto> getPublishedArtForms(String category, String district, String query, Pageable pageable) {
        return artFormRepository.searchArtForms(ArtFormStatus.PUBLISHED, category, district, query, pageable)
                .map(this::mapToSummaryDto);
    }

    // Public API: Return single PUBLISHED art form by slug
    @Transactional(readOnly = true)
    public ArtFormResponseDto getPublishedArtFormBySlug(String slug) {
        ArtForm artForm = artFormRepository.findBySlugAndStatus(slug, ArtFormStatus.PUBLISHED)
                .orElseThrow(() -> new NoSuchElementException("Published art form not found with slug: " + slug));
        return mapToResponseDto(artForm);
    }

    // Admin / Management API: Return art forms with optional status filter
    @Transactional(readOnly = true)
    public Page<ArtFormSummaryDto> getArtFormsForManagement(ArtFormStatus status, String category, String district, String query, Pageable pageable) {
        return artFormRepository.searchArtForms(status, category, district, query, pageable)
                .map(this::mapToSummaryDto);
    }

    // Admin / Management API: Get single art form by ID regardless of status
    @Transactional(readOnly = true)
    public ArtFormResponseDto getArtFormById(UUID id) {
        ArtForm artForm = artFormRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Art form not found with id: " + id));
        return mapToResponseDto(artForm);
    }

    // Create Art Form (Initial status: DRAFT)
    public ArtFormResponseDto createArtForm(CreateArtFormRequestDto request, User actor) {
        if (actor.getRole() == Role.LEARNER || actor.getRole() == Role.GUEST) {
            throw new AuthException("FORBIDDEN_ROLE", "Learners cannot create cultural art form records.");
        }

        if (artFormRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Art form slug already exists: " + request.getSlug());
        }

        ArtForm artForm = new ArtForm();
        artForm.setName(request.getName());
        artForm.setNameTa(request.getNameTa());
        artForm.setSlug(request.getSlug().toLowerCase().trim());
        artForm.setShortDescription(request.getShortDescription());
        artForm.setShortDescriptionTa(request.getShortDescriptionTa());
        artForm.setDescription(request.getDescription());
        artForm.setDescriptionTa(request.getDescriptionTa());
        artForm.setOrigin(request.getOrigin());
        artForm.setOriginTa(request.getOriginTa());
        artForm.setHistoricalPeriod(request.getHistoricalPeriod());
        artForm.setHistoricalPeriodTa(request.getHistoricalPeriodTa());
        artForm.setCulturalSignificance(request.getCulturalSignificance());
        artForm.setCulturalSignificanceTa(request.getCulturalSignificanceTa());
        artForm.setPerformanceStyle(request.getPerformanceStyle());
        artForm.setPerformanceStyleTa(request.getPerformanceStyleTa());
        artForm.setCostume(request.getCostume());
        artForm.setCostumeTa(request.getCostumeTa());
        artForm.setInstruments(request.getInstruments());
        artForm.setInstrumentsTa(request.getInstrumentsTa());
        artForm.setStatus(ArtFormStatus.DRAFT);

        assignCategoriesAndDistricts(artForm, request.getCategoryIds(), request.getDistrictIds());

        ArtForm saved = artFormRepository.save(artForm);
        return mapToResponseDto(saved);
    }

    // Update Art Form
    public ArtFormResponseDto updateArtForm(UUID id, UpdateArtFormRequestDto request, User actor) {
        if (actor.getRole() == Role.LEARNER || actor.getRole() == Role.GUEST) {
            throw new AuthException("FORBIDDEN_ROLE", "Learners cannot modify cultural records.");
        }

        ArtForm artForm = artFormRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Art form not found with id: " + id));

        if (request.getName() != null) artForm.setName(request.getName());
        if (request.getNameTa() != null) artForm.setNameTa(request.getNameTa());
        if (request.getSlug() != null && !request.getSlug().equals(artForm.getSlug())) {
            if (artFormRepository.existsBySlug(request.getSlug())) {
                throw new IllegalArgumentException("Art form slug already exists: " + request.getSlug());
            }
            artForm.setSlug(request.getSlug().toLowerCase().trim());
        }
        if (request.getShortDescription() != null) artForm.setShortDescription(request.getShortDescription());
        if (request.getShortDescriptionTa() != null) artForm.setShortDescriptionTa(request.getShortDescriptionTa());
        if (request.getDescription() != null) artForm.setDescription(request.getDescription());
        if (request.getDescriptionTa() != null) artForm.setDescriptionTa(request.getDescriptionTa());
        if (request.getOrigin() != null) artForm.setOrigin(request.getOrigin());
        if (request.getOriginTa() != null) artForm.setOriginTa(request.getOriginTa());
        if (request.getHistoricalPeriod() != null) artForm.setHistoricalPeriod(request.getHistoricalPeriod());
        if (request.getHistoricalPeriodTa() != null) artForm.setHistoricalPeriodTa(request.getHistoricalPeriodTa());
        if (request.getCulturalSignificance() != null) artForm.setCulturalSignificance(request.getCulturalSignificance());
        if (request.getCulturalSignificanceTa() != null) artForm.setCulturalSignificanceTa(request.getCulturalSignificanceTa());
        if (request.getPerformanceStyle() != null) artForm.setPerformanceStyle(request.getPerformanceStyle());
        if (request.getPerformanceStyleTa() != null) artForm.setPerformanceStyleTa(request.getPerformanceStyleTa());
        if (request.getCostume() != null) artForm.setCostume(request.getCostume());
        if (request.getCostumeTa() != null) artForm.setCostumeTa(request.getCostumeTa());
        if (request.getInstruments() != null) artForm.setInstruments(request.getInstruments());
        if (request.getInstrumentsTa() != null) artForm.setInstrumentsTa(request.getInstrumentsTa());

        if (request.getCategoryIds() != null || request.getDistrictIds() != null) {
            assignCategoriesAndDistricts(artForm, request.getCategoryIds(), request.getDistrictIds());
        }

        ArtForm updated = artFormRepository.save(artForm);
        return mapToResponseDto(updated);
    }

    // Transition Content Lifecycle Status
    public ArtFormResponseDto updateArtFormStatus(UUID id, ArtFormStatus newStatus, User actor) {
        ArtForm artForm = artFormRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Art form not found with id: " + id));

        Role role = actor.getRole();

        if (role == Role.LEARNER || role == Role.GUEST) {
            throw new AuthException("FORBIDDEN_ROLE", "Learners cannot modify content lifecycle status.");
        }

        // Governance permission validation
        if (role == Role.ARTIST || role == Role.INSTRUCTOR) {
            if (newStatus != ArtFormStatus.DRAFT && newStatus != ArtFormStatus.PENDING_REVIEW) {
                throw new AuthException("FORBIDDEN_ROLE", "Artists and Instructors can only transition content to DRAFT or PENDING_REVIEW.");
            }
        } else if (role == Role.CULTURAL_EXPERT) {
            if (newStatus == ArtFormStatus.PUBLISHED && artForm.getStatus() != ArtFormStatus.APPROVED) {
                throw new AuthException("FORBIDDEN_ROLE", "Cultural Experts can approve content, but final publishing requires Admin sign-off or pre-approval.");
            }
        }

        artForm.setStatus(newStatus);
        ArtForm updated = artFormRepository.save(artForm);
        return mapToResponseDto(updated);
    }

    // Delete Art Form (ADMIN only)
    public void deleteArtForm(UUID id, User actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new AuthException("FORBIDDEN_ROLE", "Only administrators can delete cultural art form records.");
        }
        if (!artFormRepository.existsById(id)) {
            throw new NoSuchElementException("Art form not found with id: " + id);
        }
        artFormRepository.deleteById(id);
    }

    private void assignCategoriesAndDistricts(ArtForm artForm, Set<Long> categoryIds, Set<Long> districtIds) {
        if (categoryIds != null) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(categoryIds));
            artForm.setCategories(categories);
        }
        if (districtIds != null) {
            Set<District> districts = new HashSet<>(districtRepository.findAllById(districtIds));
            artForm.setDistricts(districts);
        }
    }

    public ArtFormSummaryDto mapToSummaryDto(ArtForm a) {
        ArtFormSummaryDto dto = new ArtFormSummaryDto();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setNameTa(a.getNameTa());
        dto.setSlug(a.getSlug());
        dto.setShortDescription(a.getShortDescription());
        dto.setShortDescriptionTa(a.getShortDescriptionTa());
        dto.setOrigin(a.getOrigin());
        dto.setOriginTa(a.getOriginTa());
        dto.setStatus(a.getStatus());
        dto.setCategories(a.getCategories().stream().map(categoryDistrictService::mapCategoryToDto).collect(Collectors.toList()));
        dto.setDistricts(a.getDistricts().stream().map(categoryDistrictService::mapDistrictToDto).collect(Collectors.toList()));
        return dto;
    }

    public ArtFormResponseDto mapToResponseDto(ArtForm a) {
        ArtFormResponseDto dto = new ArtFormResponseDto();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setNameTa(a.getNameTa());
        dto.setSlug(a.getSlug());
        dto.setShortDescription(a.getShortDescription());
        dto.setShortDescriptionTa(a.getShortDescriptionTa());
        dto.setDescription(a.getDescription());
        dto.setDescriptionTa(a.getDescriptionTa());
        dto.setOrigin(a.getOrigin());
        dto.setOriginTa(a.getOriginTa());
        dto.setHistoricalPeriod(a.getHistoricalPeriod());
        dto.setHistoricalPeriodTa(a.getHistoricalPeriodTa());
        dto.setCulturalSignificance(a.getCulturalSignificance());
        dto.setCulturalSignificanceTa(a.getCulturalSignificanceTa());
        dto.setPerformanceStyle(a.getPerformanceStyle());
        dto.setPerformanceStyleTa(a.getPerformanceStyleTa());
        dto.setCostume(a.getCostume());
        dto.setCostumeTa(a.getCostumeTa());
        dto.setInstruments(a.getInstruments());
        dto.setInstrumentsTa(a.getInstrumentsTa());
        dto.setStatus(a.getStatus());
        dto.setCategories(a.getCategories().stream().map(categoryDistrictService::mapCategoryToDto).collect(Collectors.toList()));
        dto.setDistricts(a.getDistricts().stream().map(categoryDistrictService::mapDistrictToDto).collect(Collectors.toList()));
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());
        return dto;
    }
}
