package org.epam.learn.resource_service.controller;

import java.util.List;
import java.util.Map;

import org.epam.learn.resource_service.service.ResourceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping(consumes = "audio/mpeg", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> upload(@RequestBody byte[] file) {
        return ResponseEntity.ok(resourceService.upload(file));
    }

    @GetMapping(value = "/{id}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> download(@PathVariable("id") Long id) {
        return ResponseEntity.ok(resourceService.download(id));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> delete(@RequestParam("id") List<String> id) {
        return ResponseEntity.ok(resourceService.delete(id));
    }
}
