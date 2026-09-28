package com.example.resourcebooking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.resourcebooking.dto.ResourceRequestDto;
import com.example.resourcebooking.dto.ResourceResponseDto;
import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.exception.ResourceNotFoundException;
import com.example.resourcebooking.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    public ResourceResponseDto createResource(
            ResourceRequestDto request) {

        Resource resource = new Resource();
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setPrice(request.getPrice());
        resource.setAvailable(request.getAvailable());

        return toDto(resourceRepository.save(resource));
    }

    @Override
    public List<ResourceResponseDto> getAllResources() {
        return resourceRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ResourceResponseDto getResourceById(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));

        return toDto(resource);
    }

    @Override
    public ResourceResponseDto updateResource(
            Long id,
            ResourceRequestDto request) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));

        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setPrice(request.getPrice());
        resource.setAvailable(request.getAvailable());

        return toDto(resourceRepository.save(resource));
    }

    @Override
    public void deleteResource(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));

        resourceRepository.delete(resource);
    }

    private ResourceResponseDto toDto(Resource resource) {
        return new ResourceResponseDto(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getPrice(),
                resource.getAvailable());
    }
}
