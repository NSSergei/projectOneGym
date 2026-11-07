
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import project.enums.Role;
import project.model.User;
import project.service.JwtService;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.mock;

public class JwtServiceTest {
    private static final String SECRET = "unit-test-secret-key-that-is-at-least-32-bytes-long";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
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

    @Test
    void generatedTokenIsValidAndReturnsUserId() {
        String token = jwtService.generateToken(user(42L, Role.CLIENT));

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void invalidTokenIsRejectedAndMapsToNoUserId() {
        assertThat(jwtService.validateToken("not-a-jwt")).isFalse();
        assertThat(jwtService.getUserIdFromToken("not-a-jwt")).isZero();
    }

    @Test
    void expiredTokenIsRejected() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("42")
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(key)
                .compact();

        assertThat(jwtService.validateToken(token)).isFalse();
    }

}
