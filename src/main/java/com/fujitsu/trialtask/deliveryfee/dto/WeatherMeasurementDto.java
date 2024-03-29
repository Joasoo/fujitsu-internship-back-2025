package com.fujitsu.trialtask.deliveryfee.dto;

import com.fujitsu.trialtask.deliveryfee.entity.WeatherStation;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class WeatherMeasurementDto {
    private Long id;
    private Timestamp timestamp;
    private WeatherStation weatherStation;
    private Float airTemperature;
    private Float windSpeed;
    private String phenomenon;
}
