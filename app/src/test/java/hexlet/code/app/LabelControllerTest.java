package hexlet.code.app;

import hexlet.code.app.dto.AuthRequest;
import hexlet.code.app.dto.labels.LabelCreateDTO;
import hexlet.code.app.dto.labels.LabelUpdateDTO;
import hexlet.code.app.dto.tasks.TaskCreateDTO;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LabelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private Long adminId;

    @BeforeEach
    void setUp() throws Exception {
        var loginRequest = new AuthRequest();
        loginRequest.setUsername("hexlet@example.com");
        loginRequest.setPassword("password");

        var response = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        adminToken = response.replace("\"", "");
        adminId = userRepository.findByEmail("hexlet@example.com").get().getId();
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllLabels() throws Exception {
        mockMvc.perform(get("/api/labels")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[?(@.name == 'feature')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'bug')]").exists());
    }

    @Test
    void testGetLabelById() throws Exception {
        var bugLabel = labelRepository.findByName("bug").orElseThrow();

        mockMvc.perform(get("/api/labels/" + bugLabel.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bugLabel.getId()))
                .andExpect(jsonPath("$.name").value("bug"));
    }

    @Test
    void testGetLabelByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/labels/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateLabel() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName("test_create_label");

        mockMvc.perform(post("/api/labels")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test_create_label"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertThat(labelRepository.findByName("test_create_label")).isPresent();
    }

    @Test
    void testCreateLabelInvalidData() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName("ab");

        mockMvc.perform(post("/api/labels")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateDuplicateLabel() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName("bug");

        mockMvc.perform(post("/api/labels")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateLabel() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName("test_update_label");

        var result = mockMvc.perform(post("/api/labels")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andReturn();
        var labelId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        var updateDto = new LabelUpdateDTO();
        updateDto.setName("updated_label_name");

        mockMvc.perform(put("/api/labels/" + labelId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated_label_name"));
    }

    @Test
    void testDeleteLabel() throws Exception {
        var dto = new LabelCreateDTO();
        dto.setName("test_delete_label");

        var result = mockMvc.perform(post("/api/labels")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andReturn();
        var labelId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/labels/" + labelId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(labelId)).isEmpty();
    }

    @Test
    void testCannotDeleteLabelUsedByTask() throws Exception {
        var bugLabel = labelRepository.findByName("bug").orElseThrow();

        var taskDto = new TaskCreateDTO();
        taskDto.setTitle("Task using bug label");
        taskDto.setStatus("draft");
        taskDto.setAssigneeId(adminId);
        taskDto.setTaskLabelIds(Set.of(bugLabel.getId()));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(taskDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/labels/" + bugLabel.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict()); // 409 Conflict

        assertThat(labelRepository.findById(bugLabel.getId())).isPresent();
    }
}
