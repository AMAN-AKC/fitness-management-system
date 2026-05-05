package com.fitness.service;

import com.fitness.dto.AddOnDTO;
import com.fitness.entity.AddOn;
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
	private final ModelMapper mapper;

	public AddOnDTO createAddOn(AddOnDTO dto) {
		AddOn addOn = mapper.map(dto, AddOn.class);
		addOn.setIsActive(true);
		return mapper.map(addOnRepo.save(addOn), AddOnDTO.class);
	}

	public List<AddOnDTO> getAllAddOns() {
		return addOnRepo.findAll().stream().map(a -> mapper.map(a, AddOnDTO.class)).collect(Collectors.toList());
	}

	public AddOnDTO getAddOnById(Long id) {
		return mapper.map(findById(id), AddOnDTO.class);
	}

	public AddOnDTO updateAddOn(Long id, AddOnDTO dto) {
		AddOn addOn = findById(id);
		mapper.map(dto, addOn);
		return mapper.map(addOnRepo.save(addOn), AddOnDTO.class);
	}

	public void deactivateAddOn(Long id) {
		AddOn a = findById(id);
		a.setIsActive(false);
		addOnRepo.save(a);
	}

	private AddOn findById(Long id) {
		return addOnRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("AddOn", "id", id));
	}
}