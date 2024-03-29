package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherStation;
import com.fujitsu.trialtask.deliveryfee.model.WeatherObservationModel;
import com.fujitsu.trialtask.deliveryfee.model.WeatherStationModel;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapper;
import com.fujitsu.trialtask.deliveryfee.repository.WeatherMeasurementRepository;
import com.fujitsu.trialtask.deliveryfee.repository.WeatherStationRepository;
import com.fujitsu.trialtask.deliveryfee.util.constants.RequestURL;
import com.fujitsu.trialtask.deliveryfee.util.exception.WeatherDataException;
import com.fujitsu.trialtask.deliveryfee.util.exception.WeatherRequestException;
import lombok.RequiredArgsConstructor;
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

    /**
     * Finds the latest weather measurement from a station.
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

    @Scheduled(cron = "0 15 * * * *")
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
            return restTemplate.getForObject(RequestURL.WEATHER_OBSERVATION, WeatherObservationModel.class, headers);
        } catch (RestClientException e) {
            throw new WeatherRequestException("An error has occurred when attempting restTemplate.getForObject()", e.getCause());
        }
    }

    private List<WeatherMeasurement> ObservationDtoToWeatherMeasurements(final WeatherObservationModel observation) {
        // Timestamp constructor requires time in milliseconds.
        final Timestamp timestamp = new Timestamp(observation.getTimeInSeconds() * 1000);
        final List<WeatherStationModel> stationModels = observation.getStations();
        final Map<Integer, WeatherStation> requiredStations = getStationMap(stationRepository.findAll());  // K: WMO

        final List<WeatherMeasurement> measurements = new ArrayList<>();
        for (WeatherStationModel stationModel : stationModels) {
            int stationModelWMO = stationModel.getWMOcode();
            if (requiredStations.containsKey(stationModelWMO)) {
                // Maps weather readings
                final WeatherMeasurement measurement = weatherMapper.toEntity(stationModel);
                measurement.setWeatherStation(requiredStations.get(stationModelWMO));
                measurement.setTimestamp(timestamp);
                measurements.add(measurement);
            }
        }
        return measurements;
    }

    private Map<Integer, WeatherStation> getStationMap(List<WeatherStation> stations) {
        Map<Integer, WeatherStation> stationMap = new HashMap<>();
        for (WeatherStation station : stations) {
            stationMap.put(station.getWMOcode(), station);
        }
        return stationMap;
    }
}
