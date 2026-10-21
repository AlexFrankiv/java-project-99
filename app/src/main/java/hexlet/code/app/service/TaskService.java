package hexlet.code.app.service;

import hexlet.code.app.dto.tasks.TaskCreateDTO;
import hexlet.code.app.dto.tasks.TaskDTO;
import hexlet.code.app.dto.tasks.TaskUpdateDTO;
import hexlet.code.app.exceptions.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskMapper taskMapper;

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO showTask(Long id) {
        return taskRepository.findById(id)
                .map(taskMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + "not found"));
    }

    public TaskDTO create(TaskCreateDTO data) {

        var task = taskMapper.map(data);


        var status = statusRepository.findBySlug(data.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Status not found: " + data.getStatus()));
        task.setTaskStatus(status);

        if (data.getAssigneeId() != null) {
            var assignee = userRepository.findById(data.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            task.setAssignee(assignee);
        }

        taskRepository.save(task);

        return taskMapper.map(task);
    }

    public TaskDTO update(TaskUpdateDTO data, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        taskMapper.update(data, task);

        if (data.getStatus().isPresent() && data.getStatus().get() != null) {
            var status = statusRepository.findBySlug(data.getStatus().get())
                    .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
            task.setTaskStatus(status);
        }

        if (data.getAssigneeId().isPresent()) {
            var assigneeId = data.getAssigneeId().get();
            if (assigneeId == null) {
                task.setAssignee(null);
            } else {
                var assignee = userRepository.findById(assigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                task.setAssignee(assignee);
            }
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

}
