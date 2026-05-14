package com.fitness.service;

import com.fitness.entity.FeatureFlag;
import com.fitness.repository.FeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FeatureFlagService {
    @Autowired
    private FeatureFlagRepository repository;

    public List<FeatureFlag> getAllFlags() {
        return repository.findAll();
    }

    public FeatureFlag updateFlag(Long id, boolean enabled, String modifiedBy) {
        FeatureFlag flag = repository.findById(id).orElseThrow(() -> new RuntimeException("Flag not found"));
        flag.setEnabled(enabled);
        flag.setLastModifiedBy(modifiedBy);
        return repository.save(flag);
    }
}
