package project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import project.dto.UserCreateRequestDto;

import project.dto.UserResponseDto;
import project.enums.Role;
import project.exception.NotFoundException;
import project.exception.ValidationException;

import project.model.User;
import project.repository.user.UserRepository;

import java.util.List;

//@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //написать для метода фильтер аутеиндентификации

    /**
     * Получения списка пользователей
     * @return список пользователей преобразованных в DTO
     */
    public List<UserResponseDto> getAllUsers() {
        List<UserResponseDto> lst = userRepository.findAll()
                .stream()
                .map(this::toUserResponseDto)
                .toList();
        log.debug("lst size is:{}", lst.size());

        return lst;
    }

    //Pageable1
    /**
     * Возвращает страницу пользователей с учётом пагинации и сортировки.
     * @param pageable параметры страницы, размера и сортировки
     * @return страница пользователей в формате DTO ответа
     */
    public Page<UserResponseDto> getUsers(Pageable pageable) {
        Page<UserResponseDto> users = userRepository.findAll(pageable)
                .map(this::toUserResponseDto);

        log.debug("Получена страница пользователей: page={}, size={}, totalElements={}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                users.getTotalElements());

        return users;
    }

    //Pageable2

    /* <- delete
    /**
     * Возвращает страницу пользователей с учётом пагинации и сортировки.
     * @param page номер страницы, начиная с 0
     * @param size количество пользователей на странице
     * @return страница пользователей в формате DTO ответа
     */

    /* <- delete
    public Page<UserResponseDto> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(this::toUserResponseDto);
    }
    delete ->*/


    /**
     * Обновляет данные пользователя
     * @param user запись с обновленными данными
     * @return обноленная запись в формате DTO
     */
    public UserResponseDto updateUser(User user) {
        userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        User savedUser = userRepository.save(user);

        log.info("Данные пользователя обновлены: userId={}", savedUser.getId());
        return toUserResponseDto(savedUser);
    }

    /**
     * Поиск пользователя по идентификатору
     * @param id идентификатор пользователя
     * @return получение результата поиска
     */
    public UserResponseDto getUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("User with id=" + id + " not found"));

        log.info("Пользователь найден:{}",user.getId());
        return toUserResponseDto(user);
    }

    /**
     * Удаление записи по идентификатору
     * @param id идентификатор записи
     */
    public void deleteUserById(long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " not found"));

        userRepository.deleteById(id);
        log.info("Пользователь удалён: userId={}", id);
    }

    /**
     * Поиск пользователя по email
     * @param email параметр поиска
     * @return получение результата поиска
     */
    public UserResponseDto findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email=" + email + " not found"));

        log.info("Пользователь найден id:{}, email{}", user.getId(), user.getEmail());
        return toUserResponseDto(user);
    }

    /**
     * Преобразование userCreateRequest в пользователя
     * @param userCreateRequest объект преобразования в User
     * @return преобразованние в User
     */
    private User toMaptoUser(UserCreateRequestDto userCreateRequest) {
        return new User(userCreateRequest.name(),
                userCreateRequest.lastName(),
                userCreateRequest.email(),
                userCreateRequest.pass(),
                Role.CLIENT);
    }

    /**
     * Преобразование пользователя в DTO ответ
     * @param user запись преобразования в DTO
     * @return DTO ответа с данными записи
     */
    private UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(user.getName(),
                user.getLast_name(),
                user.getEmail(),
                user.getRole());
    }

}
