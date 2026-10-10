package hexlet.code.app.commponent;

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String email = "hexlet@example.com";

        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Администратор {} уже существует", email);
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(admin.getPassword(), salt);

        userRepository.save(admin);
        log.info("Администратор создан", email);

    }
}
