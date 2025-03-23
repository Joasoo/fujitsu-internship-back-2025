package com.fujitsu.trialtask.deliveryfee.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class WeatherStationDto {
    private Integer WMOcode;
    private String name;
}
