package org.epam.learn.storage_service.controller;

import java.util.List;
import java.util.Map;

import org.epam.learn.storage_service.model.StorageDto;
import org.epam.learn.storage_service.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/storages")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> createStorage(@RequestBody @Valid StorageDto storageDto) {
        return ResponseEntity.ok(storageService.create(storageDto));
    }

    @GetMapping
    public ResponseEntity<List<StorageDto>> getAllStorages() {
        return ResponseEntity.ok(storageService.getAll());
    }

    @DeleteMapping
    public ResponseEntity<List<Long>> deleteStorages(@RequestParam("id") List<String> id) {
        return ResponseEntity.ok(storageService.delete(id));
    }
}
