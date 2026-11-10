package hexlet.code.app.component;

import hexlet.code.app.exceptions.ResourceNotFoundException;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        return userRepository.findByEmail(email).
                orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public boolean isSelf(long id) {
        var currentUser = getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        if (currentUser.getEmail().equals("hexlet@example.com")) {
            return true;
        }

        return currentUser.getId().equals(id);
    }
}
