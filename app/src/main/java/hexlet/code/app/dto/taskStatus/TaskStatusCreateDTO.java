package hexlet.code.app.dto.taskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusCreateDTO {

    @NotBlank
    @Size(min = 1, max = 30)
    private String name;

    @Size(min = 1, max = 30)
    @NotBlank
    private String slug;
}
