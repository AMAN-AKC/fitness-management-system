package com.fitness.controller;

import com.fitness.dto.NotificationDTO;
import com.fitness.entity.NotificationPreference;
import com.fitness.service.NotificationService;
import com.fitness.service.PreferenceManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @MockBean
    private com.fitness.config.JwtConfig jwtConfig;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private NotificationService notifService;

    @MockBean
    private PreferenceManager preferenceManager;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void getNotifications_Success() throws Exception {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotifId(1L);

        when(notifService.getNotificationsForUser(1L)).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/notifications/user/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notifId").value(1));
    }

    @Test
    @WithMockUser
    void markAsRead_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/1/read")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(notifService).markAsRead(1L);
    }

    @Test
    @WithMockUser
    void countUnread_Success() throws Exception {
        when(notifService.countUnread(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications/user/1/unread-count")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    @WithMockUser
    void getPreferences_Success() throws Exception {
        NotificationPreference pref = new NotificationPreference();
        pref.setEmailEnabled(true);

        when(preferenceManager.getPreferences(1L)).thenReturn(pref);

        mockMvc.perform(get("/api/v1/notifications/user/1/preferences")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailEnabled").value(true));
    }
}
