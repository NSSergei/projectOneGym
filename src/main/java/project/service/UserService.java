package project.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import project.dto.UserCreateRequest;

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

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User creatUser(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new ValidationException("Email есть в базе данных");
        }

        User response = toMaptoUser(userCreateRequest);

        response.setPass(passwordEncoder.encode(response.getPass()));

        return userRepository.save(response);
    }

    public User updateUser(User user) {
        userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userRepository.save(user);
    }

    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("User with id=" + id + " not found"));
    }

    public void deleteUserById(long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " not found"));

        userRepository.deleteUserById(id);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email=" + email + " not found"));
    }

    private User toMaptoUser(UserCreateRequest userCreateRequest) {
        return new User(userCreateRequest.name(), userCreateRequest.last_name(),
                userCreateRequest.email(),userCreateRequest.pass(), Role.CLIENT);
    }
}
