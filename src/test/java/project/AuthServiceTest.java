package project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.dto.UserCreateRequest;
import project.enums.Role;
import project.exception.ValidationException;
import project.model.User;
import project.service.AuthService;
import project.service.JwtService;
import project.storage.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    @Test
    void registerStoresEncodedPasswordAndReturnsToken() {
        UserCreateRequest request = new UserCreateRequest("Test", "User", "test@example.com", "plain-password");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.pass())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(12L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("signed-token");

        var response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("signed-token");
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getPass().equals("encoded-password") && user.getRole() == Role.CLIENT));
    }

    @Test
    void registerRejectsAnExistingEmail() {
        UserCreateRequest request = new UserCreateRequest("Test", "User", "test@example.com", "plain-password");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ValidationException.class);
    }
}
