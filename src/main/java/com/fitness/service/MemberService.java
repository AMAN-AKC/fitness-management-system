package com.fitness.service;

import com.fitness.dto.MemberDTO;
import com.fitness.entity.AuditLog;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepo;
	private final BranchRepository branchRepo;
	private final SystemUserRepository userRepo;
	private final ModelMapper mapper;
	private final PasswordEncoder passwordEncoder;
	private final AuditLogService auditLogService;

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
		Member saved = memberRepo.save(member);

		// AC07: Audit log for member creation
		auditLogService.logForCurrentUser("Member", saved.getMemberId(),
				AuditLog.Action.CREATE, null,
				"{\"mem_name\":\"" + saved.getMemName() + "\",\"status\":\"PROSPECT\"}");

		return mapper.map(saved, MemberDTO.class);
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
		String oldStatus = member.getStatus().name();
		mapper.map(dto, member);
		Member saved = memberRepo.save(member);

		// AC07: Audit log for member update
		auditLogService.logForCurrentUser("Member", saved.getMemberId(),
				AuditLog.Action.UPDATE,
				"{\"status\":\"" + oldStatus + "\"}",
				"{\"status\":\"" + saved.getStatus().name() + "\"}");

		return mapper.map(saved, MemberDTO.class);
	}

	public void deactivateMember(Long id) {
		Member member = findById(id);
		String oldStatus = member.getStatus().name();
		member.setStatus(Member.Status.DEACTIVATED);
		memberRepo.save(member);

		// Also deactivate the linked SystemUser so they cannot log in (AC09)
		if (member.getUser() != null) {
			member.getUser().setActive(false);
			userRepo.save(member.getUser());
		}

		// AC07: Audit log for deactivation
		auditLogService.logForCurrentUser("Member", member.getMemberId(),
				AuditLog.Action.UPDATE,
				"{\"status\":\"" + oldStatus + "\"}",
				"{\"status\":\"DEACTIVATED\"}");
	}

	public List<MemberDTO> getMembersByBranch(Long branchId) {
		return memberRepo.findByHomeBranchBranchId(branchId).stream()
				.map(m -> mapper.map(m, MemberDTO.class))
				.collect(Collectors.toList());
	}

	/**
	 * Update member's photo/ID path (AC06)
	 */
	public MemberDTO updatePhotoPath(Long id, String photoPath) {
		Member member = findById(id);
		member.setPhotoPath(photoPath);
		return mapper.map(memberRepo.save(member), MemberDTO.class);
	}

	/**
	 * Bulk upload members from CSV (AC10).
	 * Expected CSV headers: mem_name, email, phone, dob, address, emg_contact,
	 * emg_phone, home_branch_id, referral_code, corporate_code, notes
	 *
	 * @return A list of maps, one per row, containing "row", "status", and "message"
	 */
	public List<Map<String, String>> bulkUploadMembers(MultipartFile file) {
		List<Map<String, String>> report = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
			 CSVParser parser = CSVFormat.DEFAULT
					 .builder()
					 .setHeader()
					 .setSkipHeaderRecord(true)
					 .setIgnoreHeaderCase(true)
					 .setTrim(true)
					 .build()
					 .parse(reader)) {

			int row = 1;
			for (CSVRecord record : parser) {
				Map<String, String> rowResult = new LinkedHashMap<>();
				rowResult.put("row", String.valueOf(row));
				try {
					MemberDTO dto = MemberDTO.builder()
							.memName(record.get("mem_name"))
							.email(record.get("email"))
							.phone(record.get("phone"))
							.dob(record.get("dob"))
							.address(record.get("address"))
							.emgContact(record.get("emg_contact"))
							.emgPhone(record.get("emg_phone"))
							.homeBranchId(Long.parseLong(record.get("home_branch_id")))
							.referralCode(getOptionalField(record, "referral_code"))
							.corporateCode(getOptionalField(record, "corporate_code"))
							.notes(getOptionalField(record, "notes"))
							.build();

					// Validate required fields
					List<String> errors = validateMemberDto(dto);
					if (!errors.isEmpty()) {
						rowResult.put("status", "FAILED");
						rowResult.put("message", String.join("; ", errors));
					} else {
						MemberDTO created = createMember(dto);
						rowResult.put("status", "SUCCESS");
						rowResult.put("message", "Member created with ID: " + created.getMemberId());
					}
				} catch (DuplicateResourceException e) {
					rowResult.put("status", "FAILED");
					rowResult.put("message", "Duplicate: " + e.getMessage());
				} catch (Exception e) {
					rowResult.put("status", "FAILED");
					rowResult.put("message", e.getMessage());
				}
				report.add(rowResult);
				row++;
			}
		} catch (Exception e) {
			log.error("Error parsing CSV file", e);
			Map<String, String> errorRow = new LinkedHashMap<>();
			errorRow.put("row", "0");
			errorRow.put("status", "FAILED");
			errorRow.put("message", "CSV parsing error: " + e.getMessage());
			report.add(errorRow);
		}
		return report;
	}

	private String getOptionalField(CSVRecord record, String header) {
		try {
			String value = record.get(header);
			return (value == null || value.isBlank()) ? null : value;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private List<String> validateMemberDto(MemberDTO dto) {
		List<String> errors = new ArrayList<>();
		if (dto.getMemName() == null || dto.getMemName().isBlank())
			errors.add("Name is required");
		if (dto.getEmail() == null || !dto.getEmail().matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
			errors.add("Valid email is required");
		if (dto.getPhone() == null || !dto.getPhone().matches("^[6-9]\\d{9}$"))
			errors.add("Valid 10-digit phone starting with 6-9 is required");
		if (dto.getDob() == null || dto.getDob().isBlank())
			errors.add("Date of birth is required");
		if (dto.getAddress() == null || dto.getAddress().isBlank())
			errors.add("Address is required");
		if (dto.getEmgContact() == null || dto.getEmgContact().isBlank())
			errors.add("Emergency contact name is required");
		if (dto.getEmgPhone() == null || !dto.getEmgPhone().matches("^[6-9]\\d{9}$"))
			errors.add("Valid emergency phone is required");
		return errors;
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
}
