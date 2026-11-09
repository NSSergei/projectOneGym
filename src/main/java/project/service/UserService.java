package project.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import project.dto.UserCreateRequest;

import project.dto.UserResponseDto;
import project.enums.Role;
import project.exception.NotFoundException;
import project.exception.ValidationException;

import project.model.User;
import project.storage.user.UserRepository;

import java.util.List;

//@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //написать для метода фильтер аутеиндентификации
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponseDto)
                .toList();
    }

    //Pageable1
    public Page<UserResponseDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toUserResponseDto);
    }

    //Pageable2
    /*public Page<UserResponseDto> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(this::toUserResponseDto);
    }*/

    public UserResponseDto updateUser(User user) {
        userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        User savedUser = userRepository.save(user);

        return toUserResponseDto(savedUser);
    }

    public UserResponseDto getUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("User with id=" + id + " not found"));

        return toUserResponseDto(user);
    }

    public void deleteUserById(long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " not found"));

        userRepository.deleteById(id);
    }

    public UserResponseDto findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email=" + email + " not found"));

        return toUserResponseDto(user);
    }

    private User toMaptoUser(UserCreateRequest userCreateRequest) {
        return new User(userCreateRequest.name(),
                userCreateRequest.last_name(),
                userCreateRequest.email(),
                userCreateRequest.pass(),
                Role.CLIENT);
    }

    private UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(user.getName(),
                user.getLast_name(),
                user.getEmail(),
                user.getRole());
    }

}
