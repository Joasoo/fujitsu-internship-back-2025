package com.fujitsu.trialtask.deliveryfee.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;


@Data
public class WeatherStationModel {
    @JacksonXmlProperty(localName = "name")
    private String stationName;

    @JacksonXmlProperty(localName = "wmocode")
    private Integer WMOcode;

    @JacksonXmlProperty(localName = "airtemperature")
    private Float airTemperature;

    @JacksonXmlProperty(localName = "windspeed")
    private Float windSpeed;

    @JacksonXmlProperty(localName = "phenomenon")
    private String phenomenon;
}
