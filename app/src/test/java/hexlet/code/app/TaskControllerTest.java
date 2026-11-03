package hexlet.code.app;

import hexlet.code.app.dto.AuthRequest;
import hexlet.code.app.dto.tasks.TaskCreateDTO;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

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
    void testGetAllTasks() throws Exception {
        mockMvc.perform((RequestBuilder) get("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateTask() throws Exception {
        var bugLabel = labelRepository.findByName("bug").orElseThrow();
        var dto = new TaskCreateDTO();
        dto.setTitle("Test Task with Label");
        dto.setContent("Test Content");
        dto.setStatus("draft");
        dto.setAssigneeId(adminId);
        dto.setTaskLabelIds(Set.of(bugLabel.getId()));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task with Label"))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(bugLabel.getId()));
    }

    @Test
    void testGetTaskById() throws Exception {
        var bugLabel = labelRepository.findByName("bug").orElseThrow();

        var createDto = new TaskCreateDTO();
        createDto.setTitle("Get by ID");
        createDto.setStatus("to_review");
        createDto.setTaskLabelIds(Set.of(bugLabel.getId()));

        var result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createDto)))
                .andReturn();

        var taskId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Get by ID"))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(bugLabel.getId()));
    }

    @Test
    void testUpdateTask() throws Exception {
        var createDto = new TaskCreateDTO();
        createDto.setTitle("Old Title");
        createDto.setStatus("draft");

        var result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createDto)))
                .andReturn();

        var taskId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        var featureLabel = labelRepository.findByName("feature").orElseThrow();

        String updateJson = """
                {
                    "title": "New Title",
                    "taskLabelIds": [%d]
                }
                """.formatted(featureLabel.getId());

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(featureLabel.getId()));
    }

    @Test
    void testDeleteTask() throws Exception {
        var createDto = new TaskCreateDTO();
        createDto.setTitle("To Delete");
        createDto.setStatus("draft");

        var result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createDto)))
                .andReturn();

        var taskId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform((RequestBuilder) delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(taskId)).isEmpty();
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform((RequestBuilder) get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testFilterBySingleParameter() throws Exception {
        var dto1 = new TaskCreateDTO();
        dto1.setTitle("Fix critical bug");
        dto1.setStatus("draft");
        dto1.setAssigneeId(adminId);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        var dto2 = new TaskCreateDTO();
        dto2.setTitle("Create new feature");
        dto2.setStatus("to_review");
        dto2.setAssigneeId(null);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks?titleCont=bug")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Fix critical bug"));

        mockMvc.perform(get("/api/tasks?status=to_review")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Create new feature"));
    }

    @Test
    void testFilterByAllParametersComplex() throws Exception {
        var bugLabel = labelRepository.findByName("bug").orElseThrow();

        var dto1 = new TaskCreateDTO();
        dto1.setTitle("Fix critical bug");
        dto1.setStatus("draft");
        dto1.setAssigneeId(adminId);
        dto1.setTaskLabelIds(Set.of(bugLabel.getId()));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        var dto2 = new TaskCreateDTO();
        dto2.setTitle("Fix another bug");
        dto2.setStatus("to_review");
        dto2.setAssigneeId(adminId);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks")
                        .param("titleCont", "bug")
                        .param("assigneeId", String.valueOf(adminId))
                        .param("status", "draft")
                        .param("labelId", String.valueOf(bugLabel.getId()))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Fix critical bug"));
    }
}
