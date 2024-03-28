package com.fujitsu.trialtask.deliveryfee.mapper;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherStationDto;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WeatherMeasurementMapper {
    WeatherMeasurementDto toDto(WeatherMeasurement entity);
    WeatherMeasurement toEntity(WeatherStationDto dto);
}
