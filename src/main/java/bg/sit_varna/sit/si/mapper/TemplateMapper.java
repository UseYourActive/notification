package bg.sit_varna.sit.si.mapper;

import bg.sit_varna.sit.si.dto.response.TemplateResponse;
import bg.sit_varna.sit.si.entity.TemplateRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jakarta")
public interface TemplateMapper {

    TemplateResponse toResponse(TemplateRecord record);
}