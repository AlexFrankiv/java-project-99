package hexlet.code.app.service.interfaces;

import hexlet.code.app.dto.tasks.TaskCreateDTO;
import hexlet.code.app.dto.tasks.TaskDTO;
import hexlet.code.app.dto.tasks.TaskUpdateDTO;

import java.util.List;

public interface TaskService {
    List<TaskDTO> getAllTasks(String titleCont, Long assigneeId, String status, Long labelId);
    TaskDTO showTask(Long id);
    TaskDTO create(TaskCreateDTO data);
    TaskDTO update(TaskUpdateDTO data, Long id);
    void delete(Long id);
}
