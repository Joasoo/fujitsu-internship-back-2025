package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherStation;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapper;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapperImpl;
import com.fujitsu.trialtask.deliveryfee.repository.WeatherMeasurementRepository;
import com.fujitsu.trialtask.deliveryfee.util.exception.WeatherDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {
    @Spy
    private WeatherMeasurementMapper mapper = new WeatherMeasurementMapperImpl();
    @Mock
    private WeatherMeasurementRepository repository;
    @InjectMocks
    private WeatherService service;

    @Test
    void getLatestMeasurementFromStation_MeasurementExists_FilledWeatherMeasurementDto() {
        // given
        WeatherStation station = WeatherStation.builder()
                .WMOcode(12345)
                .name("Test station")
                .build();
        WeatherMeasurement measurement = WeatherMeasurement.builder()
                .id(1L)
                .timestamp(new Timestamp(100000))
                .weatherStation(station)
                .airTemperature(20F)
                .windSpeed(10F)
                .phenomenon("Snow")
                .build();
        given(repository.findTopByWeatherStationWMOcodeOrderByTimestampDesc(station.getWMOcode())).willReturn(Optional.of(measurement));

        // when
        WeatherMeasurementDto result = service.getLatestMeasurementFromStation(station);

        // then
        then(repository).should().findTopByWeatherStationWMOcodeOrderByTimestampDesc(station.getWMOcode());
        then(mapper).should().toDto(measurement);
        assertWeatherMeasurementDto(measurement, result);
    }

    @Test
    void getLatestMeasurementFromStation_NoMeasurement_WeatherDataException() {
        WeatherStation station = WeatherStation.builder()
                .WMOcode(12345)
                .name("Test station")
                .build();
        given(repository.findTopByWeatherStationWMOcodeOrderByTimestampDesc(station.getWMOcode())).willReturn(Optional.empty());

        WeatherDataException thrown = assertThrows(WeatherDataException.class, () -> service.getLatestMeasurementFromStation(station));

        assertEquals("Weather data is not available", thrown.getMessage());
        assertEquals(station.getWMOcode(), thrown.getWMOcode());
    }

    private void assertWeatherMeasurementDto(WeatherMeasurement expected, WeatherMeasurementDto actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.getWeatherStation(), actual.getWeatherStation());
        assertEquals(expected.getAirTemperature(), actual.getAirTemperature());
        assertEquals(expected.getWindSpeed(), actual.getWindSpeed());
        assertEquals(expected.getPhenomenon(), actual.getPhenomenon());
    }
}
