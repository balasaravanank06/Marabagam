package com.marabagam.art.controller;

import com.marabagam.art.dto.ArtFormResponseDto;
import com.marabagam.art.dto.ArtFormSummaryDto;
import com.marabagam.art.dto.CategoryDto;
import com.marabagam.art.dto.DistrictDto;
import com.marabagam.art.service.ArtFormService;
import com.marabagam.art.service.CategoryDistrictService;
import com.marabagam.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PublicArtFormController {

    @Autowired
    private ArtFormService artFormService;

    @Autowired
    private CategoryDistrictService categoryDistrictService;

    @GetMapping("/art-forms")
    public ResponseEntity<ApiResponse<Page<ArtFormSummaryDto>>> getPublishedArtForms(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<ArtFormSummaryDto> result = artFormService.getPublishedArtForms(category, district, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Published art forms retrieved successfully", result));
    }

    @GetMapping("/art-forms/{slug}")
    public ResponseEntity<ApiResponse<ArtFormResponseDto>> getArtFormBySlug(@PathVariable String slug) {
        ArtFormResponseDto artForm = artFormService.getPublishedArtFormBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Art form details retrieved successfully", artForm));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        List<CategoryDto> categories = categoryDistrictService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @GetMapping("/districts")
    public ResponseEntity<ApiResponse<List<DistrictDto>>> getDistricts() {
        List<DistrictDto> districts = categoryDistrictService.getAllDistricts();
        return ResponseEntity.ok(ApiResponse.success("Districts retrieved successfully", districts));
    }
}
