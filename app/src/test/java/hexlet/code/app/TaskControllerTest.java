package hexlet.code.app;

import hexlet.code.app.dto.AuthRequest;
import hexlet.code.app.dto.tasks.TaskCreateDTO;
import hexlet.code.app.dto.tasks.TaskUpdateDTO;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskRepository taskRepository;

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
    void testGetAllTasks() throws Exception {
        mockMvc.perform((RequestBuilder) get("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateTask() throws Exception {
        var dto = new TaskCreateDTO();
        dto.setTitle("Test Task");
        dto.setContent("Test Content");
        dto.setStatus("draft");
        dto.setAssigneeId(adminId);
        dto.setIndex(100);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.assignee_id").value(adminId))
                .andExpect(jsonPath("$.index").value(100))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void testGetTaskById() throws Exception {
        var createDto = new TaskCreateDTO();
        createDto.setTitle("Get by ID");
        createDto.setStatus("to_review");

        var result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createDto)))
                .andReturn();

        var taskId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform((RequestBuilder) get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.id").value(taskId))
                .andExpect((ResultMatcher) jsonPath("$.title").value("Get by ID"))
                .andExpect((ResultMatcher) jsonPath("$.status").value("to_review"));
    }

    @Test
    void testUpdateTask() throws Exception {
        var createDto = new TaskCreateDTO();
        createDto.setTitle("Old Title");
        createDto.setContent("Old Content");
        createDto.setStatus("draft");

        var result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createDto)))
                .andReturn();

        var taskId = om.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String partialUpdateJson = """
        {
            "title": "New Title",
            "status": "to_be_fixed"
        }
        """;

        mockMvc.perform(put("/api/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(partialUpdateJson));
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
}
