package com.fujitsu.trialtask.deliveryfee.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class WeatherObservationDto {
    private List<WeatherStationDto> stations;
    private Timestamp timestamp;
 }
