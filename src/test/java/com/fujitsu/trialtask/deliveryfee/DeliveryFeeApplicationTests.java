package com.fujitsu.trialtask.deliveryfee;

import com.fujitsu.trialtask.deliveryfee.entity.City;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapper;
import com.fujitsu.trialtask.deliveryfee.mapper.WeatherMeasurementMapperImpl;
import com.fujitsu.trialtask.deliveryfee.repository.CityRepository;
import com.fujitsu.trialtask.deliveryfee.service.WeatherService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

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

    }

    @Test
    void justTesting() {

    }

}
