package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherObservationDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherStationDto;
import com.fujitsu.trialtask.deliveryfee.entity.City;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapper;
import com.fujitsu.trialtask.deliveryfee.repository.CityRepository;
import com.fujitsu.trialtask.deliveryfee.repository.WeatherMeasurementRepository;
import com.fujitsu.trialtask.deliveryfee.util.constants.RequestURL;
import com.fujitsu.trialtask.deliveryfee.util.exception.DeliveryFeeException;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WeatherMeasurementRepository weatherMeasurementRepository;
    private final CityRepository cityRepository;
    private final WeatherMeasurementMapper weatherMeasurementMapper;
    private final RestTemplate restTemplate;

    /**
     * Finds the latest weather measurement from a station (WMO).
     * @param WMOcode wmo code of the station
     * @return WeatherMeasurementDto
     * @throws WeatherDataException Weather data for the station is not available in the database
     */
    public WeatherMeasurementDto getLatestMeasurementFromStation(final Integer WMOcode) throws WeatherDataException {
        WeatherMeasurement measurement = weatherMeasurementRepository.findTopByWMOcodeOrderByTimestampDesc(WMOcode)
                .orElseThrow(() -> new WeatherDataException("Weather data is not available", WMOcode));
        return weatherMeasurementMapper.toDto(measurement);
    }

    @Scheduled(cron = "0 15 * * * *")
    private void updateWeather() {
        final WeatherObservationDto observation = requestWeatherObservation();
        if (observation == null) {
            throw new WeatherRequestException("Weather observation from request is null at " + LocalDateTime.now());
        }
        final List<WeatherMeasurement> measurements = ObservationDtoToWeatherMeasurements(observation);
        if (measurements.isEmpty()) {
            throw new WeatherRequestException("Weather observation from request does not contain measurements at" + LocalDateTime.now());
        }
        weatherMeasurementRepository.saveAll(measurements);
    }

    private WeatherObservationDto requestWeatherObservation() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_XML));

        try {
            return restTemplate.getForObject(RequestURL.WEATHER_OBSERVATION, WeatherObservationDto.class, headers);
        } catch (RestClientException e) {
            throw new WeatherRequestException("An error has occurred when attempting restTemplate.getForObject()", e.getCause());
        }
    }

    private List<WeatherMeasurement> ObservationDtoToWeatherMeasurements(final WeatherObservationDto observation) {
        // Timestamp constructor requires time in milliseconds.
        final Timestamp timestamp = new Timestamp(observation.getTimeInSeconds() * 1000);
        final List<WeatherStationDto> stations = observation.getStations();
        final List<Integer> requiredWmoCodes = cityRepository.findAll().stream().map(City::getWMOcode).toList();
        final List<WeatherMeasurement> measurements = new ArrayList<>();
        for (WeatherStationDto station : stations) {
            if (requiredWmoCodes.contains(station.getWMOcode())) {
                final WeatherMeasurement measurement = weatherMeasurementMapper.toEntity(station);
                measurement.setTimestamp(timestamp);
                measurements.add(measurement);
            }
        }
        return measurements;
    }
}
