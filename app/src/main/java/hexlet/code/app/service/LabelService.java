package hexlet.code.app.service;

import hexlet.code.app.dto.labels.LabelCreateDTO;
import hexlet.code.app.dto.labels.LabelDTO;
import hexlet.code.app.dto.labels.LabelUpdateDTO;
import hexlet.code.app.exceptions.ResourceInUseException;
import hexlet.code.app.exceptions.ResourceNotFoundException;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabelService {
    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelMapper labelMapper;

    public List<LabelDTO> getAll() {
        return labelRepository.findAll().stream().map(labelMapper::map).toList();
    }

    public LabelDTO create(LabelCreateDTO dto) {
        var label = labelMapper.map(dto);
        labelRepository.save(label);

        return labelMapper.map(label);
    }

    public LabelDTO show(Long id) {
        return labelRepository.findById(id).map(labelMapper::map)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
    }

    public LabelDTO update(LabelUpdateDTO dto, Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
        labelMapper.update(dto, label);
        labelRepository.save(label);

        return labelMapper.map(label);
    }

    public void delete(Long id) {
        if (taskRepository.existsByLabelsId(id)) {
            throw new ResourceInUseException("Cannot delete label: it is used by tasks");
        }
        labelRepository.deleteById(id);
    }
}
