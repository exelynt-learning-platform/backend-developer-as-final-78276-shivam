package com.example.resourcebooking.controller;

import java.util.List;

import com.example.resourcebooking.dto.ResourceRequestDto;
import com.example.resourcebooking.dto.ResourceResponseDto;
import com.example.resourcebooking.service.ResourceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<ResourceResponseDto> createResource(
            @Valid @RequestBody ResourceRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.createResource(request));
    }

    @GetMapping
    public ResponseEntity<List<ResourceResponseDto>> getAllResources() {
        return ResponseEntity.ok(
                resourceService.getAllResources());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponseDto> getResourceById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                resourceService.getResourceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponseDto> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequestDto request) {

        return ResponseEntity.ok(
                resourceService.updateResource(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long id) {

        resourceService.deleteResource(id);

        return ResponseEntity.noContent().build();
    }
}
