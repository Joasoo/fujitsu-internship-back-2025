package com.fujitsu.trialtask.deliveryfee.service;


import com.fujitsu.trialtask.deliveryfee.entity.WorkProhibition;
import com.fujitsu.trialtask.deliveryfee.repository.WorkProhibitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkProhibitionService {
    private final WorkProhibitionRepository workProhibitionRepository;

    public Optional<WorkProhibition> findWeatherProhibitionForVehicle(List<String> weatherCodes, Long vehicleId) {
        return workProhibitionRepository.findByVehicleIdAndCodeItemCodeIn(vehicleId, weatherCodes);
    }
}
