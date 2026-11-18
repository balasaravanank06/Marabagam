package com.marabagam.art.controller;

import com.marabagam.art.dto.DistrictDetailDto;
import com.marabagam.art.dto.DistrictDto;
import com.marabagam.art.service.DistrictHeritageService;
import com.marabagam.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PublicDistrictController {

    @Autowired
    private DistrictHeritageService districtHeritageService;

    @GetMapping("/districts/search")
    public ResponseEntity<ApiResponse<Page<DistrictDto>>> searchDistricts(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Page<DistrictDto> result = districtHeritageService.searchDistricts(query, page, size);
        return ResponseEntity.ok(ApiResponse.success("Districts search completed successfully", result));
    }

    @GetMapping("/districts/{id}")
    public ResponseEntity<ApiResponse<DistrictDetailDto>> getDistrictById(@PathVariable Long id) {
        DistrictDetailDto district = districtHeritageService.getDistrictById(id);
        return ResponseEntity.ok(ApiResponse.success("District detail retrieved successfully", district));
    }

    @GetMapping("/art-forms/{slug}/districts")
    public ResponseEntity<ApiResponse<List<DistrictDto>>> getDistrictsByArtFormSlug(@PathVariable String slug) {
        List<DistrictDto> districts = districtHeritageService.getDistrictsByArtFormSlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Districts for art form retrieved successfully", districts));
    }
}
