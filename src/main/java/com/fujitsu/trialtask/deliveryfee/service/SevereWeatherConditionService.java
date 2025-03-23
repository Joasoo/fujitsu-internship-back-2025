package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;
import com.fujitsu.trialtask.deliveryfee.entity.SevereWeatherCondition;
import com.fujitsu.trialtask.deliveryfee.repository.SevereWeatherConditionRepository;
import com.fujitsu.trialtask.deliveryfee.util.enums.CodeClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SevereWeatherConditionService {
    private final SevereWeatherConditionRepository weatherConditionRepository;

    public List<CodeItem> getCodeItemsFromWeatherMeasurementDto(WeatherMeasurementDto measurementDto) {
        List<CodeItem> items = new ArrayList<>();
        items.addAll(getCodeItemsFromWeatherPhenomenon(measurementDto.getPhenomenon()));
        items.addAll(getCodeItemsFromWeatherFloatMeasurement(measurementDto.getAirTemperature(), CodeClass.AT));
        items.addAll(getCodeItemsFromWeatherFloatMeasurement(measurementDto.getWindSpeed(), CodeClass.WS));
        return items;
    }

    private List<CodeItem> getCodeItemsFromWeatherPhenomenon(String phenomenon) {
        if (phenomenon == null || phenomenon.isBlank()) {
            return List.of();
        }

        List<SevereWeatherCondition> allConditions =
                weatherConditionRepository.findAllByCodeItemCodeClass(CodeClass.WP.name());
        List<SevereWeatherCondition> conditions = allConditions.stream()
                .filter(r -> r.getPhenomena()
                        .stream()
                        .anyMatch(p -> phenomenon.toLowerCase().contains(p.toLowerCase())))
                .toList();

        return conditionsToCodeItems(conditions);
    }

    private List<CodeItem> getCodeItemsFromWeatherFloatMeasurement(Float measurement, CodeClass codeClass) {
        if (measurement == null) {
            return List.of();
        }

        List<SevereWeatherCondition> conditions =
                weatherConditionRepository.findAllByCodeItemCodeClassAndMeasurement(codeClass.name(), measurement);

        return conditionsToCodeItems(conditions);
    }

    private List<CodeItem> conditionsToCodeItems(List<SevereWeatherCondition> conditions) {
        return conditions.stream().map(SevereWeatherCondition::getCodeItem).toList();
    }
}
