package project.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JwtService {
    private final SecretKey key;
    final Duration accessTokenTtl = Duration.ofDays(14);

    /**
     * Создание ключа для подписи JWT токенов
     * @param secret секрет, используемый для формирования ключа подписи
     */
    public JwtService(@Value("${JWT_SECRET}") String secret) {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Генерация JWT токена
     * @param user получение данных
     * @return возвращает JWT токн
     */
     public String generateToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role",user.getRole().name())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(accessTokenTtl)))
                .signWith(key)
                .compact();
    }

    /**
     *
     * @param token JWT токен
     * @return результат проверка переданного JWT токена
     */
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

    /**
     * Получени id
     * @param token JWT токен для поиска пользователя
     * @return результат поиска id пользователя
     */
    public Long getUserIdFromToken(String token) {
        if (!validateToken(token)) {
            return 0L;
        }
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * Проверяет подпись и извлекает claims из JWT-токена.
     * @param token JWT-токен для разбора
     * @return claims из payload токена
     * @throws JwtException если токен некорректен, просрочен или подпись недействительна
     */
    public Claims parseClaims(String token) {
         return Jwts.parser()
                 .verifyWith(key)
                 .build()
                 .parseSignedClaims(token)
                 .getPayload();
    }
}
