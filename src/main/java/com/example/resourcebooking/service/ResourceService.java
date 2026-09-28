package com.example.resourcebooking.service;

import java.util.List;

import com.example.resourcebooking.dto.ResourceRequestDto;
import com.example.resourcebooking.dto.ResourceResponseDto;

public interface ResourceService {

    ResourceResponseDto createResource(ResourceRequestDto request);

    List<ResourceResponseDto> getAllResources();

    ResourceResponseDto getResourceById(Long id);

    ResourceResponseDto updateResource(
            Long id,
            ResourceRequestDto request);

    void deleteResource(Long id);
}
