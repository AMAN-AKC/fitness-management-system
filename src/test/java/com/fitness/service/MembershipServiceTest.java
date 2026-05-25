package com.fitness.service;

import com.fitness.dto.MembershipDTO;
import com.fitness.entity.Branch;
import com.fitness.entity.Member;
import com.fitness.entity.Membership;
import com.fitness.entity.Plan;
import com.fitness.exception.BusinessRuleException;
import com.fitness.exception.ResourceNotFoundException;
import com.fitness.repository.BranchRepository;
import com.fitness.repository.MemberRepository;
import com.fitness.repository.MembershipRepository;
import com.fitness.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MembershipServiceTest {

    @InjectMocks
    private MembershipService membershipService;

    @Mock
    private MembershipRepository membershipRepo;

    @Mock
    private MemberRepository memberRepo;

    @Mock
    private PlanRepository planRepo;

    @Mock
    private BranchRepository branchRepo;

    @Mock
    private ModelMapper mapper;

    @Mock
    private ProratedPriceService proratedPriceService;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private AuditLogService auditLogService;

    @Test
    void createMembership_Success() {
        MembershipDTO dto = new MembershipDTO();
        dto.setMemberId(1L);
        dto.setPlanId(1L);
        dto.setBranchId(1L);

        Member member = new Member();
        member.setMemberId(1L);

        Plan plan = new Plan();
        plan.setPlanId(1L);
        plan.setIsActive(true);
        plan.setDurationDays(30);
        plan.setPrice(BigDecimal.TEN);

        Branch branch = new Branch();
        branch.setBranchId(1L);

        Membership membership = new Membership();
        membership.setMemId(1L);

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(planRepo.findById(1L)).thenReturn(Optional.of(plan));
        when(branchRepo.findById(1L)).thenReturn(Optional.of(branch));
        when(mapper.map(dto, Membership.class)).thenReturn(membership);
        when(membershipRepo.save(any(Membership.class))).thenReturn(membership);
        when(mapper.map(membership, MembershipDTO.class)).thenReturn(dto);

        MembershipDTO result = membershipService.createMembership(dto);

        assertNotNull(result);
        assertEquals(Membership.Status.PENDING, membership.getStatus());
        verify(invoiceService).createInvoice(any());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), any(), anyString());
    }

    @Test
    void createMembership_InactivePlan_ThrowsException() {
        MembershipDTO dto = new MembershipDTO();
        dto.setMemberId(1L);
        dto.setPlanId(1L);

        Member member = new Member();
        Plan plan = new Plan();
        plan.setIsActive(false);

        when(memberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(planRepo.findById(1L)).thenReturn(Optional.of(plan));

        assertThrows(BusinessRuleException.class, () -> membershipService.createMembership(dto));
    }

    @Test
    void getMembershipsByMember_Success() {
        Membership membership = new Membership();
        when(membershipRepo.findByMemberMemberId(1L)).thenReturn(List.of(membership));
        when(mapper.map(any(), eq(MembershipDTO.class))).thenReturn(new MembershipDTO());

        List<MembershipDTO> result = membershipService.getMembershipsByMember(1L);
        assertFalse(result.isEmpty());
    }

    @Test
    void suspendMembership_Success() {
        Membership membership = new Membership();
        membership.setMemId(1L);
        membership.setEndDate(LocalDate.now());
        Member member = new Member();
        membership.setMember(member);

        when(membershipRepo.findById(1L)).thenReturn(Optional.of(membership));
        when(membershipRepo.save(any())).thenReturn(membership);
        when(mapper.map(any(), eq(MembershipDTO.class))).thenReturn(new MembershipDTO());

        MembershipDTO result = membershipService.suspendMembership(1L, 2, "Vacation");
        
        assertEquals(Membership.Status.SUSPENDED, membership.getStatus());
        assertEquals(Member.Status.SUSPENDED, member.getStatus());
        verify(auditLogService).logForCurrentUser(anyString(), any(), any(), anyString(), anyString());
    }
}
