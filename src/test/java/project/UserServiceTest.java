package project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.dto.UserResponseDto;
import project.enums.Role;
import project.exception.NotFoundException;
import project.exception.ValidationException;
import project.model.User;
import project.service.UserService;
import project.storage.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private User user() {
        return User.builder()
                .id(3L)
                .name("Taylor")
                .last_name("Jordan")
                .email("taylor@example.com")
                .pass("encoded")
                .role(Role.CLIENT)
                .balance(10)
                .build();
    }

    @Test
    void listUsersMapsPublicResponseFields() {
        when(userRepository.findAll()).thenReturn(List.of(user()));

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).containsExactly(new UserResponseDto("Taylor", "Jordan", "taylor@example.com", Role.CLIENT));
    }

    @Test
    void getUserMapsExistingUser() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user()));

        assertThat(userService.getUserById(3L)).isEqualTo(
                new UserResponseDto("Taylor", "Jordan", "taylor@example.com", Role.CLIENT));
    }

    @Test
    void getUserRejectsMissingId() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(3L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User with id=3 not found");
    }

    @Test
    void updateUserSavesExistingUser() {
        User user = user();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        assertThat(userService.updateUser(user).email()).isEqualTo("taylor@example.com");
    }

    @Test
    void updateUserRejectsMissingUser() {
        User user = user();
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(user))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
        verify(userRepository, never()).save(user);
    }

    @Test
    void findUserByEmailMapsExistingUser() {
        when(userRepository.findByEmail("taylor@example.com")).thenReturn(Optional.of(user()));

        assertThat(userService.findUserByEmail("taylor@example.com").role()).isEqualTo(Role.CLIENT);
    }

    @Test
    void findUserByEmailRejectsMissingEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByEmail("missing@example.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with email=missing@example.com not found");
    }

    @Test
    void deleteUserDeletesExistingRecord() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(user()));

        userService.deleteUserById(3L);

        verify(userRepository).deleteById(3L);
    }

    @Test
    void deleteUserRejectsMissingRecord() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(3L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User with id=3 not found");
        verify(userRepository, never()).deleteById(3L);
    }
}
