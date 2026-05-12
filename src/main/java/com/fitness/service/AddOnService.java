package com.fitness.service;

import com.fitness.dto.AddOnDTO;
import com.fitness.entity.AddOn;
import com.fitness.entity.AuditLog.Action;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.AddOnRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddOnService {

	private final AddOnRepository addOnRepo;
	private final AuditLogService auditLogService;
	private final ModelMapper mapper;

	public AddOnDTO createAddOn(AddOnDTO dto) {
		AddOn addOn = mapper.map(dto, AddOn.class);
		addOn.setIsActive(true);
		AddOn saved = addOnRepo.save(addOn);
		auditLogService.logForCurrentUser("AddOn", saved.getAddonId(), Action.CREATE,
			"Add-on created: " + saved.getAddonName(), null);
		return mapper.map(saved, AddOnDTO.class);
	}

	public List<AddOnDTO> getAllAddOns() {
		return addOnRepo.findAll().stream().map(a -> mapper.map(a, AddOnDTO.class)).collect(Collectors.toList());
	}

	public AddOnDTO getAddOnById(Long id) {
		return mapper.map(findById(id), AddOnDTO.class);
	}

	public AddOnDTO updateAddOn(Long id, AddOnDTO dto) {
		AddOn addOn = findById(id);
		String oldValues = String.format("price=%s, taxPercent=%s", 
			addOn.getPrice(), addOn.getTaxPercent());
		mapper.map(dto, addOn);
		AddOn updated = addOnRepo.save(addOn);
		String newValues = String.format("price=%s, taxPercent=%s", 
			updated.getPrice(), updated.getTaxPercent());
		auditLogService.logForCurrentUser("AddOn", id, Action.UPDATE, oldValues, newValues);
		return mapper.map(updated, AddOnDTO.class);
	}

	public void deactivateAddOn(Long id) {
		AddOn a = findById(id);
		a.setIsActive(false);
		addOnRepo.save(a);
		auditLogService.logForCurrentUser("AddOn", id, Action.UPDATE, "isActive=true", "isActive=false");
	}

	private AddOn findById(Long id) {
		return addOnRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("AddOn", "id", id));
	}
}