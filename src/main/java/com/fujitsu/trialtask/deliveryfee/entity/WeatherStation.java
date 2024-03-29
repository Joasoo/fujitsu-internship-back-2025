package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "weather_station")
public class WeatherStation {
    @Id
    @Column(name = "wmo_code")
    private Integer WMOcode;

    @NotNull
    @Column(name = "name")
    private String name;
}
