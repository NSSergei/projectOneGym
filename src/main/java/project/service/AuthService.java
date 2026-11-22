package project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.dto.LoginResponseDto;
import project.dto.UserCreateRequestDto;
import project.dto.UserLoginRequestDto;
import project.enums.Role;
import project.exception.ValidationException;
import project.model.User;
import project.repository.user.UserRepository;

//Контроллер чтобы регистрация и логин работали
//проверить хешированный и пришедший пароль

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
    * @param    userCreateRequest  параметры для регистрации пользователя
     * @return  результат добавление пользователя и полученеи JWT токена
     */
    public LoginResponseDto register(UserCreateRequestDto userCreateRequest) {
        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new ValidationException("invalide email or password");
        }

        User user = User.builder()
                .name(userCreateRequest.name())
                .last_name(userCreateRequest.lastName())
                .email(userCreateRequest.email())
                .pass(passwordEncoder.encode(userCreateRequest.pass()))
                .role(Role.CLIENT)
                .balance(0)
                .build();

        log.debug("Начата регистрация пользователя");
        user = userRepository.save(user);
        log.info("Регистрация завершена: userId={}", user.getId());

        String token = jwtService.generateToken(user);

        return new LoginResponseDto(token, "Jwt");
    }
    /**
    * @param    userLoginRequest параметры для Аутентификации
     * @return результат генерация и получение Jwt токена
     * */
    public LoginResponseDto login(UserLoginRequestDto userLoginRequest) {
        User user = userRepository.findByEmail(userLoginRequest.email())
                    .orElseThrow(() -> new ValidationException ("invalide email or password"));

        if (!passwordEncoder.matches(userLoginRequest.pass(), user.getPass())) {
            throw new ValidationException("invalide email or password");
        }

        String token = jwtService.generateToken(user);

        log.info("Успешный вход: userId={}", user.getId());

        return new LoginResponseDto(token, "Jwt");
    }
}
