package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherStation;
import com.fujitsu.trialtask.deliveryfee.model.WeatherObservationModel;
import com.fujitsu.trialtask.deliveryfee.model.WeatherStationModel;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapper;
import com.fujitsu.trialtask.deliveryfee.repository.WeatherMeasurementRepository;
import com.fujitsu.trialtask.deliveryfee.repository.WeatherStationRepository;
import com.fujitsu.trialtask.deliveryfee.util.exception.WeatherDataException;
import com.fujitsu.trialtask.deliveryfee.util.exception.WeatherRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WeatherMeasurementRepository weatherRepository;
    private final WeatherStationRepository stationRepository;
    private final WeatherMeasurementMapper weatherMapper;
    private final RestTemplate restTemplate;
    @Value("${weather.service.request-url}")
    private String weatherRequestUrl;

    /**
     * Finds the latest weather measurement from a station.
     *
     * @param station WeatherStation entity
     * @return WeatherMeasurementDto
     * @throws WeatherDataException Weather data for the station is not available in the database
     */
    public WeatherMeasurementDto getLatestMeasurementFromStation(final WeatherStation station) throws WeatherDataException {
        WeatherMeasurement measurement =
                weatherRepository.findTopByWeatherStationWMOcodeOrderByTimestampDesc(station.getWMOcode())
                        .orElseThrow(() -> new WeatherDataException("Weather data is not available", station.getWMOcode()));
        return weatherMapper.toDto(measurement);
    }

    @Scheduled(cron = "${weather.service.cron-expression}")
    private void updateWeather() {
        final WeatherObservationModel observation = requestWeatherObservation();
        if (observation == null) {
            throw new WeatherRequestException("Weather observation from request is null at " + LocalDateTime.now());
        }
        final List<WeatherMeasurement> measurements = ObservationDtoToWeatherMeasurements(observation);
        if (measurements.isEmpty()) {
            throw new WeatherRequestException("Weather observation from request does not contain measurements at" + LocalDateTime.now());
        }
        weatherRepository.saveAll(measurements);
    }

    private WeatherObservationModel requestWeatherObservation() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_XML));
        try {
            return restTemplate.getForObject(weatherRequestUrl, WeatherObservationModel.class, headers);
        } catch (RestClientException e) {
            throw new WeatherRequestException("An error has occurred when attempting restTemplate.getForObject()", e.getCause());
        }
    }

    /**
     * Extracts weather data as WeatherMeasurement objects from the observation.
     * Only extracts data for stations that are present in the repository.
     *
     * @param observation WeatherObservationModel object from the HTTP request
     * @return List of extracted weather data objects (WeatherMeasurement)
     */
    private List<WeatherMeasurement> ObservationDtoToWeatherMeasurements(final WeatherObservationModel observation) {
        // Timestamp constructor requires time in milliseconds.
        final Timestamp timestamp = new Timestamp(observation.getTimeInSeconds() * 1000);
        final List<WeatherStationModel> stationModels = observation.getStations();
        final Map<Integer, WeatherStation> requiredStations = getStationMap(stationRepository.findAll());

        final List<WeatherMeasurement> measurements = new ArrayList<>();
        for (WeatherStationModel stationModel : stationModels) {
            if (stationModel.getWMOcode() != null) {
                int stationModelWMO = stationModel.getWMOcode();
                if (requiredStations.containsKey(stationModelWMO)) {
                    // Maps air temp, wind speed, phenomenon to measurement.
                    final WeatherMeasurement measurement = weatherMapper.toEntity(stationModel);
                    measurement.setWeatherStation(requiredStations.get(stationModelWMO));
                    measurement.setTimestamp(timestamp);
                    measurements.add(measurement);
                }
            }
        }
        return measurements;
    }

    /**
     * Converts a list of WeatherStation entities into a Map of WeatherStation entities.
     *
     * @param stations List of WeatherStation entities
     * @return Map(Key : Integer ( WMO), Value: WeatherStation)
     */
    private Map<Integer, WeatherStation> getStationMap(final List<WeatherStation> stations) {
        final Map<Integer, WeatherStation> stationMap = new HashMap<>();
        for (WeatherStation station : stations) {
            stationMap.put(station.getWMOcode(), station);
        }
        return stationMap;
    }
}
