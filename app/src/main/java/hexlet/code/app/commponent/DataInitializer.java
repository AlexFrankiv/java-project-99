package hexlet.code.app.commponent;

import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initAdmin();
        initTaskStatuses();
    }

    private void initAdmin() {
        String email = "hexlet@example.com";

        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Администратор {} уже существует", email);
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode("password"));
        userRepository.save(admin);

        log.info("Администратор {} создан", email);
    }

    private void initTaskStatuses() {
        createStatusIfNotExists("draft", "Draft");
        createStatusIfNotExists("to_review", "To Review");
        createStatusIfNotExists("to_be_fixed", "To Be Fixed");
        createStatusIfNotExists("to_publish", "To Publish");
        createStatusIfNotExists("published", "Published");
    }

    private void createStatusIfNotExists(String slug, String name) {
        if (taskStatusRepository.findBySlug(slug).isEmpty()) {
            var status = new TaskStatus();
            status.setSlug(slug);
            status.setName(name);
            taskStatusRepository.save(status);
        }
    }
}
