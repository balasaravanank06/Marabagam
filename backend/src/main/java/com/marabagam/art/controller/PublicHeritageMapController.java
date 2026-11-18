package com.marabagam.art.controller;

import com.marabagam.art.dto.HeritageMapSummaryDto;
import com.marabagam.art.service.DistrictHeritageService;
import com.marabagam.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PublicHeritageMapController {

    @Autowired
    private DistrictHeritageService districtHeritageService;

    @GetMapping("/heritage-map")
    public ResponseEntity<ApiResponse<HeritageMapSummaryDto>> getHeritageMap() {
        long startTime = System.currentTimeMillis();
        HeritageMapSummaryDto summary = districtHeritageService.getHeritageMapSummary();
        long executionTime = System.currentTimeMillis() - startTime;
        return ResponseEntity.ok(ApiResponse.success("Heritage map summary retrieved successfully in " + executionTime + "ms", summary));
    }
}
