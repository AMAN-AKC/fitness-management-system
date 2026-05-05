package com.fitness.service;

import com.fitness.dto.FacilityDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.Facility;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityService {

	private final FacilityRepository facilityRepo;
	private final BranchRepository branchRepo;
	private final ModelMapper mapper;

	public FacilityDTO createFacility(FacilityDTO dto) {
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
		Facility facility = mapper.map(dto, Facility.class);
		facility.setBranch(branch);
		facility.setIsActive(true);
		return mapper.map(facilityRepo.save(facility), FacilityDTO.class);
	}

	public List<FacilityDTO> getFacilitiesByBranch(Long branchId) {
		return facilityRepo.findByBranchBranchId(branchId).stream()
				.map(f -> mapper.map(f, FacilityDTO.class)).collect(Collectors.toList());
	}

	public FacilityDTO getFacilityById(Long id) {
		return mapper.map(findById(id), FacilityDTO.class);
	}

	public FacilityDTO updateFacility(Long id, FacilityDTO dto) {
		Facility facility = findById(id);
		mapper.map(dto, facility);
		return mapper.map(facilityRepo.save(facility), FacilityDTO.class);
	}

	public void deactivateFacility(Long id) {
		Facility f = findById(id);
		f.setIsActive(false);
		facilityRepo.save(f);
	}

	private Facility findById(Long id) {
		return facilityRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Facility", "id", id));
	}
}