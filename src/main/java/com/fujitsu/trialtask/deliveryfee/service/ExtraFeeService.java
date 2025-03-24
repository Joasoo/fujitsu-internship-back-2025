package com.fujitsu.trialtask.deliveryfee.service;


import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;
import com.fujitsu.trialtask.deliveryfee.entity.ExtraFee;
import com.fujitsu.trialtask.deliveryfee.repository.ExtraFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ExtraFeeService {
    private final ExtraFeeRepository extraFeeRepository;

    public List<ExtraFee> getWeatherExtraFees(List<CodeItem> weatherCodes, Long vehicleId) {
        return extraFeeRepository.findAllByVehicleIdAndCodeItemIn(vehicleId, weatherCodes);
    }
}
