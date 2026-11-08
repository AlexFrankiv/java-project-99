package hexlet.code.app.service;

import hexlet.code.app.commponent.UserUtils;
import hexlet.code.app.dto.users.UserCreateDTO;
import hexlet.code.app.dto.users.UserDTO;
import hexlet.code.app.dto.users.UserUpdateDTO;
import hexlet.code.app.exceptions.ResourceInUseException;
import hexlet.code.app.exceptions.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserUtils userUtils;

    public List<UserDTO> getAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO showUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id - " + id + " not found!"));
        var userDTO = userMapper.map(user);

        return userDTO;
    }

    public UserDTO createUser(UserCreateDTO dataDTO) {
        var user = userMapper.map(dataDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        var userDTO = userMapper.map(user);

        return userDTO;
    }

    public UserDTO updateUser(UserUpdateDTO dataDTO, Long id) {
        var currentUser = userUtils.getCurrentUser();

        if (currentUser != null &&
                !currentUser.getEmail().equals("hexlet@example.com") &&
                !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("You can only edit your own profile");
        }

        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id - " + id + " not found!"));

        String oldPassword = user.getPassword();

        userMapper.update(dataDTO, user);

        if (dataDTO.getPassword() != null && dataDTO.getPassword().isPresent()
                && dataDTO.getPassword().get() != null && !dataDTO.getPassword().get().isBlank()) {
            user.setPassword(passwordEncoder.encode(dataDTO.getPassword().get()));
        } else {
            user.setPassword(oldPassword);
        }

        userRepository.save(user);
        return userMapper.map(user);

    }

    public void deleteUser(Long id) {
        var currentUser = userUtils.getCurrentUser();

        if (currentUser != null &&
                !currentUser.getEmail().equals("hexlet@example.com") &&
                !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("You can only delete your own profile");
        }

        if (taskRepository.existsByAssigneeId(id)) {
            throw new ResourceInUseException("Cannot delete user: user is assigned to tasks");
        }
        userRepository.deleteById(id);
    }
}
