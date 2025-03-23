package com.fujitsu.trialtask.deliveryfee.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class WeatherMeasurementDto {
    private Long id;
    private Timestamp timestamp;
    private WeatherStationDto weatherStation;
    private Float airTemperature;
    private Float windSpeed;
    private String phenomenon;
}
