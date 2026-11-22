package project.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import project.model.User;
import project.security.user.CustomUserDetails;
import project.service.JwtService;
import project.repository.user.UserRepository;

import java.io.IOException;

/**
 * Проверяет JWT из заголовка Authorization и устанавливает аутентификацию
 * текущего пользователя в {@link SecurityContextHolder}.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Обрабатывает запрос: проверяет Bearer-токен, загружает пользователя
     * и устанавливает его данные в контекст безопасности.
     * Если токена нет, передаёт запрос дальше без аутентификации.
     * При недействительном токене или отсутствии пользователя возвращает 401.
     *
     * @param request входящий HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException если обработка запроса прерывается ошибкой сервлета
     * @throws IOException если происходит ошибка ввода-вывода
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //вытащить id из токена
        Long idToken = jwtService.getUserIdFromToken(token);


        //проверить пользователя по данному id
        User user = userRepository.findById(idToken).orElse(null);
        if (user == null) {
            log.debug("Запрос отклонён: пользователь из JWT не найден, userId={}, path={}",
                    idToken, request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        //создать userDitails
        CustomUserDetails userDetails = new CustomUserDetails(user);

        //UserDetails user = userDetailsService.loadUserByUsername(username);
        //нужнно ли использовать loadUserByUsername() или нет

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        //заполнить security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
