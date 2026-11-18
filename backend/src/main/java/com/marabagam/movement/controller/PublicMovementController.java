package com.marabagam.movement.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.movement.dto.MovementResponseDto;
import com.marabagam.movement.service.MovementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movements")
public class PublicMovementController {

    private final MovementService movementService;

    public PublicMovementController(MovementService movementService) {
        this.movementService = movementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovementResponseDto>>> getMovements(
            @RequestParam(required = false) UUID artFormId) {
        List<MovementResponseDto> result = movementService.getMovements(artFormId);
        return ResponseEntity.ok(ApiResponse.success("Published movements retrieved successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovementResponseDto>> getMovementById(@PathVariable UUID id) {
        MovementResponseDto result = movementService.getMovementById(id);
        return ResponseEntity.ok(ApiResponse.success("Movement details retrieved successfully", result));
    }
}
