package com.fitness.service;

import com.fitness.dto.SystemUserDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.SystemUser;
import com.fitness.entity.Trainer;
import com.fitness.enums.Role;
import com.fitness.exception.DuplicateResourceException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.SystemUserRepository;
import com.fitness.repository.TrainerRepository;
import com.fitness.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SystemUserServiceTest {

    @InjectMocks
    private SystemUserService systemUserService;

    @Mock
    private SystemUserRepository userRepo;
    @Mock
    private BranchRepository branchRepo;
    @Mock
    private ModelMapper mapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordValidationService passwordValidator;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private TrainerRepository trainerRepo;
    @Mock
    private MemberRepository memberRepo;

    private SystemUser mockUser;
    private SystemUserDTO mockDto;
    private Branch mockBranch;

    @BeforeEach
    void setUp() {
        mockUser = new SystemUser();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("testuser@example.com");
        mockUser.setRole(Role.MEMBER);

        mockDto = new SystemUserDTO();
        mockDto.setUsername("testuser");
        mockDto.setEmail("testuser@example.com");
        mockDto.setFullName("Test User");

        mockBranch = new Branch();
        mockBranch.setBranchId(10L);
        mockBranch.setBranchName("Downtown Branch");
    }

    @Test
    void createUser_Success() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(mapper.map(mockDto, SystemUser.class)).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpass");
        when(userRepo.save(any())).thenReturn(mockUser);
        when(mapper.map(mockUser, SystemUserDTO.class)).thenReturn(mockDto);

        SystemUserDTO result = systemUserService.createUser(mockDto, "SecurePass123!");

        assertNotNull(result);
        assertEquals("Global Access", result.getBranchName());
        verify(passwordValidator).validatePassword("SecurePass123!");
        verify(userRepo).save(mockUser);
    }
    
    @Test
    void createUser_WithBranch_Success() {
        mockDto.setBranchName("Downtown Branch");
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(mapper.map(mockDto, SystemUser.class)).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpass");
        
        when(branchRepo.findByBranchNameIgnoreCase("Downtown Branch")).thenReturn(Optional.of(mockBranch));
        
        SystemUser savedUser = new SystemUser();
        savedUser.setUserId(1L);
        savedUser.setBranch(mockBranch);
        when(userRepo.save(any())).thenReturn(savedUser);
        
        when(mapper.map(savedUser, SystemUserDTO.class)).thenReturn(mockDto);

        SystemUserDTO result = systemUserService.createUser(mockDto, "SecurePass123!");

        assertNotNull(result);
        assertEquals("Downtown Branch", result.getBranchName());
    }

    @Test
    void createUser_AsTrainer_CreatesTrainerEntity() {
        mockDto.setRole(Role.TRAINER);
        mockUser.setRole(Role.TRAINER);
        
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(mapper.map(mockDto, SystemUser.class)).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpass");
        when(userRepo.save(any())).thenReturn(mockUser);
        when(mapper.map(mockUser, SystemUserDTO.class)).thenReturn(mockDto);

        SystemUserDTO result = systemUserService.createUser(mockDto, "SecurePass123!");

        assertNotNull(result);
        verify(trainerRepo).save(any(Trainer.class));
    }

    @Test
    void createUser_DuplicateUsername_ThrowsException() {
        when(userRepo.existsByUsername("testuser")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> systemUserService.createUser(mockDto, "pass"));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> systemUserService.createUser(mockDto, "pass"));
    }

    @Test
    void getAllUsers_Success() {
        SystemUser admin = new SystemUser();
        admin.setRole(Role.ADMIN);
        
        SystemUser trainer = new SystemUser();
        trainer.setUserId(2L);
        trainer.setRole(Role.TRAINER);

        SystemUser member = new SystemUser();
        member.setUserId(3L);
        member.setRole(Role.MEMBER);

        when(userRepo.findAll()).thenReturn(Arrays.asList(admin, trainer, member));
        
        SystemUserDTO adminDto = new SystemUserDTO(); adminDto.setRole(Role.ADMIN);
        SystemUserDTO trainerDto = new SystemUserDTO(); trainerDto.setRole(Role.TRAINER);
        SystemUserDTO memberDto = new SystemUserDTO(); memberDto.setRole(Role.MEMBER);
        
        when(mapper.map(admin, SystemUserDTO.class)).thenReturn(adminDto);
        when(mapper.map(trainer, SystemUserDTO.class)).thenReturn(trainerDto);
        when(mapper.map(member, SystemUserDTO.class)).thenReturn(memberDto);
        
        when(jdbcTemplate.queryForObject(contains("trainer"), eq(String.class), eq(2L))).thenReturn("Trainer Branch");
        when(jdbcTemplate.queryForObject(contains("member"), eq(String.class), eq(3L))).thenReturn("Member Branch");

        List<SystemUserDTO> result = systemUserService.getAllUsers();
        
        assertEquals(3, result.size());
        assertEquals("System HQ", result.get(0).getBranchName());
        assertEquals("Trainer Branch", result.get(1).getBranchName());
        assertEquals("Member Branch", result.get(2).getBranchName());
    }

    @Test
    void getUserById_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        when(mapper.map(mockUser, SystemUserDTO.class)).thenReturn(mockDto);
        
        SystemUserDTO result = systemUserService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals(mockDto.getUsername(), result.getUsername());
    }
    
    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> systemUserService.getUserById(99L));
    }

    @Test
    void updateUser_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepo.save(any())).thenReturn(mockUser);
        lenient().when(mapper.map(mockUser, SystemUserDTO.class)).thenReturn(mockDto);
        
        SystemUserDTO result = systemUserService.updateUser(1L, mockDto);
        
        assertNotNull(result);
        verify(mapper).map(mockDto, mockUser);
        verify(userRepo).save(mockUser);
    }
    
    @Test
    void updateUser_DuplicateUsername_ThrowsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        
        SystemUserDTO updateDto = new SystemUserDTO();
        updateDto.setUsername("newuser");
        updateDto.setEmail("newuser@example.com");
        
        when(userRepo.existsByUsername("newuser")).thenReturn(true);
        
        assertThrows(DuplicateResourceException.class, () -> systemUserService.updateUser(1L, updateDto));
    }

    @Test
    void deactivateUser_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        
        systemUserService.deactivateUser(1L);
        
        assertFalse(mockUser.isActive());
        verify(userRepo).save(mockUser);
    }

    @Test
    void lockUser_Success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        
        systemUserService.lockUser(1L);
        
        assertNotNull(mockUser.getLockedUntil());
        verify(userRepo).save(mockUser);
    }

    @Test
    void unlockUser_Success() {
        mockUser.setLockedUntil(java.time.LocalDateTime.now());
        mockUser.setFailedAttempts(5);
        when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));
        
        systemUserService.unlockUser(1L);
        
        assertNull(mockUser.getLockedUntil());
        assertEquals(0, mockUser.getFailedAttempts());
        verify(userRepo).save(mockUser);
    }
}
