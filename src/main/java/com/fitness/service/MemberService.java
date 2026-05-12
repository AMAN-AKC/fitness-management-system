package com.fitness.service;

import com.fitness.dto.MemberDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.Member;
import com.fitness.entity.SystemUser;
import com.fitness.enums.Role;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepo;
	private final BranchRepository branchRepo;
	private final SystemUserRepository userRepo;
	private final ModelMapper mapper;
	private final PasswordEncoder passwordEncoder;

	public MemberDTO createMember(MemberDTO dto) {
		if (memberRepo.existsByEmail(dto.getEmail()))
			throw new DuplicateResourceException("Member", "email", dto.getEmail());
		if (memberRepo.existsByPhone(dto.getPhone()))
			throw new DuplicateResourceException("Member", "phone", dto.getPhone());
		if (userRepo.existsByEmail(dto.getEmail()))
			throw new DuplicateResourceException("User", "email", dto.getEmail());

		Branch branch = branchRepo.findById(dto.getHomeBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getHomeBranchId()));

		SystemUser createdBy = getCurrentUser();
		SystemUser memberUser = SystemUser.builder()
				.username(generateUsername(dto))
				.email(dto.getEmail())
				.passwordHash(passwordEncoder.encode(dto.getPhone()))
				.role(Role.MEMBER)
				.active(true)
				.failedAttempts(0)
				.build();

		Member member = mapper.map(dto, Member.class);
		member.setHomeBranch(branch);
		member.setUser(userRepo.save(memberUser));
		member.setCreatedBy(createdBy);
		member.setStatus(Member.Status.PROSPECT);
		return mapper.map(memberRepo.save(member), MemberDTO.class);
	}

	public List<MemberDTO> getAllMembers() {
		return memberRepo.findAll().stream()
				.map(m -> mapper.map(m, MemberDTO.class))
				.collect(Collectors.toList());
	}

	public MemberDTO getMemberById(Long id) {
		return mapper.map(findById(id), MemberDTO.class);
	}

	public MemberDTO updateMember(Long id, MemberDTO dto) {
		Member member = findById(id);
		mapper.map(dto, member);
		return mapper.map(memberRepo.save(member), MemberDTO.class);
	}

	public void deactivateMember(Long id) {
		Member member = findById(id);
		member.setStatus(Member.Status.DEACTIVATED);
		memberRepo.save(member);
	}

	public List<MemberDTO> getMembersByBranch(Long branchId) {
		return memberRepo.findByHomeBranchBranchId(branchId).stream()
				.map(m -> mapper.map(m, MemberDTO.class))
				.collect(Collectors.toList());
	}

	private Member findById(Long id) {
		return memberRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
	}

	private SystemUser getCurrentUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepo.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("SystemUser", "username", username));
	}

	private String generateUsername(MemberDTO dto) {
		String base = dto.getEmail().split("@")[0].replaceAll("[^A-Za-z0-9._-]", "");
		String username = base.isBlank() ? "member" + dto.getPhone() : base;
		if (!userRepo.existsByUsername(username)) {
			return username;
		}
		String suffix = dto.getPhone().length() > 4 ? dto.getPhone().substring(dto.getPhone().length() - 4)
				: dto.getPhone();
		return username + suffix;
	}

	/**
	 * Update member's photo/ID path (AC06)
	 * 
	 * @param id        Member ID
	 * @param photoPath File path of uploaded photo
	 * @return Updated MemberDTO
	 */
	public MemberDTO updatePhotoPath(Long id, String photoPath) {
		Member member = findById(id);
		member.setPhotoPath(photoPath);
		return mapper.map(memberRepo.save(member), MemberDTO.class);
	}
}
