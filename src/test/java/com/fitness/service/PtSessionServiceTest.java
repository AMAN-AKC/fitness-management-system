package com.fitness.service;

import com.fitness.dto.PtSessionDTO;
import com.fitness.entity.*;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.PtSessionRepository;
import com.fitness.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PtSessionServiceTest {

    @InjectMocks
    private PtSessionService ptSessionService;

    @Mock
    private PtSessionRepository ptRepo;
    @Mock
    private MemberRepository memberRepo;
    @Mock
    private TrainerRepository trainerRepo;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ModelMapper mapper;

    private Member mockMember;
    private Trainer mockTrainer;
    private SystemUser mockUser;
    private SystemUser trainerUser;
    private PtSessionDTO mockDto;
    private PtSession mockSession;

    @BeforeEach
    void setUp() {
        mockUser = new SystemUser();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");

        mockMember = new Member();
        mockMember.setMemberId(1L);
        mockMember.setUser(mockUser);
        mockMember.setPtSessionCredits(5);

        trainerUser = new SystemUser();
        trainerUser.setUserId(2L);
        trainerUser.setUsername("trainer");
        trainerUser.setFullName("Trainer John");

        mockTrainer = new Trainer();
        mockTrainer.setTrainerId(10L);
        mockTrainer.setUser(trainerUser);

        mockDto = new PtSessionDTO();
        mockDto.setMemberId(1L);
        mockDto.setTrainerId(10L);
        mockDto.setScheduledAt("2030-01-01T10:00:00");
        mockDto.setDurationMins(60);

        mockSession = new PtSession();
        mockSession.setSessionId(100L);
        mockSession.setMember(mockMember);
        mockSession.setTrainer(mockTrainer);
        mockSession.setScheduledAt(LocalDateTime.parse("2030-01-01T10:00:00"));
        mockSession.setDurationMins(60);
        mockSession.setStatus(PtSession.Status.REQUESTED);
    }

    @Test
    void requestSession_Success() {
        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));
        when(trainerRepo.findById(10L)).thenReturn(Optional.of(mockTrainer));
        when(ptRepo.findOverlappingForTrainer(eq(10L), any(), any())).thenReturn(Collections.emptyList());
        when(ptRepo.findByMemberMemberId(1L)).thenReturn(Collections.emptyList());
        
        when(mapper.map(mockDto, PtSession.class)).thenReturn(mockSession);
        when(ptRepo.save(any())).thenReturn(mockSession);
        when(mapper.map(mockSession, PtSessionDTO.class)).thenReturn(mockDto);

        PtSessionDTO result = ptSessionService.requestSession(mockDto);

        assertNotNull(result);
        verify(ptRepo).save(mockSession);
        verify(notificationService).sendNotification(anyLong(), any(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void requestSession_TrainerOverlap_ThrowsException() {
        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));
        when(trainerRepo.findById(10L)).thenReturn(Optional.of(mockTrainer));
        when(ptRepo.findOverlappingForTrainer(eq(10L), any(), any())).thenReturn(Collections.singletonList(mockSession));
        
        assertThrows(BusinessRuleException.class, () -> ptSessionService.requestSession(mockDto));
    }

    @Test
    void requestSession_SameDayOverlap_ThrowsException() {
        when(memberRepo.findById(1L)).thenReturn(Optional.of(mockMember));
        when(trainerRepo.findById(10L)).thenReturn(Optional.of(mockTrainer));
        when(ptRepo.findOverlappingForTrainer(eq(10L), any(), any())).thenReturn(Collections.emptyList());
        
        PtSession existing = new PtSession();
        existing.setStatus(PtSession.Status.REQUESTED);
        existing.setScheduledAt(LocalDateTime.parse("2030-01-01T15:00:00")); // Same day
        when(ptRepo.findByMemberMemberId(1L)).thenReturn(Collections.singletonList(existing));
        
        assertThrows(BusinessRuleException.class, () -> ptSessionService.requestSession(mockDto));
    }

    @Test
    void updateStatus_ToApproved_DeductsCredit() {
        when(ptRepo.findById(100L)).thenReturn(Optional.of(mockSession));
        when(ptRepo.save(any())).thenReturn(mockSession);
        when(mapper.map(mockSession, PtSessionDTO.class)).thenReturn(mockDto);

        ptSessionService.updateStatus(100L, PtSession.Status.APPROVED, "Looking forward to it!");

        assertEquals(4, mockMember.getPtSessionCredits());
        verify(memberRepo).save(mockMember);
        assertEquals(PtSession.Status.APPROVED, mockSession.getStatus());
        assertEquals("Looking forward to it!", mockSession.getTrainerNotes());
    }

    @Test
    void updateStatus_ToCancelled_RestoresCredit() {
        mockSession.setStatus(PtSession.Status.APPROVED);
        when(ptRepo.findById(100L)).thenReturn(Optional.of(mockSession));
        when(ptRepo.save(any())).thenReturn(mockSession);
        when(mapper.map(mockSession, PtSessionDTO.class)).thenReturn(mockDto);

        ptSessionService.updateStatus(100L, PtSession.Status.CANCELLED, "Sorry");

        assertEquals(6, mockMember.getPtSessionCredits());
        verify(memberRepo).save(mockMember);
        assertEquals(PtSession.Status.CANCELLED, mockSession.getStatus());
    }

    @Test
    void rescheduleSession_Success() {
        mockSession.setStatus(PtSession.Status.REQUESTED);
        when(ptRepo.findById(100L)).thenReturn(Optional.of(mockSession));
        when(ptRepo.findOverlappingForTrainer(eq(10L), any(), any())).thenReturn(Collections.emptyList());
        when(ptRepo.save(any())).thenReturn(mockSession);
        when(mapper.map(mockSession, PtSessionDTO.class)).thenReturn(mockDto);

        PtSessionDTO result = ptSessionService.rescheduleSession(100L, "2030-01-02T10:00:00");

        assertNotNull(result);
        assertEquals(LocalDateTime.parse("2030-01-02T10:00:00"), mockSession.getScheduledAt());
    }

    @Test
    void rescheduleSession_Within24Hours_ThrowsException() {
        mockSession.setStatus(PtSession.Status.ACCEPTED);
        mockSession.setScheduledAt(LocalDateTime.now().plusHours(10));
        when(ptRepo.findById(100L)).thenReturn(Optional.of(mockSession));

        assertThrows(BusinessRuleException.class, () -> ptSessionService.rescheduleSession(100L, "2030-01-02T10:00:00"));
    }

    @Test
    void cancelSession_Success() {
        mockSession.setStatus(PtSession.Status.REQUESTED);
        when(ptRepo.findById(100L)).thenReturn(Optional.of(mockSession));
        when(ptRepo.save(any())).thenReturn(mockSession);
        when(mapper.map(mockSession, PtSessionDTO.class)).thenReturn(mockDto);

        PtSessionDTO result = ptSessionService.cancelSession(100L);

        assertNotNull(result);
        assertEquals(PtSession.Status.CANCELLED, mockSession.getStatus());
        // Credits not restored since it wasn't approved/accepted
        assertEquals(5, mockMember.getPtSessionCredits());
    }

    @Test
    void getSessionsByMember_Success() {
        when(ptRepo.findByMemberMemberId(1L)).thenReturn(Collections.singletonList(mockSession));
        when(mapper.map(mockSession, PtSessionDTO.class)).thenReturn(mockDto);
        
        List<PtSessionDTO> result = ptSessionService.getSessionsByMember(1L);
        
        assertEquals(1, result.size());
    }
    
    @Test
    void getSessionsByTrainer_Success() {
        when(ptRepo.findByTrainerTrainerId(10L)).thenReturn(Collections.singletonList(mockSession));
        when(mapper.map(mockSession, PtSessionDTO.class)).thenReturn(mockDto);
        
        List<PtSessionDTO> result = ptSessionService.getSessionsByTrainer(10L);
        
        assertEquals(1, result.size());
    }
}
