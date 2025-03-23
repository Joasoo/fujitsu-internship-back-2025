package com.fujitsu.trialtask.deliveryfee.service;


import com.fujitsu.trialtask.deliveryfee.entity.ExtraFee;
import com.fujitsu.trialtask.deliveryfee.repository.ExtraFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class WeatherExtraFeeService {
    private final ExtraFeeRepository extraFeeRepository;

    public List<ExtraFee> getExtraFees(List<String> weatherCodes, Long vehicleId) {
        return extraFeeRepository.findAllByVehicleIdAndCodeItemCodeIn(vehicleId, weatherCodes);
    }

    /*private Optional<ExtraFee> getWeatherPhenomenonExtraFee(String phenomenon, Long vehicleId) {
        if (phenomenon == null || phenomenon.isBlank()) {
            return Optional.empty();
        }

        List<WeatherCondition> allConditions =
                weatherConditionRepository.findAllByCodeItem_CodeClassIgnoreCase(CodeClass.WP.name());
        Optional<WeatherCondition> condition = allConditions.stream()
                .filter(r -> r.getPhenomenons()
                        .stream()
                        .anyMatch(p -> phenomenon.toLowerCase().contains(p.toLowerCase())))
                .findFirst();

        if (condition.isEmpty()) {
            return Optional.empty();
        }
        return extraFeeRepository.findByVehicleIdAndCodeItemCode(vehicleId, condition.get().getCodeItem().getCode());
    }

    private Optional<ExtraFee> getExtraFeeFromFloatMeasurement(Float measurement, CodeClass codeClass, Long vehicleId) {
        if (measurement == null) {
            return Optional.empty();
        }

        List<WeatherCondition> conditions =
                weatherConditionRepository.findAllByCodeItem_CodeClassAndMeasurement(codeClass.name(), measurement);
        if (conditions.isEmpty()) {
            return Optional.empty();
        }

        List<String> codes = conditions.stream().map(wc -> wc.getCodeItem().getCode()).toList();

        return extraFeeRepository.findByVehicleIdAndCodeItemCodeIn(vehicleId, codes);
    }*/
}
