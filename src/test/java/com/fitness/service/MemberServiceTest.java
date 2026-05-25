package com.fitness.service;

import com.fitness.dto.MemberDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.Member;
import com.fitness.entity.SystemUser;
import com.fitness.enums.Role;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.PromoCodeRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.fitness.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepo;

    @Mock
    private BranchRepository branchRepo;

    @Mock
    private SystemUserRepository userRepo;

    @Mock
    private ModelMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PromoCodeRepository promoRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createMember_Success() {
        MemberDTO dto = MemberDTO.builder()
                .memName("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .dob("1990-01-01")
                .homeBranchId(1L)
                .build();

        Branch branch = new Branch();
        branch.setBranchId(1L);

        SystemUser currentUser = new SystemUser();
        currentUser.setUsername("admin");
        currentUser.setRole(Role.ADMIN);

        Member member = new Member();
        member.setMemberId(1L);
        member.setEmail("john@example.com");

        when(memberRepo.existsByEmail(anyString())).thenReturn(false);
        when(memberRepo.existsByPhone(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(currentUser));
        
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepo.save(any(SystemUser.class))).thenReturn(new SystemUser());
        when(mapper.map(dto, Member.class)).thenReturn(member);
        when(memberRepo.save(any(Member.class))).thenReturn(member);
        when(mapper.map(member, MemberDTO.class)).thenReturn(dto);

        MemberDTO result = memberService.createMember(dto);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        verify(memberRepo).save(any(Member.class));
        verify(promoRepo).save(any());
        verify(emailService).sendRegistrationWelcomeEmail(anyString(), any(), any(), any(), any());
    }

    @Test
    void createMember_DuplicateEmail_ThrowsException() {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").build();
        when(memberRepo.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> memberService.createMember(dto));
    }

    @Test
    void getAllMembers_Admin_ReturnsAll() {
        SystemUser currentUser = new SystemUser();
        currentUser.setUsername("admin");
        currentUser.setRole(Role.ADMIN);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(currentUser));

        Member member = new Member();
        when(memberRepo.findAll()).thenReturn(List.of(member));
        when(mapper.map(any(), eq(MemberDTO.class))).thenReturn(new MemberDTO());

        List<MemberDTO> result = memberService.getAllMembers();
        assertFalse(result.isEmpty());
    }

    @Test
    void createMember_DuplicatePhone_ThrowsException() {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").phone("1234567890").build();
        lenient().when(memberRepo.existsByEmail(anyString())).thenReturn(false);
        lenient().when(memberRepo.existsByPhone(anyString())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> memberService.createMember(dto));
    }

    @Test
    void createMember_DuplicateUserEmail_ThrowsException() {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").phone("1234567890").build();
        lenient().when(memberRepo.existsByEmail(anyString())).thenReturn(false);
        lenient().when(memberRepo.existsByPhone(anyString())).thenReturn(false);
        lenient().when(userRepo.existsByEmail(anyString())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> memberService.createMember(dto));
    }

    @Test
    void createMember_InvalidReferralCode_ThrowsException() {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").phone("1234567890").referralCode("INV").build();
        lenient().when(memberRepo.existsByEmail(anyString())).thenReturn(false);
        lenient().when(memberRepo.existsByPhone(anyString())).thenReturn(false);
        lenient().when(userRepo.existsByEmail(anyString())).thenReturn(false);
        lenient().when(promoRepo.existsByCode("INV")).thenReturn(false);
        assertThrows(BusinessRuleException.class, () -> memberService.createMember(dto));
    }

    @Test
    void createMember_InvalidCorporateCode_ThrowsException() {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").phone("1234567890").corporateCode("INV").build();
        lenient().when(memberRepo.existsByEmail(anyString())).thenReturn(false);
        lenient().when(memberRepo.existsByPhone(anyString())).thenReturn(false);
        lenient().when(userRepo.existsByEmail(anyString())).thenReturn(false);
        lenient().when(promoRepo.existsByCode("INV")).thenReturn(false);
        assertThrows(BusinessRuleException.class, () -> memberService.createMember(dto));
    }

    @Test
    void createMember_InvalidDobBlank_ThrowsException() {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").phone("1234567890").dob("").build();
        assertThrows(BusinessRuleException.class, () -> memberService.createMember(dto));
    }

    @Test
    void createMember_InvalidDobFormat_ThrowsException() {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").phone("1234567890").dob("invalid").build();
        assertThrows(BusinessRuleException.class, () -> memberService.createMember(dto));
    }

    @Test
    void createMember_Under14_ThrowsException() {
        MemberDTO dto = MemberDTO.builder().email("john@example.com").phone("1234567890").dob(LocalDate.now().minusYears(10).toString()).build();
        assertThrows(BusinessRuleException.class, () -> memberService.createMember(dto));
    }

    @Test
    void createMember_BranchNotFound_ThrowsException() {
        MemberDTO dto = MemberDTO.builder()
                .email("john@example.com")
                .phone("1234567890")
                .dob(LocalDate.now().minusYears(20).toString())
                .homeBranchId(99L)
                .build();
        lenient().when(branchRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> memberService.createMember(dto));
    }

    @Test
    void getAllMembers_NonAdminWithBranch() {
        SystemUser currentUser = new SystemUser();
        currentUser.setUsername("manager");
        currentUser.setRole(Role.MANAGER);
        Branch branch = new Branch();
        branch.setBranchId(10L);
        currentUser.setBranch(branch);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("manager");
        lenient().when(userRepo.findByUsername("manager")).thenReturn(Optional.of(currentUser));

        lenient().when(memberRepo.findByHomeBranchBranchId(10L)).thenReturn(List.of(new Member()));
        lenient().when(mapper.map(any(), eq(MemberDTO.class))).thenReturn(new MemberDTO());

        List<MemberDTO> result = memberService.getAllMembers();
        assertFalse(result.isEmpty());
    }

    @Test
    void getMemberById_Found() {
        Member member = new Member();
        lenient().when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(mapper.map(member, MemberDTO.class)).thenReturn(new MemberDTO());
        assertNotNull(memberService.getMemberById(1L));
    }

    @Test
    void getMemberById_NotFound() {
        lenient().when(memberRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> memberService.getMemberById(1L));
    }

    @Test
    void updateMember_Success() {
        MemberDTO dto = MemberDTO.builder().dob(LocalDate.now().minusYears(20).toString()).build();
        Member member = new Member();
        member.setStatus(Member.Status.PROSPECT);
        SystemUser user = new SystemUser();
        member.setUser(user);
        member.setHomeBranch(new Branch());

        lenient().when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(memberRepo.save(any(Member.class))).thenReturn(member);
        lenient().when(mapper.map(any(), eq(MemberDTO.class))).thenReturn(new MemberDTO());

        assertNotNull(memberService.updateMember(1L, dto));
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), any());
    }

    @Test
    void updateMember_InvalidDob() {
        MemberDTO dto = MemberDTO.builder().dob("invalid").build();
        Member member = new Member();
        member.setStatus(Member.Status.PROSPECT);
        lenient().when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        assertThrows(BusinessRuleException.class, () -> memberService.updateMember(1L, dto));
    }

    @Test
    void updateMember_Under14() {
        MemberDTO dto = MemberDTO.builder().dob(LocalDate.now().minusYears(10).toString()).build();
        Member member = new Member();
        member.setStatus(Member.Status.PROSPECT);
        lenient().when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        assertThrows(BusinessRuleException.class, () -> memberService.updateMember(1L, dto));
    }

    @Test
    void deactivateMember_Success() {
        Member member = new Member();
        member.setStatus(Member.Status.PROSPECT);
        SystemUser user = new SystemUser();
        member.setUser(user);

        lenient().when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        
        memberService.deactivateMember(1L);

        assertEquals(Member.Status.DEACTIVATED, member.getStatus());
        assertFalse(user.isActive());
        verify(memberRepo).save(member);
        verify(userRepo).save(user);
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), any());
    }

    @Test
    void getMembersByBranch() {
        lenient().when(memberRepo.findByHomeBranchBranchId(1L)).thenReturn(List.of(new Member()));
        lenient().when(mapper.map(any(), eq(MemberDTO.class))).thenReturn(new MemberDTO());
        assertFalse(memberService.getMembersByBranch(1L).isEmpty());
    }

    @Test
    void updatePhotoPath() {
        Member member = new Member();
        lenient().when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(memberRepo.save(any())).thenReturn(member);
        lenient().when(mapper.map(any(), eq(MemberDTO.class))).thenReturn(new MemberDTO());
        assertNotNull(memberService.updatePhotoPath(1L, "path"));
        assertEquals("path", member.getPhotoPath());
    }

    @Test
    void bulkUploadMembers_SuccessAndFailure() throws Exception {
        String csv = "mem_name,email,phone,dob,address,emg_contact,emg_phone,home_branch_id,referral_code,corporate_code,notes\n"
                   + "Valid Name,valid@test.com,1234567890,1990-01-01,Address,Contact,0987654321,1,,,\n"
                   + "Inv,invalid,phone,,addr,emg,phone,1,,,";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("admin");
        
        SystemUser currentUser = new SystemUser();
        currentUser.setUsername("admin");
        currentUser.setRole(Role.ADMIN);
        lenient().when(userRepo.findByUsername("admin")).thenReturn(Optional.of(currentUser));

        Branch branch = new Branch();
        branch.setBranchId(1L);
        lenient().when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        lenient().when(memberRepo.existsByEmail("valid@test.com")).thenReturn(false);
        lenient().when(memberRepo.existsByPhone("1234567890")).thenReturn(false);
        lenient().when(userRepo.existsByEmail("valid@test.com")).thenReturn(false);
        
        Member member = new Member();
        member.setMemberId(1L);
        member.setMemName("Valid Name");
        member.setEmail("valid@test.com");
        
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("pwd");
        lenient().when(userRepo.save(any())).thenReturn(new SystemUser());
        lenient().when(mapper.map(any(MemberDTO.class), eq(Member.class))).thenReturn(member);
        lenient().when(memberRepo.save(any())).thenReturn(member);
        lenient().when(mapper.map(any(Member.class), eq(MemberDTO.class))).thenReturn(new MemberDTO());

        List<java.util.Map<String, String>> result = memberService.bulkUploadMembers(file);
        assertEquals(2, result.size());
        assertEquals("SUCCESS", result.get(0).get("status"));
        assertEquals("FAILED", result.get(1).get("status"));
    }

    @Test
    void bulkUploadMembers_DuplicateException() throws Exception {
        String csv = "mem_name,email,phone,dob,address,emg_contact,emg_phone,home_branch_id,referral_code,corporate_code,notes\n"
                   + "Valid Name,valid@test.com,1234567890,1990-01-01,Address,Contact,0987654321,1,,,";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());
        lenient().when(memberRepo.existsByEmail("valid@test.com")).thenReturn(true);
        List<java.util.Map<String, String>> result = memberService.bulkUploadMembers(file);
        assertEquals("FAILED", result.get(0).get("status"));
        assertTrue(result.get(0).get("message").contains("Duplicate"));
    }

    @Test
    void bulkUploadMembers_CsvException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.getInputStream()).thenThrow(new java.io.IOException("Test"));
        List<java.util.Map<String, String>> result = memberService.bulkUploadMembers(file);
        assertEquals(1, result.size());
        assertEquals("FAILED", result.get(0).get("status"));
    }
}
