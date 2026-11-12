package hexlet.code.app.service.interfaces;

import hexlet.code.app.dto.users.UserCreateDTO;
import hexlet.code.app.dto.users.UserDTO;
import hexlet.code.app.dto.users.UserUpdateDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAll();
    UserDTO showUser(Long id);
    UserDTO createUser(UserCreateDTO dataDTO);
    UserDTO updateUser(UserUpdateDTO dataDTO, Long id);
    void deleteUser(Long id);
}
