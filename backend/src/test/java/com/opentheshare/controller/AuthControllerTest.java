package com.opentheshare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentheshare.config.SecurityConfig;
import com.opentheshare.dto.LoginRequestDto;
import com.opentheshare.dto.UserRegistrationDto;
import com.opentheshare.entity.User;
import com.opentheshare.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Signup returns 200 and user data on success")
    void signup_Success() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(User.Role.USER);

        given(userService.register(any(UserRegistrationDto.class))).willReturn(user);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).register(any(UserRegistrationDto.class));
    }

    @Test
    @DisplayName("Login returns 200 and user data on success")
    void login_Success() throws Exception {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail(dto.getEmail());
        user.setRole(User.Role.USER);

        given(userService.authenticate(anyString(), anyString())).willReturn(user);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).authenticate(dto.getEmail(), dto.getPassword());
    }

    @Test
    @DisplayName("Signup returns 400 when service throws exception")
    void signup_Failure() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("existing@example.com");
        dto.setPassword("password");

        given(userService.register(any(UserRegistrationDto.class)))
                .willThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }
}
