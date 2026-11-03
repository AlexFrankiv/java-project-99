package hexlet.code.app.mapper;

import hexlet.code.app.dto.labels.LabelCreateDTO;
import hexlet.code.app.dto.labels.LabelDTO;
import hexlet.code.app.dto.labels.LabelUpdateDTO;
import hexlet.code.app.model.Label;
import org.mapstruct.*;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LabelMapper {
    public abstract LabelDTO map(Label label);

    public abstract Label map(LabelCreateDTO dto);

    public abstract void update(LabelUpdateDTO dto, @MappingTarget Label label);
}
