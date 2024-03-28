package com.fujitsu.trialtask.deliveryfee;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherObservationDto;
import com.fujitsu.trialtask.deliveryfee.entity.City;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherMeasurement;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapper;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapperImpl;
import com.fujitsu.trialtask.deliveryfee.repository.CityRepository;
import com.fujitsu.trialtask.deliveryfee.service.DeliveryFeeService;
import com.fujitsu.trialtask.deliveryfee.service.WeatherService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.mockito.BDDMockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DeliveryFeeApplicationTests {
    @Spy
    private RestTemplate template = new RestTemplate();
    @Spy
    private WeatherMeasurementMapper weatherMeasurementMapper = new WeatherMeasurementMapperImpl();
    @Mock
    private CityRepository cityRepository;
    @InjectMocks
    private WeatherService service;
    private static List<City> cities;

    @BeforeAll
    static void setUp() {
        City tallinn = new City();
        tallinn.setId(0L);
        tallinn.setName("Tallinn");
        tallinn.setWMOcode(26038);

        City tartu = new City();
        tallinn.setId(1L);
        tartu.setName("Tartu");
        tartu.setWMOcode(26242);

        City parnu = new City();
        parnu.setId(2L);
        parnu.setName("Pärnu");
        parnu.setWMOcode(26231);

        cities = List.of(tallinn, tartu, parnu);
    }

    @Test
    void justTesting() {
        given(cityRepository.findAll()).willReturn(cities);
        WeatherObservationDto observation = service.requestWeatherObservation();
        List<WeatherMeasurement> measurements = service.ObservationDtoToWeatherMeasurements(observation);
        measurements.forEach(System.out::println);
    }

}
