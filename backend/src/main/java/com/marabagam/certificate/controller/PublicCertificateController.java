package com.marabagam.certificate.controller;

import com.marabagam.certificate.dto.VerificationResponseDto;
import com.marabagam.certificate.service.CertificateService;
import com.marabagam.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/certificates/verify")
public class PublicCertificateController {

    @Autowired
    private CertificateService certificateService;

    @GetMapping("/{certificateCode}")
    public ResponseEntity<ApiResponse<VerificationResponseDto>> verifyCertificate(@PathVariable String certificateCode) {
        VerificationResponseDto result = certificateService.verifyCertificate(certificateCode);
        return ResponseEntity.ok(ApiResponse.success("Certificate verification completed", result));
    }
}
