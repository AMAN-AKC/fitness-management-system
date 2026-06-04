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
	private final AuditLogService auditLogService;
	private final ModelMapper mapper;

	private FacilityDTO mapToDTO(Facility facility) {
		FacilityDTO dto = mapper.map(facility, FacilityDTO.class);
		if (facility.getBranch() != null) {
			dto.setBranchId(facility.getBranch().getBranchId());
		}
		return dto;
	}

	public FacilityDTO createFacility(FacilityDTO dto) {
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));
		Facility facility = mapper.map(dto, Facility.class);
		facility.setBranch(branch);
		facility.setIsActive(true);
		facility.setUnderMaintenance(false);
		return mapToDTO(facilityRepo.save(facility));
	}

	public List<FacilityDTO> getFacilitiesByBranch(Long branchId) {
		return facilityRepo.findByBranchBranchId(branchId).stream()
				.map(this::mapToDTO).collect(Collectors.toList());
	}

	public List<FacilityDTO> getAllFacilities() {
		return facilityRepo.findAll().stream()
				.map(this::mapToDTO).collect(Collectors.toList());
	}

	public FacilityDTO getFacilityById(Long id) {
		return mapToDTO(findById(id));
	}

	public FacilityDTO updateFacility(Long id, FacilityDTO dto) {
		Facility facility = findById(id);
		mapper.map(dto, facility);
		return mapToDTO(facilityRepo.save(facility));
	}

	public void deactivateFacility(Long id) {
		Facility f = findById(id);
		f.setIsActive(false);
		facilityRepo.save(f);
	}

	/**
	 * AC07: Toggle maintenance mode — blocks bookings during downtime
	 */
	public FacilityDTO toggleMaintenance(Long id, boolean underMaintenance, String reason) {
		Facility facility = findById(id);
		facility.setUnderMaintenance(underMaintenance);
		facility.setMaintenanceReason(underMaintenance ? reason : null);
		Facility saved = facilityRepo.save(facility);

		auditLogService.logForCurrentUser("Facility", id,
				com.fitness.entity.AuditLog.Action.UPDATE, null,
				underMaintenance
						? "Room set to maintenance: " + (reason != null ? reason : "No reason")
						: "Room maintenance ended");

		return mapToDTO(saved);
	}

	private Facility findById(Long id) {
		return facilityRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Facility", "id", id));
	}
}