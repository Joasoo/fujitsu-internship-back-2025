package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherMeasurementRepository extends JpaRepository<WeatherMeasurement, Long> {
    Optional<WeatherMeasurement> findTopByWeatherStationWMOcodeOrderByTimestampDesc(Integer WMOcode);
}
