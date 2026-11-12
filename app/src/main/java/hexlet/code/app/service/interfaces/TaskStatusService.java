package hexlet.code.app.service.interfaces;

import hexlet.code.app.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusDTO;
import hexlet.code.app.dto.taskStatus.TaskStatusUpdateDTO;

import java.util.List;

public interface TaskStatusService {
    List<TaskStatusDTO> getAllStatuses();
    TaskStatusDTO showStatus(Long id);
    TaskStatusDTO createStatus(TaskStatusCreateDTO data);
    TaskStatusDTO updateStatus(TaskStatusUpdateDTO data, Long id);
    void deleteStatus(Long id);

}
