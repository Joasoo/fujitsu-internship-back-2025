package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.entity.RegionalBaseFee;
import com.fujitsu.trialtask.deliveryfee.repository.RegionalBaseFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RegionalBaseFeeService {
    private final RegionalBaseFeeRepository baseFeeRepository;

    public Optional<RegionalBaseFee> getBaseFee(Long cityId, Long vehicleId) {
        return baseFeeRepository.findByCityIdAndVehicleId(cityId, vehicleId);
    }
}


