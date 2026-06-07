package project.service;

import org.springframework.stereotype.Service;
import project.dto.UserCreateRequest;
import project.exception.ValidationException;
import project.model.User;
import project.storage.user.UserRepository;

import java.util.List;
import java.util.Optional;

//@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User creatUser(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new ValidationException("Email есть в базе данных");
        }

        User response = userRepository.save(toMaptoUser(userCreateRequest));
        return response;
    }

    private User toMaptoUser(UserCreateRequest userCreateRequest) {
        return new User(userCreateRequest.name(), userCreateRequest.last_name(),
                userCreateRequest.email(),userCreateRequest.pass(), userCreateRequest.role());
    }

    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

}
