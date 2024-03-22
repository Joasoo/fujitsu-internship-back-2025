package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapper;
import com.fujitsu.trialtask.deliveryfee.repository.WeatherMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WeatherMeasurementRepository weatherMeasurementRepository;
    private final WeatherMeasurementMapper weatherMeasurementMapper;

    public WeatherMeasurementDto getLatestMeasurementFromStation(Integer stationId) {
        Optional<WeatherMeasurement> optionalMeasurement =
                weatherMeasurementRepository.findTopByWMOCodeOrderByTimestampDesc(stationId);
        WeatherMeasurement measurement = optionalMeasurement.orElseThrow(() -> new RuntimeException("TODO"));
        return weatherMeasurementMapper.toDto(measurement);
    }
}
