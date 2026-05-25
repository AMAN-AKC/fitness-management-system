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
}
