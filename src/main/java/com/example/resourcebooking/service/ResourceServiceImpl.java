package com.example.resourcebooking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public ResourceResponseDto createResource(
            ResourceRequestDto request) {

        Resource resource = new Resource();
        resource.setName(request.getName().trim());
        resource.setDescription(request.getDescription().trim());
        resource.setPrice(request.getPrice());
        resource.setAvailable(request.getAvailable());

        return toDto(resourceRepository.save(resource));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponseDto> getAllResources() {

        return resourceRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponseDto getResourceById(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));

        return toDto(resource);
    }

    @Override
    @Transactional
    public ResourceResponseDto updateResource(
            Long id,
            ResourceRequestDto request) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found"));

        resource.setName(request.getName().trim());
        resource.setDescription(request.getDescription().trim());
        resource.setPrice(request.getPrice());
        resource.setAvailable(request.getAvailable());

        return toDto(resourceRepository.save(resource));
    }

    @Override
    @Transactional
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
