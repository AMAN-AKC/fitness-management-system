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
	private final ModelMapper mapper;
	private final PasswordEncoder passwordEncoder;

	public SystemUserDTO createUser(SystemUserDTO dto, String rawPassword) {
		if (userRepo.existsByUsername(dto.getUsername()))
			throw new DuplicateResourceException("User", "username", dto.getUsername());
		if (userRepo.existsByEmail(dto.getEmail()))
			throw new DuplicateResourceException("User", "email", dto.getEmail());
		SystemUser user = mapper.map(dto, SystemUser.class);
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		user.setActive(true);
		user.setFailedAttempts(0);
		return mapper.map(userRepo.save(user), SystemUserDTO.class);
	}

	public List<SystemUserDTO> getAllUsers() {
		return userRepo.findAll().stream()
				.map(u -> mapper.map(u, SystemUserDTO.class))
				.collect(Collectors.toList());
	}

	public SystemUserDTO getUserById(Long id) {
		return mapper.map(findById(id), SystemUserDTO.class);
	}

	public SystemUserDTO updateUser(Long id, SystemUserDTO dto) {
		SystemUser user = findById(id);
		mapper.map(dto, user);
		return mapper.map(userRepo.save(user), SystemUserDTO.class);
	}

	public void deactivateUser(Long id) {
		SystemUser user = findById(id);
		user.setActive(false);
		userRepo.save(user);
	}

	private SystemUser findById(Long id) {
		return userRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "id", id));
	}
}
