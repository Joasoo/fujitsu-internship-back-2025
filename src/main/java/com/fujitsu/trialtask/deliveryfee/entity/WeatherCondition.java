package com.fujitsu.trialtask.deliveryfee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "weather_condition")
public class WeatherCondition {
    @Id
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "code_item")
    private CodeItem codeItem;

    @Column(name = "min_measurement")
    private Float minMeasurement;

    @Column(name = "max_measurement")
    private Float maxMeasurement;

    /**Only one of the phenomenons has to apply (OR operator)**/
    @ElementCollection
    @CollectionTable(name = "weather_phenomenon", joinColumns = @JoinColumn(name = "weather_condition_id"))
    @Column(name = "phenomenon")
    private List<String> phenomenons;
}
