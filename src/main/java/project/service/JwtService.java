package project.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import project.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

//Добавить дополнительные поле
//Роль

@Service
public class JwtService {
    //@Value("${jwt.secret}") String secret;
    private final SecretKey key;
    final Duration accessTokenTtl = Duration.ofDays(14);

    public JwtService(@Value("${JWT_SECRET}") String secret) {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /*SecretKey key = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
    );*/


     public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("role",user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(accessTokenTtl)))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        if (!validateToken(token)) {
            return 0L;
        }
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public Claims parseClaims(String token) {
         return Jwts.parser()
                 .setSigningKey(key)
                 .verifyWith(key)
                 .build()
                 .parseSignedClaims(token)
                 .getPayload();
    }
}

/*У тебя:
.signWith(SignatureAlgorithm.HS256, secret)

Это старый API JJWT.
В новых версиях обычно делают:

SecretKey key = Keys.hmacShaKeyFor(
        secret.getBytes(StandardCharsets.UTF_8)
);

.signWith(key)
*/