package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.DeliveryFeeDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.City;
import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;
import com.fujitsu.trialtask.deliveryfee.entity.ExtraFee;
import com.fujitsu.trialtask.deliveryfee.entity.RegionalBaseFee;
import com.fujitsu.trialtask.deliveryfee.entity.WorkProhibition;
import com.fujitsu.trialtask.deliveryfee.repository.CityRepository;
import com.fujitsu.trialtask.deliveryfee.repository.CodeItemRepository;
import com.fujitsu.trialtask.deliveryfee.repository.ExtraFeeRepository;
import com.fujitsu.trialtask.deliveryfee.repository.RegionalBaseFeeRepository;
import com.fujitsu.trialtask.deliveryfee.repository.VehicleRepository;
import com.fujitsu.trialtask.deliveryfee.repository.WorkProhibitionRepository;
import com.fujitsu.trialtask.deliveryfee.util.enums.WeatherCode;
import com.fujitsu.trialtask.deliveryfee.util.exception.DeliveryFeeException;
import com.fujitsu.trialtask.deliveryfee.util.exception.CodeItemException;
import com.fujitsu.trialtask.deliveryfee.util.exception.WeatherDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryFeeService {
    private final ExtraFeeRepository extraFeeRepository;
    private final CodeItemRepository codeItemRepository;
    private final RegionalBaseFeeRepository baseFeeRepository;
    private final VehicleRepository vehicleRepository;
    private final CityRepository cityRepository;
    private final WorkProhibitionRepository workProhibitionRepository;
    private final WeatherService weatherService;

    /**
     * Validates weather conditions for given vehicle. Calculates the base fee, extra fee and total fee for delivery
     * based on city and vehicle.
     * @param cityId vehicle id
     * @param vehicleId city id
     * @return DeliveryFeeDto(cityId, vehicleId, baseFee, extraFee, totalFee)
     * @throws DeliveryFeeException Invalid city ID | Invalid vehicle ID | Unfit weather conditions for vehicle
     * @throws WeatherDataException Weather data for the city's station is not available in the database
     */
    public DeliveryFeeDto getDeliveryFee(final Long cityId, final Long vehicleId) throws DeliveryFeeException, WeatherDataException {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new DeliveryFeeException("Invalid vehicle ID");
        }

        final City city = cityRepository.findById(cityId).orElseThrow(
                () -> new DeliveryFeeException("Invalid City ID")
        );
        final WeatherMeasurementDto measurement = weatherService.getLatestMeasurementFromStation(city.getWeatherStation());
        final List<WeatherCode> weatherCodes = getWeatherCodes(measurement);
        final List<CodeItem> weatherCodeItems = getCodeItems(weatherCodes.stream().map(WeatherCode::name).toList());

        if (unfitWeatherConditions(vehicleId, weatherCodeItems)) {
            throw new DeliveryFeeException("Usage of selected vehicle type is forbidden");
        }

        final BigDecimal baseFee = getBaseFeeAmount(cityId, vehicleId);
        final BigDecimal extraFee = getExtraFees(weatherCodeItems, vehicleId);
        final BigDecimal totalFee = baseFee.add(extraFee);
        return new DeliveryFeeDto(cityId, vehicleId, baseFee, extraFee, totalFee);
    }

    private boolean unfitWeatherConditions(final Long vehicleId, final List<CodeItem> weatherCodes) {
        for (CodeItem codeItem : weatherCodes) {
            final Optional<WorkProhibition> prohibition =
                    workProhibitionRepository.findByVehicleIdAndCodeItemCode(vehicleId, codeItem.getCode());
            if (prohibition.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal getBaseFeeAmount(final Long cityId, final Long vehicleId) throws DeliveryFeeException {
        final RegionalBaseFee baseFee = baseFeeRepository.findByCityIdAndVehicleId(cityId, vehicleId)
                .orElseThrow(() -> new DeliveryFeeException("This type of vehicle is not allowed in this city."));
        return baseFee.getFeeAmount();
    }

    private BigDecimal getExtraFees(final List<CodeItem> weatherCodes, final Long vehicleId) {
        BigDecimal total = BigDecimal.ZERO;
        for (CodeItem codeItem : weatherCodes) {
            final Optional<ExtraFee> extraFee = extraFeeRepository.findByVehicleIdAndCodeItemCode(vehicleId, codeItem.getCode());
            if (extraFee.isPresent()) {
                total = total.add(extraFee.get().getFeeAmount());
            }
        }
        return total;
    }

    private List<WeatherCode> getWeatherCodes(final WeatherMeasurementDto measurement) {
        final List<WeatherCode> codes = new ArrayList<>();
        final Optional<WeatherCode> airTempCode = getAirTemperatureCode(measurement);
        final Optional<WeatherCode> windSpeedCode = getWindSpeedCode(measurement);
        final Optional<WeatherCode> phenomenonCode = getWeatherPhenomenonCode(measurement);
        airTempCode.ifPresent(codes::add);
        windSpeedCode.ifPresent(codes::add);
        phenomenonCode.ifPresent(codes::add);
        return codes;
    }

    private Optional<WeatherCode> getWindSpeedCode(final WeatherMeasurementDto measurement) {
        if (measurement.getWindSpeed() == null) {
            return Optional.empty();
        }

        final boolean speedTenToTwenty = 10 <= measurement.getWindSpeed() && measurement.getWindSpeed() <= 20;
        final boolean speedAboveTwenty = measurement.getWindSpeed() > 20;

        if (speedTenToTwenty) {
            return Optional.of(WeatherCode.WS_TEN_TO_TWENTY);
        } else if (speedAboveTwenty) {
            return Optional.of(WeatherCode.WS_ABOVE_TWENTY);
        }

        return Optional.empty();
    }

    private Optional<WeatherCode> getAirTemperatureCode(final WeatherMeasurementDto measurement) {
        if (measurement.getAirTemperature() == null) {
            return Optional.empty();
        }

        final boolean underMinusTenDegrees = measurement.getAirTemperature() < -10;
        final boolean minusTenToZeroDegrees =
                -10 <= measurement.getAirTemperature() && measurement.getAirTemperature() <= 0;

        if (underMinusTenDegrees) {
            return Optional.of(WeatherCode.AT_UNDER_MINUS_TEN);
        } else if (minusTenToZeroDegrees) {
            return Optional.of(WeatherCode.AT_MINUS_TEN_TO_ZERO);
        }

        return Optional.empty();
    }

    private Optional<WeatherCode> getWeatherPhenomenonCode(final WeatherMeasurementDto measurement) {
        if (measurement.getPhenomenon() == null) {
            return Optional.empty();
        }

        final String phenomenon = measurement.getPhenomenon().toLowerCase();
        final boolean rain = phenomenon.contains("rain");
        final boolean snowOrFleet = phenomenon.matches(".*fleet.*|.*snow.*");
        final boolean glazeOrHailOrThunder = phenomenon.matches(".*glaze.*|.*hail.*|.*thunder.*");

        if (rain) {
            return Optional.of(WeatherCode.WP_RAIN);
        } else if (snowOrFleet) {
            return Optional.of(WeatherCode.WP_SNOW_FLEET);
        } else if (glazeOrHailOrThunder) {
            return Optional.of(WeatherCode.WP_GLAZE_HAIL_THUNDER);
        }

        return Optional.empty();
    }

    private List<CodeItem> getCodeItems (final List<String> codes) {
        List<CodeItem> codeItems = new ArrayList<>();
        for (String code : codes) {
            try {
                CodeItem item = codeItemRepository.findByCode(code).orElseThrow(() -> new CodeItemException(code));
                codeItems.add(item);
            } catch (CodeItemException e) {
                log.error(e.getMessage(), e);
            }
        }
        return codeItems;
    }
}
