package hexlet.code.app;

import hexlet.code.app.dto.AuthRequest;
import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.app.dto.tasks.TaskCreateDTO;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class TaskStatusControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

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
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllStatuses() throws Exception {
        mockMvc.perform(get("/api/task_statuses")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[?(@.slug == 'draft')]").exists())
                .andExpect(jsonPath("$[?(@.slug == 'to_review')]").exists());
    }

    @Test
    void testGetStatusById() throws Exception {
        var draft = taskStatusRepository.findBySlug("draft").orElseThrow();

        mockMvc.perform(get("/api/task_statuses/" + draft.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(draft.getId()))
                .andExpect(jsonPath("$.slug").value("draft"));
    }

    @Test
    void testGetStatusByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/task_statuses/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateStatus() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setName("Test Status");
        dto.setSlug("test_create_slug");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Status"))
                .andExpect(jsonPath("$.slug").value("test_create_slug"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertThat(taskStatusRepository.findBySlug("test_create_slug")).isPresent();
    }

    @Test
    void testCreateStatusInvalidData() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setName("");
        dto.setSlug("");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateDuplicateStatus() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setName("Another Draft");
        dto.setSlug("draft");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testUpdateStatus() throws Exception {
        var draft = taskStatusRepository.findBySlug("draft").orElseThrow();

        var dto = new TaskStatusUpdateDTO();
        dto.setName("Updated Draft Name");

        mockMvc.perform(put("/api/task_statuses/" + draft.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Draft Name"))
                .andExpect(jsonPath("$.slug").value("draft"));
    }

    @Test
    void testDeleteStatus() throws Exception {
        var dto = new TaskStatusCreateDTO();
        dto.setName("To Delete");
        dto.setSlug("test_delete_slug");

        var result = mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andReturn();

        var statusId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/task_statuses/" + statusId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(statusId)).isEmpty();
    }

    @Test
    void testCannotDeleteStatusUsedByTask() throws Exception {
        var taskDto = new TaskCreateDTO();
        taskDto.setTitle("Task using draft");
        taskDto.setStatus("draft");
        taskDto.setAssigneeId(adminId);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(taskDto)))
                .andExpect(status().isCreated());

        var draft = taskStatusRepository.findBySlug("draft").orElseThrow();

        mockMvc.perform(delete("/api/task_statuses/" + draft.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());

        assertThat(taskStatusRepository.findById(draft.getId())).isPresent();
    }
}
