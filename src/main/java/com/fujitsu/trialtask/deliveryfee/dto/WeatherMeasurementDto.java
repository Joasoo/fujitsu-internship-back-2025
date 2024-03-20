package com.fujitsu.trialtask.deliveryfee.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class WeatherMeasurementDto {
    private Long id;
    private Timestamp timestamp;
    private String stationName;
    private Integer WMOCode;
    private Float airTemperature;
    private Float windSpeed;
    private String phenomenon;
}
