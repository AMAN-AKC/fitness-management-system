package com.fitness.service;

import com.fitness.dto.NotificationDTO;
import com.fitness.entity.Notification;
import com.fitness.entity.SystemUser;
import com.fitness.repository.NotificationRepository;
import com.fitness.repository.SystemUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notifRepo;

    @Mock
    private SystemUserRepository userRepo;

    @Mock
    private ModelMapper mapper;

    @Test
    void sendNotification_Success() {
        SystemUser user = new SystemUser();
        user.setUserId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        
        Notification notification = new Notification();
        when(notifRepo.save(any(Notification.class))).thenReturn(notification);
        
        NotificationDTO dto = new NotificationDTO();
        when(mapper.map(any(), eq(NotificationDTO.class))).thenReturn(dto);

        NotificationDTO result = notificationService.sendNotification(1L, Notification.NotifType.GENERAL, Notification.Channel.IN_APP, "Title", "Body");

        assertNotNull(result);
        verify(notifRepo).save(any(Notification.class));
    }

    @Test
    void markAsRead_Success() {
        Notification notification = new Notification();
        notification.setNotifId(1L);
        notification.setIsRead(false);

        when(notifRepo.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertTrue(notification.getIsRead());
        verify(notifRepo).save(notification);
    }

    @Test
    void countUnread_Success() {
        when(notifRepo.findByUserUserIdAndIsReadFalse(1L)).thenReturn(List.of(new Notification(), new Notification()));

        long count = notificationService.countUnread(1L);

        assertEquals(2, count);
    }
}
