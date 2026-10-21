package hexlet.code.app;

import hexlet.code.app.dto.AuthRequest;
import hexlet.code.app.dto.users.UserCreateDTO;
import hexlet.code.app.dto.users.UserUpdateDTO;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class AppApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;

    @BeforeEach
    void setUp() throws Exception {
        var loginRequest = new AuthRequest();
        loginRequest.setUsername("hexlet@example.com");
        loginRequest.setPassword("password");

        var response = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        adminToken = response.replace("\"", "");
        assertThat(adminToken).isNotBlank();
    }

    @Test
    void testLoginSuccess() throws Exception {
        var loginRequest = new AuthRequest();
        loginRequest.setUsername("hexlet@example.com");
        loginRequest.setPassword("password");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.emptyString())));
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        var loginRequest = new AuthRequest();
        loginRequest.setUsername("hexlet@example.com");
        loginRequest.setPassword("wrong-password");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllUsersWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateUserWithoutToken() throws Exception {
        var dto = new UserCreateDTO();
        dto.setEmail("noauth@google.com");
        dto.setFirstName("No");
        dto.setLastName("Auth");
        dto.setPassword("password");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetAllUsersWithToken() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateUserWithToken() throws Exception {
        var dto = new UserCreateDTO();
        dto.setEmail("alex@google.com");
        dto.setFirstName("Alex");
        dto.setLastName("Ivanov");
        dto.setPassword("some-password");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated()) // 201
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("alex@google.com"))
                .andExpect(jsonPath("$.firstName").value("Alex"))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertThat(userRepository.findByEmail("alex@google.com")).isPresent();
    }

    @Test
    void testUpdateUserWithToken() throws Exception {
        var createDto = new UserCreateDTO();
        createDto.setEmail("update@google.com");
        createDto.setPassword("password");

        var createResult = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        var responseJson = createResult.getResponse().getContentAsString();
        var userId = om.readTree(responseJson).get("id").asLong();

        var updateDto = new UserUpdateDTO();
        updateDto.setEmail(JsonNullable.of("update.new@google.com"));
        updateDto.setPassword(JsonNullable.of("new-password"));

        mockMvc.perform(put("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("update.new@google.com"));
    }

    @Test
    void testDeleteUserWithToken() throws Exception {
        var createDto = new UserCreateDTO();
        createDto.setEmail("delete@google.com");
        createDto.setFirstName("To");
        createDto.setLastName("Delete");
        createDto.setPassword("password");

        var createResult = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        var userId = om.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findByEmail("delete@google.com")).isEmpty();
    }

    @Test
    void testGetUserByIdWithToken() throws Exception {
        var dto = new UserCreateDTO();
        dto.setEmail("getbyid@google.com");
        dto.setFirstName("Get");
        dto.setLastName("ById");
        dto.setPassword("password");

        var result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        var userId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("getbyid@google.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testGetUserByIdWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUserWithoutToken() throws Exception {
        var dto = new UserUpdateDTO();
        dto.setEmail(JsonNullable.of("update@google.com"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteUserWithoutToken() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginNonExistentUser() throws Exception {
        var req = new AuthRequest();
        req.setUsername("nobody@nowhere.com");
        req.setPassword("any-password");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateUserWithInvalidData() throws Exception {
        var dto = new UserCreateDTO();
        dto.setEmail("not-an-email");
        dto.setPassword("ab");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAdminCreatedAtStartup() {
        assertThat(userRepository.findByEmail("hexlet@example.com")).isPresent();
    }
}
