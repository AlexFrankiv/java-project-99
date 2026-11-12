package hexlet.code.app.service;

import hexlet.code.app.dto.tasks.TaskCreateDTO;
import hexlet.code.app.dto.tasks.TaskDTO;
import hexlet.code.app.dto.tasks.TaskUpdateDTO;
import hexlet.code.app.exceptions.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskSpecification;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.service.interfaces.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskStatusRepository statusRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final LabelRepository labelRepository;

    public List<TaskDTO> getAllTasks(String titleCont, Long assigneeId, String status, Long labelId) {
        Specification<Task> spec = Specification.where(TaskSpecification.titleContains(titleCont))
                .and(TaskSpecification.hasAssigneeId(assigneeId))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasLabelId(labelId));

        return taskRepository.findAll(spec).stream()
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

        if (data.getTaskLabelIds() != null) {
            Set<Label> labels = new HashSet<>(labelRepository.findAllById(data.getTaskLabelIds()));
            task.setLabels(labels);
        }

        taskRepository.save(task);

        return taskMapper.map(task);
    }

    public TaskDTO update(TaskUpdateDTO data, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        taskMapper.update(data, task);

        if (data.getStatus() != null) {
            var status = statusRepository.findBySlug(data.getStatus())
                    .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
            task.setTaskStatus(status);
        }

        if (data.getAssigneeId() != null) {
            var assignee = userRepository.findById(data.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            task.setAssignee(assignee);
        }

        if (data.getTaskLabelIds() != null) {
            Set<Long> ids = data.getTaskLabelIds();
            task.setLabels(new HashSet<>(labelRepository.findAllById(ids)));
        }


        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

}
