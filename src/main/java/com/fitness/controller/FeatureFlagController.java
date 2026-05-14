package com.fitness.controller;

import com.fitness.entity.FeatureFlag;
import com.fitness.service.FeatureFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/feature-flags")
@CrossOrigin(origins = "*")
public class FeatureFlagController {
    @Autowired
    private FeatureFlagService service;

    @GetMapping
    public List<FeatureFlag> getAllFlags() {
        return service.getAllFlags();
    }

    @PutMapping("/{id}")
    public FeatureFlag updateFlag(@PathVariable Long id, @RequestParam boolean enabled, @RequestParam String modifiedBy) {
        return service.updateFlag(id, enabled, modifiedBy);
    }
}
