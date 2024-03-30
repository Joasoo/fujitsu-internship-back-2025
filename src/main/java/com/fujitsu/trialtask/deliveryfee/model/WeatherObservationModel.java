package com.fujitsu.trialtask.deliveryfee.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * Class for parsing XML observation object.
 */
@Data
@JacksonXmlRootElement(localName = "observations")
public class WeatherObservationModel {
    @JacksonXmlProperty(localName = "timestamp", isAttribute = true)
    private Long timeInSeconds;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "station")
    private List<WeatherStationModel> stations;
}
