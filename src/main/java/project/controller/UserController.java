package project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.dto.UserCreateRequest;
import project.dto.UserResponseDto;
import project.model.User;
import project.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponseDto> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/email/{email}")
    public UserResponseDto getUserByEmail(@PathVariable String email) {
        return userService.findUserByEmail(email);
    }

    @PutMapping
    public UserResponseDto updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable long id) {
        userService.deleteUserById(id);
    }

}
