package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.DeliveryFeeDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.City;
import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;
import com.fujitsu.trialtask.deliveryfee.entity.ExtraFee;
import com.fujitsu.trialtask.deliveryfee.repository.CityRepository;
import com.fujitsu.trialtask.deliveryfee.repository.VehicleRepository;
import com.fujitsu.trialtask.deliveryfee.util.exception.DeliveryFeeException;
import com.fujitsu.trialtask.deliveryfee.util.exception.WeatherDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryFeeService {
    private final VehicleRepository vehicleRepository;
    private final CityRepository cityRepository;
    private final SevereWeatherConditionService weatherConditionService;
    private final WeatherService weatherService;
    private final WorkProhibitionService prohibitionService;
    private final RegionalBaseFeeService baseFeeService;
    private final ExtraFeeService extraFeeService;

    /**
     * Validates weather conditions for given vehicle. Calculates the base fee, extra fee and total fee for delivery
     * based on city and vehicle.
     *
     * @param cityId    vehicle id
     * @param vehicleId city id
     * @return DeliveryFeeDto(cityId, vehicleId, baseFee, extraFee, totalFee)
     * @throws DeliveryFeeException Invalid city ID | Invalid vehicle ID | Unfit weather conditions for vehicle
     * @throws WeatherDataException Weather data for the city's station is not available in the database
     */
    public DeliveryFeeDto getDeliveryFee(Long cityId, Long vehicleId) throws DeliveryFeeException, WeatherDataException {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new DeliveryFeeException("Invalid vehicle ID");
        }

        City city = cityRepository.findById(cityId).orElseThrow(
                () -> new DeliveryFeeException("Invalid city ID")
        );

        WeatherMeasurementDto measurementDto = weatherService.getLatestMeasurementFromStation(city.getWeatherStation());
        List<String> weatherCodes = weatherConditionService.getCodeItemsFromWeatherMeasurementDto(measurementDto)
                .stream()
                .map(CodeItem::getCode)
                .toList();

        if (prohibitionService.findWeatherProhibitionForVehicle(weatherCodes, vehicleId).isPresent()) {
            throw new DeliveryFeeException("Usage of selected vehicle type is forbidden");
        }

        BigDecimal baseFee = baseFeeService.getBaseFee(cityId, vehicleId).orElseThrow(
                () -> new DeliveryFeeException("This type of vehicle is not allowed in this city")
        ).getFeeAmount();
        BigDecimal extraFee =
                getTotalExtraFeeAmount(extraFeeService.getWeatherExtraFees(weatherCodes, vehicleId));
        BigDecimal totalFee = baseFee.add(extraFee);

        return DeliveryFeeDto.builder()
                .cityId(cityId)
                .vehicleId(vehicleId)
                .baseFee(baseFee)
                .extraFee(extraFee)
                .totalFee(totalFee)
                .build();
    }

    private BigDecimal getTotalExtraFeeAmount(List<ExtraFee> extraFees) {
        BigDecimal total = BigDecimal.ZERO;
        for (ExtraFee extraFee : extraFees) {
            total = total.add(extraFee.getFeeAmount());
        }
        return total;
    }
}
