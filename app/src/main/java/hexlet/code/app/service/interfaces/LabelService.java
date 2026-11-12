package hexlet.code.app.service.interfaces;

import hexlet.code.app.dto.labels.LabelCreateDTO;
import hexlet.code.app.dto.labels.LabelDTO;
import hexlet.code.app.dto.labels.LabelUpdateDTO;

import java.util.List;

public interface LabelService {
    List<LabelDTO> getAll();
    LabelDTO show(Long id);
    LabelDTO create(LabelCreateDTO data);
    LabelDTO update(LabelUpdateDTO data, Long id);
    void delete(Long id);
}
