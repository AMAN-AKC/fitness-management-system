package com.fitness.service;

import com.fitness.dto.AttendanceDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

	private final AttendanceRepository attendanceRepo;
	private final MemberRepository memberRepo;
	private final BranchRepository branchRepo;
	private final MembershipRepository membershipRepo;
	private final ModelMapper mapper;
	private static final int DUPLICATE_CHECK_MINUTES = 30;

	public AttendanceDTO checkIn(AttendanceDTO dto) {
		Member member = memberRepo.findById(dto.getMemberId())
				.orElseThrow(() -> new ResourceNotFoundException("Member", "id", dto.getMemberId()));
		Branch branch = branchRepo.findById(dto.getBranchId())
				.orElseThrow(() -> new ResourceNotFoundException("Branch", "id", dto.getBranchId()));

		// Verify active membership
		boolean hasActiveMembership = membershipRepo
				.findByMemberMemberIdAndStatus(member.getMemberId(), Membership.Status.ACTIVE)
				.isPresent();
		if (!hasActiveMembership)
			throw new BusinessRuleException("Member does not have an active membership. Check-in denied.");

		// Duplicate check-in guard
		LocalDateTime from = LocalDateTime.now().minusMinutes(DUPLICATE_CHECK_MINUTES);
		boolean alreadyCheckedIn = attendanceRepo
				.existsByMemberMemberIdAndCheckInTimeBetween(member.getMemberId(), from, LocalDateTime.now());
		if (alreadyCheckedIn)
			throw new BusinessRuleException(
					"Duplicate check-in detected within the last " + DUPLICATE_CHECK_MINUTES + " minutes.");

		Attendance attendance = mapper.map(dto, Attendance.class);
		attendance.setMember(member);
		attendance.setBranch(branch);
		attendance.setCheckInTime(LocalDateTime.now());
		attendance.setSyncStatus(Attendance.SyncStatus.SYNCED);
		return mapper.map(attendanceRepo.save(attendance), AttendanceDTO.class);
	}

	public List<AttendanceDTO> getAttendanceByMember(Long memberId) {
		return attendanceRepo.findByMemberMemberId(memberId).stream()
				.map(a -> mapper.map(a, AttendanceDTO.class)).collect(Collectors.toList());
	}

	public List<AttendanceDTO> getTodayAttendanceByBranch(Long branchId) {
		LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
		return attendanceRepo.findByCheckInTimeBetween(startOfDay, LocalDateTime.now()).stream()
				.filter(a -> a.getBranch().getBranchId().equals(branchId))
				.map(a -> mapper.map(a, AttendanceDTO.class)).collect(Collectors.toList());
	}
}
