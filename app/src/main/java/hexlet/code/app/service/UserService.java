package hexlet.code.app.service;

import hexlet.code.app.dto.users.UserCreateDTO;
import hexlet.code.app.dto.users.UserDTO;
import hexlet.code.app.dto.users.UserUpdateDTO;
import hexlet.code.app.exceptions.ResourceInUseException;
import hexlet.code.app.exceptions.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

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
        userRepository.deleteById(id);
    }
}
