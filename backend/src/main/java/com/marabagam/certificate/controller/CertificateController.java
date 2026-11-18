package com.marabagam.certificate.controller;

import com.marabagam.certificate.dto.CertificateDto;
import com.marabagam.certificate.service.CertificateService;
import com.marabagam.common.response.ApiResponse;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/certificates")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<CertificateDto>>> getUserCertificates(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        List<CertificateDto> certificates = certificateService.getUserCertificates(user.getId());
        return ResponseEntity.ok(ApiResponse.success("User certificates retrieved successfully", certificates));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CertificateDto>> getCertificateById(
            @PathVariable UUID id,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        CertificateDto cert = certificateService.getCertificateById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Certificate details retrieved successfully", cert));
    }

    @DeleteMapping("/admin/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeCertificate(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "Policy violation or administrative order") String reason) {
        certificateService.revokeCertificate(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Certificate revoked successfully", null));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        String identifier = authentication != null ? authentication.getName() : null;
        if (identifier == null) {
            throw new ResourceNotFoundException("Unauthenticated user");
        }
        try {
            UUID userId = UUID.fromString(identifier);
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) return userOpt.get();
        } catch (IllegalArgumentException ignored) {}

        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found for identifier: " + identifier));
    }
}
