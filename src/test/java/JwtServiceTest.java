
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import project.enums.Role;
import project.model.User;
import project.service.JwtService;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.mock;

public class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("unit-test-secret-key-that-is-at-least-32-bytes-long");
    }

    private User user (Long id, Role role) {
        User user = mock(User.class);

        when(user.getId()).thenReturn(id);

        when(user.getRole()).thenReturn(role);

        return user;
    }

    @Test
    void generateToken_ContainsSubjectAndRole() {
        String token = jwtService.generateToken(user(42L,Role.ADMINISTRATOR));

        Claims claims = jwtService.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("role",String.class)).isEqualTo("ADMINISTRATOR");
    }

}
