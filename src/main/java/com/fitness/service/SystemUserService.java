package com.fitness.service;

import com.fitness.dto.SystemUserDTO;
import com.fitness.entity.SystemUser;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemUserService {

	private final SystemUserRepository userRepo;
	private final com.fitness.repository.BranchRepository branchRepo;
	private final ModelMapper mapper;
	private final PasswordEncoder passwordEncoder;
	private final PasswordValidationService passwordValidator;
	private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
	private final com.fitness.repository.TrainerRepository trainerRepo;

	public SystemUserDTO createUser(SystemUserDTO dto, String rawPassword) {
		// Validate password complexity
		passwordValidator.validatePassword(rawPassword);

		if (dto.getFullName() == null || dto.getFullName().isBlank()) {
			dto.setFullName(dto.getUsername());
		}
		if (dto.getEmail() != null && dto.getEmail().contains("@")) {
			String autoUsername = dto.getEmail().substring(0, dto.getEmail().indexOf('@')).replaceAll("\\s+", "");
			dto.setUsername(autoUsername);
		}

		if (userRepo.existsByUsername(dto.getUsername()))
			throw new DuplicateResourceException("User", "username", dto.getUsername());
		if (userRepo.existsByEmail(dto.getEmail()))
			throw new DuplicateResourceException("User", "email", dto.getEmail());
		SystemUser user = mapper.map(dto, SystemUser.class);

		if (dto.getBranchName() != null && !dto.getBranchName().isBlank()) {
			branchRepo.findByBranchNameIgnoreCase(dto.getBranchName())
					.ifPresent(user::setBranch);
		}

		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		user.setActive(true);
		user.setFailedAttempts(0);
		
		SystemUser savedUser = userRepo.save(user);
		
		if (savedUser.getRole() == com.fitness.enums.Role.TRAINER) {
			com.fitness.entity.Trainer trainer = com.fitness.entity.Trainer.builder()
					.user(savedUser)
					.branch(savedUser.getBranch())
					.isActive(true)
					.acceptingPtClients(true)
					.rating(5.0)
					.bio("New trainer ready to help you achieve your goals.")
					.specialties("General Fitness")
					.build();
			trainerRepo.save(trainer);
		}
		
		SystemUserDTO responseDto = mapper.map(savedUser, SystemUserDTO.class);
		if (savedUser.getBranch() != null) {
			responseDto.setBranchName(savedUser.getBranch().getBranchName());
		} else if (savedUser.getRole() == com.fitness.enums.Role.ADMIN) {
			responseDto.setBranchName("System HQ");
		} else {
			responseDto.setBranchName("Unassigned");
		}
		return responseDto;
	}

	public List<SystemUserDTO> getAllUsers() {
		return userRepo.findAll().stream()
				.map(u -> {
					SystemUserDTO dto = mapper.map(u, SystemUserDTO.class);
					if (u.getBranch() != null) {
						dto.setBranchName(u.getBranch().getBranchName());
					} else if (u.getRole() == com.fitness.enums.Role.MEMBER) {
						try {
							String bName = jdbcTemplate.queryForObject("SELECT b.branch_name FROM branch b JOIN member m ON b.branch_id = m.home_branch_id WHERE m.user_id = ?", String.class, u.getUserId());
							dto.setBranchName(bName);
						} catch (Exception e) {}
					} else if (u.getRole() == com.fitness.enums.Role.TRAINER) {
						try {
							String bName = jdbcTemplate.queryForObject("SELECT b.branch_name FROM branch b JOIN trainer t ON b.branch_id = t.branch_id WHERE t.user_id = ?", String.class, u.getUserId());
							dto.setBranchName(bName);
						} catch (Exception e) {}
					} else if (u.getRole() == com.fitness.enums.Role.ADMIN) {
						dto.setBranchName("System HQ");
					} else {
						dto.setBranchName("Unassigned");
					}
					return dto;
				})
				.collect(Collectors.toList());
	}

	public SystemUserDTO getUserById(Long id) {
		return mapper.map(findById(id), SystemUserDTO.class);
	}

	public SystemUserDTO updateUser(Long id, SystemUserDTO dto) {
		SystemUser user = findById(id);

		if (dto.getFullName() == null || dto.getFullName().isBlank()) {
			dto.setFullName(dto.getUsername());
		}
		if (dto.getEmail() != null && dto.getEmail().contains("@")) {
			String autoUsername = dto.getEmail().substring(0, dto.getEmail().indexOf('@')).replaceAll("\\s+", "");
			dto.setUsername(autoUsername);
		}

		if (!user.getUsername().equals(dto.getUsername()) && userRepo.existsByUsername(dto.getUsername())) {
			throw new DuplicateResourceException("User", "username", dto.getUsername());
		}
		if (!user.getEmail().equals(dto.getEmail()) && userRepo.existsByEmail(dto.getEmail())) {
			throw new DuplicateResourceException("User", "email", dto.getEmail());
		}

		mapper.map(dto, user);

		if (dto.getBranchName() != null && !dto.getBranchName().isBlank()) {
			branchRepo.findByBranchNameIgnoreCase(dto.getBranchName())
					.ifPresent(user::setBranch);
		} else {
			user.setBranch(null);
		}

		SystemUser savedUser = userRepo.save(user);
		if (savedUser.getRole() == com.fitness.enums.Role.TRAINER) {
			com.fitness.entity.Trainer trainer = trainerRepo.findByUserUserId(savedUser.getUserId()).orElseGet(() ->
				com.fitness.entity.Trainer.builder()
					.user(savedUser)
					.isActive(true)
					.acceptingPtClients(true)
					.rating(5.0)
					.bio("New trainer ready to help you achieve your goals.")
					.specialties("General Fitness")
					.build()
			);
			trainer.setBranch(savedUser.getBranch());
			trainerRepo.save(trainer);
		}

		SystemUserDTO responseDto = mapper.map(savedUser, SystemUserDTO.class);
		if (savedUser.getBranch() != null) {
			responseDto.setBranchName(savedUser.getBranch().getBranchName());
		} else if (savedUser.getRole() == com.fitness.enums.Role.ADMIN) {
			responseDto.setBranchName("System HQ");
		} else {
			responseDto.setBranchName("Unassigned");
		}
		return responseDto;
	}

	public void deactivateUser(Long id) {
		SystemUser user = findById(id);
		user.setActive(false);
		userRepo.save(user);
	}

	public void lockUser(Long id) {
		SystemUser user = findById(id);
		user.setLockedUntil(java.time.LocalDateTime.now().plusYears(100)); // manual lock essentially forever
		userRepo.save(user);
	}

	public void unlockUser(Long id) {
		SystemUser user = findById(id);
		user.setLockedUntil(null);
		user.setFailedAttempts(0);
		userRepo.save(user);
	}

	private SystemUser findById(Long id) {
		return userRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", id));
	}
}
