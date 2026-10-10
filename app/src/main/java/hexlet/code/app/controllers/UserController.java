package hexlet.code.app.controllers;

import hexlet.code.app.dto.users.UserCreateDTO;
import hexlet.code.app.dto.users.UserDTO;
import hexlet.code.app.dto.users.UserUpdateDTO;
import hexlet.code.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/users/{id}")
    public UserDTO show(@PathVariable Long id) {
        return userService.showUser(id);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO dataDTO) {
        return userService.createUser(dataDTO);
    }

    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@Valid @RequestBody UserUpdateDTO dataDTO, @PathVariable Long id) {
        return userService.updateUser(dataDTO, id);
    }

    @PatchMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO patchUpdate(@Valid @RequestBody UserUpdateDTO dataDTO, @PathVariable Long id) {
        return userService.updateUser(dataDTO, id);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
