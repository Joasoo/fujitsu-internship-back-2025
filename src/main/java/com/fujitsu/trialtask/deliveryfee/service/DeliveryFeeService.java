package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.DeliveryFeeDto;
import com.fujitsu.trialtask.deliveryfee.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryFeeService {
    private final ExtraFeeRepository extraFeeRepository;
    private final RegionalBaseFeeRepository baseFeeRepository;
    private final VehicleRepository vehicleRepository;
    private final CityRepository cityRepository;
    private final WeatherService weatherService;

    public DeliveryFeeDto getDeliveryFee(Long cityId, Long vehicleId) {
        return new DeliveryFeeDto();
    }
}
