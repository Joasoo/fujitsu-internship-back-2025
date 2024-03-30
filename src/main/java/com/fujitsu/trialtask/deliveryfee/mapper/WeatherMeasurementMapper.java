package com.fujitsu.trialtask.deliveryfee.mapper;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.model.WeatherStationModel;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WeatherMeasurementMapper {
    WeatherMeasurementDto toDto(WeatherMeasurement entity);

    /**
     * Maps readings like air temperature etc. from requested XML station object.
     * @param dto XML station object
     * @return WeatherMeasurement
     */
    WeatherMeasurement toEntity(WeatherStationModel dto);
}
