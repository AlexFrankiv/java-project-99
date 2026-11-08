package hexlet.code.app.commponent;

import hexlet.code.app.model.Label;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initAdmin();
        initTaskStatuses();
        initLabels();
    }

    private void initAdmin() {
        String email = "hexlet@example.com";

        var existingAdmin = userRepository.findByEmail(email);

        if (existingAdmin.isPresent()) {
            var admin = existingAdmin.get();
            admin.setPassword(passwordEncoder.encode("qwerty"));
            userRepository.save(admin);
            log.info("Пароль администратора {} обновлен", email);
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setFirstName("Admin");
        admin.setLastName("Adminov");
        admin.setPassword(passwordEncoder.encode("qwerty"));
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

    private void initLabels() {
        createLabelIfNotExists("feature");
        createLabelIfNotExists("bug");
    }

    private void createLabelIfNotExists(String name) {
        if (labelRepository.findByName(name).isEmpty()) {
            Label label = new Label();
            label.setName(name);
            labelRepository.save(label);
        }
    }
}
