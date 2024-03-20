package com.fujitsu.trialtask.deliveryfee.dto;

import lombok.Data;

@Data
public class WeatherStationDto {
    private String stationName;
    private Integer WMOCode;
    private Float airTemperature;
    private Float windSpeed;
    private String phenomenon;
}
