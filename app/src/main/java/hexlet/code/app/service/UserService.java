package hexlet.code.app.service;

import hexlet.code.app.dto.users.UserCreateDTO;
import hexlet.code.app.dto.users.UserDTO;
import hexlet.code.app.dto.users.UserUpdateDTO;
import hexlet.code.app.exceptions.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public List<UserDTO> getAll() {
        var result = userRepository.findAll().stream().map(userMapper::map).toList();

        return result;
    }

    public UserDTO showUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id - " + id + " not found!"));
        var userDTO = userMapper.map(user);

        return userDTO;
    }

    public UserDTO createUser(UserCreateDTO dataDTO) {
        var user = userMapper.map(dataDTO);
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(user.getPassword(), salt);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        var userDTO = userMapper.map(user);

        return userDTO;
    }

    public UserDTO updateUser(UserUpdateDTO dataDTO, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id - " + id + " not found!"));
        userMapper.update(dataDTO, user);
        userRepository.save(user);
        var userDTO = userMapper.map(user);

        return userDTO;

    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
