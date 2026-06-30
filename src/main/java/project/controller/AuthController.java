package project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.dto.LoginResponseDto;
import project.dto.UserCreateRequest;
import project.dto.UserLoginRequest;
import project.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")

//Контроллер чтобы регистрация и логин работали
//проверить хешированный и декодированный пароль

public class AuthController {
    private final AuthService authService;


    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        return authService.login(userLoginRequest);
    }

    @PostMapping("/register")
    public LoginResponseDto register(@Valid  @RequestBody UserCreateRequest userCreateRequest) {
        return authService.register(userCreateRequest);
    }
}

