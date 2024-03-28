package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "weather_measurement")
public class WeatherMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "timestamp")
    private Timestamp timestamp;

    @Column(name = "station_name")
    private String stationName;

    @Column(name = "wmo_code")
    private Integer WMOcode;

    @Column(name = "air_temperature")
    private Float airTemperature;

    @Column(name = "wind_speed")
    private Float windSpeed;

    @Column(name = "phenomenon")
    private String phenomenon;

    @Override
    public String toString() {
        return new StringBuilder().append("----- Weather Measurement -----\n")
                .append("ID: ").append(id).append("\n")
                .append("Timestamp: ").append(timestamp).append("\n")
                .append("Name: ").append(stationName).append("\n")
                .append("WMO: ").append(WMOcode).append("\n")
                .append("Air temperature: ").append(airTemperature).append("\n")
                .append("Wind Speed: ").append(windSpeed).append("\n")
                .append("Phenomenon: ").append(phenomenon)
                .toString();
    }
}
