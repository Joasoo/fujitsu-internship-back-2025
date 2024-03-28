package com.fujitsu.trialtask.deliveryfee.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "observations")
public class WeatherObservationDto {
    @JacksonXmlProperty(localName = "timestamp", isAttribute = true)
    private Long timeInSeconds;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "station")
    private List<WeatherStationDto> stations;
}
