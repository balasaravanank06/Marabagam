package com.marabagam.art.service;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.domain.CulturalEvent;
import com.marabagam.art.domain.District;
import com.marabagam.art.domain.HeritageLocation;
import com.marabagam.art.dto.*;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.CulturalEventRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.art.repository.HeritageLocationRepository;
import com.marabagam.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DistrictHeritageService {

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private ArtFormRepository artFormRepository;

    @Autowired
    private HeritageLocationRepository heritageLocationRepository;

    @Autowired
    private CulturalEventRepository culturalEventRepository;

    @Autowired
    private ArtFormService artFormService;

    public HeritageMapSummaryDto getHeritageMapSummary() {
        List<District> districts = districtRepository.findAll(Sort.by("name"));

        List<DistrictDto> dtos = districts.stream().map(d -> {
            long artFormCount = d.getId() != null ? artFormRepository.countByDistrictsIdAndStatus(d.getId(), ArtFormStatus.PUBLISHED) : 0;
            long locationCount = d.getId() != null ? heritageLocationRepository.countByDistrictIdAndStatus(d.getId(), "PUBLISHED") : 0;
            long eventCount = d.getId() != null ? culturalEventRepository.countByDistrictIdAndStatus(d.getId(), "PUBLISHED") : 0;

            return new DistrictDto(
                d.getId(), d.getName(), d.getNameTa(), d.getCode(),
                d.getLatitude(), d.getLongitude(),
                d.getShortHistory(), d.getShortHistoryTa(),
                d.getCulturalSignificance(), d.getCulturalSignificanceTa(),
                d.getGeojsonFeatureCode(), artFormCount, locationCount, eventCount
            );
        }).collect(Collectors.toList());

        long totalPublishedArtForms = artFormRepository.countByStatus(ArtFormStatus.PUBLISHED);
        long totalPublishedLocations = heritageLocationRepository.countByStatus("PUBLISHED");
        long totalPublishedEvents = culturalEventRepository.countByStatus("PUBLISHED");

        return new HeritageMapSummaryDto(
            "Tamil Nadu",
            districts.size(),
            totalPublishedArtForms,
            totalPublishedLocations,
            totalPublishedEvents,
            dtos
        );
    }

    public Page<DistrictDto> searchDistricts(String query, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<District> districtPage;

        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim();
            districtPage = districtRepository.findByNameContainingIgnoreCaseOrNameTaContainingIgnoreCase(q, q, pageable);
        } else {
            districtPage = districtRepository.findAll(pageable);
        }

        return districtPage.map(d -> {
            long artFormCount = artFormRepository.countByDistrictsIdAndStatus(d.getId(), ArtFormStatus.PUBLISHED);
            long locationCount = heritageLocationRepository.countByDistrictIdAndStatus(d.getId(), "PUBLISHED");
            long eventCount = culturalEventRepository.countByDistrictIdAndStatus(d.getId(), "PUBLISHED");
            return new DistrictDto(
                d.getId(), d.getName(), d.getNameTa(), d.getCode(),
                d.getLatitude(), d.getLongitude(),
                d.getShortHistory(), d.getShortHistoryTa(),
                d.getCulturalSignificance(), d.getCulturalSignificanceTa(),
                d.getGeojsonFeatureCode(), artFormCount, locationCount, eventCount
            );
        });
    }

    public DistrictDetailDto getDistrictById(Long id) {
        District d = districtRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("District not found with id: " + id));

        DistrictDetailDto dto = new DistrictDetailDto();
        dto.setId(d.getId());
        dto.setName(d.getName());
        dto.setNameTa(d.getNameTa());
        dto.setCode(d.getCode());
        dto.setLatitude(d.getLatitude());
        dto.setLongitude(d.getLongitude());
        dto.setShortHistory(d.getShortHistory());
        dto.setShortHistoryTa(d.getShortHistoryTa());
        dto.setCulturalSignificance(d.getCulturalSignificance());
        dto.setCulturalSignificanceTa(d.getCulturalSignificanceTa());
        dto.setGeojsonFeatureCode(d.getGeojsonFeatureCode());

        List<ArtForm> publishedArtForms = artFormRepository.findByDistrictsIdAndStatus(id, ArtFormStatus.PUBLISHED);
        List<ArtFormSummaryDto> artFormDtos = publishedArtForms.stream()
            .map(artFormService::mapToSummaryDto)
            .collect(Collectors.toList());
        dto.setArtForms(artFormDtos);

        // Get PUBLISHED heritage locations
        List<HeritageLocation> publishedLocations = heritageLocationRepository.findByDistrictIdAndStatus(id, "PUBLISHED");
        List<HeritageLocationDto> locationDtos = publishedLocations.stream()
            .map(this::mapLocationToDto)
            .collect(Collectors.toList());
        dto.setLocations(locationDtos);

        // Get PUBLISHED cultural events
        List<CulturalEvent> publishedEvents = culturalEventRepository.findByDistrictIdAndStatus(id, "PUBLISHED");
        List<CulturalEventDto> eventDtos = publishedEvents.stream()
            .map(this::mapEventToDto)
            .collect(Collectors.toList());
        dto.setEvents(eventDtos);

        return dto;
    }

    public List<DistrictDto> getDistrictsByArtFormSlug(String slug) {
        // Find art form to verify existence
        ArtForm artForm = artFormRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Art form not found with slug: " + slug));

        return artForm.getDistricts().stream()
            .map(d -> {
                long artFormCount = artFormRepository.countByDistrictsIdAndStatus(d.getId(), ArtFormStatus.PUBLISHED);
                long locationCount = heritageLocationRepository.countByDistrictIdAndStatus(d.getId(), "PUBLISHED");
                long eventCount = culturalEventRepository.countByDistrictIdAndStatus(d.getId(), "PUBLISHED");
                return new DistrictDto(
                    d.getId(), d.getName(), d.getNameTa(), d.getCode(),
                    d.getLatitude(), d.getLongitude(),
                    d.getShortHistory(), d.getShortHistoryTa(),
                    d.getCulturalSignificance(), d.getCulturalSignificanceTa(),
                    d.getGeojsonFeatureCode(), artFormCount, locationCount, eventCount
                );
            })
            .sorted((a, b) -> a.getName().compareTo(b.getName()))
            .collect(Collectors.toList());
    }

    public HeritageLocationDto mapLocationToDto(HeritageLocation loc) {
        HeritageLocationDto dto = new HeritageLocationDto();
        dto.setId(loc.getId());
        dto.setName(loc.getName());
        dto.setNameTa(loc.getNameTa());
        dto.setSlug(loc.getSlug());
        dto.setDistrictId(loc.getDistrict().getId());
        dto.setDistrictName(loc.getDistrict().getName());
        dto.setDistrictNameTa(loc.getDistrict().getNameTa());
        if (loc.getArtForm() != null) {
            dto.setArtFormId(loc.getArtForm().getId());
            dto.setArtFormName(loc.getArtForm().getName());
            dto.setArtFormNameTa(loc.getArtForm().getNameTa());
        }
        dto.setLocationType(loc.getLocationType());
        dto.setDescription(loc.getDescription());
        dto.setDescriptionTa(loc.getDescriptionTa());
        dto.setAddress(loc.getAddress());
        dto.setAddressTa(loc.getAddressTa());
        dto.setLatitude(loc.getLatitude());
        dto.setLongitude(loc.getLongitude());
        dto.setContactPhone(loc.getContactPhone());
        dto.setContactEmail(loc.getContactEmail());
        dto.setWebsiteUrl(loc.getWebsiteUrl());
        dto.setProvenanceSource(loc.getProvenanceSource());
        dto.setStatus(loc.getStatus());
        dto.setCreatedById(loc.getCreatedBy().getId());
        dto.setCreatedByName(loc.getCreatedBy().getUsername());
        dto.setCreatedAt(loc.getCreatedAt());
        return dto;
    }

    public CulturalEventDto mapEventToDto(CulturalEvent event) {
        CulturalEventDto dto = new CulturalEventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setTitleTa(event.getTitleTa());
        dto.setSlug(event.getSlug());
        dto.setDistrictId(event.getDistrict().getId());
        dto.setDistrictName(event.getDistrict().getName());
        dto.setDistrictNameTa(event.getDistrict().getNameTa());
        if (event.getArtForm() != null) {
            dto.setArtFormId(event.getArtForm().getId());
            dto.setArtFormName(event.getArtForm().getName());
            dto.setArtFormNameTa(event.getArtForm().getNameTa());
        }
        dto.setDescription(event.getDescription());
        dto.setDescriptionTa(event.getDescriptionTa());
        dto.setEventType(event.getEventType());
        dto.setEventPeriod(event.getEventPeriod());
        dto.setEventPeriodTa(event.getEventPeriodTa());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setIsDateApproximate(event.getIsDateApproximate());
        dto.setVenueName(event.getVenueName());
        dto.setVenueNameTa(event.getVenueNameTa());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setProvenanceSource(event.getProvenanceSource());
        dto.setStatus(event.getStatus());
        dto.setCreatedById(event.getCreatedBy().getId());
        dto.setCreatedByName(event.getCreatedBy().getUsername());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }
}
