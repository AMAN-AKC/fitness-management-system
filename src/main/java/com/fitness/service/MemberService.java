package com.fitness.service;

import com.fitness.dto.MemberDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.Member;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepo;
	private final BranchRepository branchRepo;
	private final ModelMapper mapper;

	public MemberDTO createMember(MemberDTO dto) {
		if (memberRepo.existsByEmail(dto.getEmail()))
			throw new DuplicateResourceException("Member", "email", dto.getEmail());
		if (memberRepo.existsByPhone(dto.getPhone()))
			throw new DuplicateResourceException("Member", "phone", dto.getPhone());
		Branch branch = branchRepo.findById(dto.getHomeBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getHomeBranchId()));
		Member member = mapper.map(dto, Member.class);
		member.setHomeBranch(branch);
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
}
