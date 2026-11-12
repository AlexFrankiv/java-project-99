package hexlet.code.app.controllers;

import hexlet.code.app.dto.users.UserCreateDTO;
import hexlet.code.app.dto.users.UserDTO;
import hexlet.code.app.dto.users.UserUpdateDTO;
import hexlet.code.app.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userServiceImpl;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        var users = userServiceImpl.getAll();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.size()))
                .body(users);

    }

    @GetMapping("/users/{id}")
    public UserDTO show(@PathVariable Long id) {
        return userServiceImpl.showUser(id);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO dataDTO) {
        return userServiceImpl.createUser(dataDTO);
    }

    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userUtils.isSelf(#id)")
    public UserDTO update(@Valid @RequestBody UserUpdateDTO dataDTO, @PathVariable Long id) {
        return userServiceImpl.updateUser(dataDTO, id);
    }

    @PatchMapping("/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@userUtils.isSelf(#id)")
    public UserDTO patchUpdate(@Valid @RequestBody UserUpdateDTO dataDTO, @PathVariable Long id) {
        return userServiceImpl.updateUser(dataDTO, id);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@userUtils.isSelf(#id)")
    public void destroy(@PathVariable Long id) {
        userServiceImpl.deleteUser(id);
    }
}
