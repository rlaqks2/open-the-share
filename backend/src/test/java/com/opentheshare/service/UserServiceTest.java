package com.opentheshare.service;

import com.opentheshare.dto.UserRegistrationDto;
import com.opentheshare.entity.User;
import com.opentheshare.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Registration succeeds with new email")
    void register_Success() {
        // Given
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setName("Test User");

        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(dto.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        User result = userService.register(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getRole()).isEqualTo(User.Role.USER);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Registration fails when email already exists")
    void register_Failure_DuplicateEmail() {
        // Given
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("existing@example.com");
        dto.setPassword("password");
        dto.setName("Test User");

        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.of(new User()));

        // When & Then
        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Authentication succeeds with correct credentials")
    void authenticate_Success() {
        // Given
        String email = "test@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);

        // When
        User result = userService.authenticate(email, password);

        // Then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("Authentication fails with wrong password")
    void authenticate_Failure_WrongPassword() {
        // Given
        String email = "test@example.com";
        String password = "wrongPassword";
        String encodedPassword = "encodedPassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, encodedPassword)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.authenticate(email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("Authentication fails with non-existent email")
    void authenticate_Failure_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password";

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.authenticate(email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email or password");
    }
}
