package project.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
    private final String secret = "aEkxNVkA1xuz•••••••••••••••••••4NNIjrpgHb4K";
    final Duration accessTokenTtl = Duration.ofDays(14);

     public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(accessTokenTtl)))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
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