package project.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.dto.LoginResponseDto;
import project.dto.UserCreateRequest;
import project.dto.UserLoginRequest;
import project.enums.Role;
import project.exception.ValidationException;
import project.model.User;
import project.storage.user.UserRepository;

//Контроллер чтобы регистрация и логин работали
//проверить хешированный и пришедший пароль

@Service
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

    public LoginResponseDto register(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new ValidationException("invalide email or password");
        }

        User user = User.builder()
                .name(userCreateRequest.name())
                .last_name(userCreateRequest.last_name())
                .email(userCreateRequest.email())
                .pass(passwordEncoder.encode(userCreateRequest.pass()))
                .role(Role.CLIENT)
                .balance(0)
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new LoginResponseDto(token, "Jwt");
    }

    public LoginResponseDto login(UserLoginRequest userLoginRequest) {
        System.out.println("LOGIN START");

        User user = userRepository.findByEmail(userLoginRequest.email())
                    .orElseThrow(() -> new ValidationException ("invalide email or password"));

        if (!passwordEncoder.matches(userLoginRequest.pass(), user.getPass())) {
            throw new ValidationException("invalide email or password");
        }

        System.out.println("PASSWORD OK");

        String token = jwtService.generateToken(user);

        return new LoginResponseDto(token, "Jwt");
    }
}
