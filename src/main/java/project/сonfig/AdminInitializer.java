package project.сonfig;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import project.enums.Role;
import project.exception.ValidationException;
import project.model.User;
import project.storage.user.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private String email;
    private String password;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${ADMIN_EMAIL}") String email,
                            @Value("${ADMIN_PASSWORD}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }
    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail(email)) {

            User admin = User.builder()
                    .name("Сергей")
                    .last_name("N")
                    .email(email)
                    .pass(passwordEncoder.encode(password))
                    .role(Role.ADMINISTRATOR)
                    .build();
            userRepository.save(admin);
        }
    }
}


