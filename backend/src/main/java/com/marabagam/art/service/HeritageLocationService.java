package com.marabagam.art.service;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.District;
import com.marabagam.art.domain.HeritageLocation;
import com.marabagam.art.dto.CreateHeritageLocationRequestDto;
import com.marabagam.art.dto.HeritageLocationDto;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.art.repository.HeritageLocationRepository;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class HeritageLocationService {

    private static final double MIN_LAT = 8.0;
    private static final double MAX_LAT = 13.5;
    private static final double MIN_LNG = 76.0;
    private static final double MAX_LNG = 80.5;

    @Autowired
    private HeritageLocationRepository heritageLocationRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private ArtFormRepository artFormRepository;

    @Autowired
    private DistrictHeritageService districtHeritageService;

    public HeritageLocationDto createLocation(CreateHeritageLocationRequestDto dto, User creator) {
        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        District district = districtRepository.findById(dto.getDistrictId())
            .orElseThrow(() -> new ResourceNotFoundException("District not found with id: " + dto.getDistrictId()));

        ArtForm artForm = null;
        if (dto.getArtFormId() != null) {
            artForm = artFormRepository.findById(dto.getArtFormId())
                .orElseThrow(() -> new ResourceNotFoundException("Art form not found with id: " + dto.getArtFormId()));
        }

        HeritageLocation loc = new HeritageLocation();
        loc.setName(dto.getName());
        loc.setNameTa(dto.getNameTa());
        loc.setSlug(generateSlug(dto.getName()));
        loc.setDistrict(district);
        loc.setArtForm(artForm);
        loc.setLocationType(dto.getLocationType());
        loc.setDescription(dto.getDescription());
        loc.setDescriptionTa(dto.getDescriptionTa());
        loc.setAddress(dto.getAddress());
        loc.setAddressTa(dto.getAddressTa());
        loc.setLatitude(dto.getLatitude());
        loc.setLongitude(dto.getLongitude());
        loc.setContactPhone(dto.getContactPhone());
        loc.setContactEmail(dto.getContactEmail());
        loc.setWebsiteUrl(dto.getWebsiteUrl());
        loc.setProvenanceSource(dto.getProvenanceSource());
        loc.setStatus("DRAFT");
        loc.setCreatedBy(creator);

        HeritageLocation saved = heritageLocationRepository.save(loc);
        return districtHeritageService.mapLocationToDto(saved);
    }

    public HeritageLocationDto updateLocation(UUID id, CreateHeritageLocationRequestDto dto, User currentUser) {
        HeritageLocation loc = heritageLocationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Heritage location not found with id: " + id));

        // Ownership Check (IDOR Protection)
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!loc.getCreatedBy().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to modify this location proposal.");
        }

        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        District district = districtRepository.findById(dto.getDistrictId())
            .orElseThrow(() -> new ResourceNotFoundException("District not found with id: " + dto.getDistrictId()));

        ArtForm artForm = null;
        if (dto.getArtFormId() != null) {
            artForm = artFormRepository.findById(dto.getArtFormId())
                .orElseThrow(() -> new ResourceNotFoundException("Art form not found with id: " + dto.getArtFormId()));
        }

        loc.setName(dto.getName());
        loc.setNameTa(dto.getNameTa());
        loc.setDistrict(district);
        loc.setArtForm(artForm);
        loc.setLocationType(dto.getLocationType());
        loc.setDescription(dto.getDescription());
        loc.setDescriptionTa(dto.getDescriptionTa());
        loc.setAddress(dto.getAddress());
        loc.setAddressTa(dto.getAddressTa());
        loc.setLatitude(dto.getLatitude());
        loc.setLongitude(dto.getLongitude());
        loc.setContactPhone(dto.getContactPhone());
        loc.setContactEmail(dto.getContactEmail());
        loc.setWebsiteUrl(dto.getWebsiteUrl());
        loc.setProvenanceSource(dto.getProvenanceSource());

        HeritageLocation updated = heritageLocationRepository.save(loc);
        return districtHeritageService.mapLocationToDto(updated);
    }

    public HeritageLocationDto updateStatus(UUID id, String newStatus, User currentUser) {
        HeritageLocation loc = heritageLocationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Heritage location not found with id: " + id));

        String s = newStatus.toUpperCase();
        if (!s.equals("DRAFT") && !s.equals("PENDING_REVIEW") && !s.equals("APPROVED") && !s.equals("REJECTED") && !s.equals("PUBLISHED") && !s.equals("ARCHIVED")) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        loc.setStatus(s);
        if (s.equals("ARCHIVED")) {
            loc.setArchivedBy(currentUser);
            loc.setArchivedAt(Instant.now());
        }

        HeritageLocation updated = heritageLocationRepository.save(loc);
        return districtHeritageService.mapLocationToDto(updated);
    }

    public void archiveLocation(UUID id, User currentUser) {
        updateStatus(id, "ARCHIVED", currentUser);
    }

    public void deleteLocation(UUID id) {
        HeritageLocation loc = heritageLocationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Heritage location not found with id: " + id));
        heritageLocationRepository.delete(loc);
    }

    @Transactional(readOnly = true)
    public List<HeritageLocationDto> getPublishedLocations() {
        return heritageLocationRepository.findByStatus("PUBLISHED").stream()
            .map(districtHeritageService::mapLocationToDto)
            .collect(Collectors.toList());
    }

    private void validateCoordinates(Double lat, Double lng) {
        if (lat == null || lng == null) {
            throw new IllegalArgumentException("Latitude and Longitude coordinates must not be null.");
        }
        if (lat < MIN_LAT || lat > MAX_LAT || lng < MIN_LNG || lng > MAX_LNG) {
            throw new IllegalArgumentException(String.format("Coordinates (%.5f, %.5f) are outside Tamil Nadu bounding box [%.1f-%.1f N, %.1f-%.1f E].", lat, lng, MIN_LAT, MAX_LAT, MIN_LNG, MAX_LNG));
        }
    }

    private String generateSlug(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        String slug = base;
        int counter = 1;
        while (heritageLocationRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
