package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.entity.RegionalBaseFee;
import com.fujitsu.trialtask.deliveryfee.repository.RegionalBaseFeeRepository;
import com.fujitsu.trialtask.deliveryfee.util.exception.DeliveryFeeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RegionalBaseFeeService {
    private final RegionalBaseFeeRepository baseFeeRepository;

    public RegionalBaseFee getBaseFee(Long cityId, Long vehicleId) throws DeliveryFeeException {
        return baseFeeRepository.findByCityIdAndVehicleId(cityId, vehicleId)
                .orElseThrow(() -> new DeliveryFeeException("This type of vehicle is not allowed in this city"));
    }
}


