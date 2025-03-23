package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;
import com.fujitsu.trialtask.deliveryfee.entity.WeatherCondition;
import com.fujitsu.trialtask.deliveryfee.repository.WeatherConditionRepository;
import com.fujitsu.trialtask.deliveryfee.util.enums.CodeClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeItemService {
    private final WeatherConditionRepository weatherConditionRepository;

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

        List<WeatherCondition> allConditions =
                weatherConditionRepository.findAllByCodeItemCodeClass(CodeClass.WP.name());
        List<WeatherCondition> conditions = allConditions.stream()
                .filter(r -> r.getPhenomenons()
                        .stream()
                        .anyMatch(p -> phenomenon.toLowerCase().contains(p.toLowerCase())))
                .toList();

        return conditionsToCodeItems(conditions);
    }

    private List<CodeItem> getCodeItemsFromWeatherFloatMeasurement(Float measurement, CodeClass codeClass) {
        if (measurement == null) {
            return List.of();
        }

        List<WeatherCondition> conditions =
                weatherConditionRepository.findAllByCodeItemCodeClassAndMeasurement(codeClass.name(), measurement);

        return conditionsToCodeItems(conditions);
    }

    private List<CodeItem> conditionsToCodeItems(List<WeatherCondition> conditions) {
        return conditions.stream().map(WeatherCondition::getCodeItem).toList();
    }
}
