package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.DeliveryFeeDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.*;
import com.fujitsu.trialtask.deliveryfee.repository.CityRepository;
import com.fujitsu.trialtask.deliveryfee.repository.ExtraFeeCodeRepository;
import com.fujitsu.trialtask.deliveryfee.repository.ExtraFeeRepository;
import com.fujitsu.trialtask.deliveryfee.repository.RegionalBaseFeeRepository;
import com.fujitsu.trialtask.deliveryfee.repository.VehicleRepository;
import com.fujitsu.trialtask.deliveryfee.util.enums.VehicleType;
import com.fujitsu.trialtask.deliveryfee.util.enums.WeatherCode;
import com.fujitsu.trialtask.deliveryfee.util.exception.DeliveryFeeException;
import com.fujitsu.trialtask.deliveryfee.util.exception.ExtraFeeCodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryFeeService {
    private final ExtraFeeRepository extraFeeRepository;
    private final ExtraFeeCodeRepository extraFeeCodeRepository;
    private final RegionalBaseFeeRepository baseFeeRepository;
    private final VehicleRepository vehicleRepository;
    private final CityRepository cityRepository;
    private final WeatherService weatherService;

    public DeliveryFeeDto getDeliveryFee(Long cityId, Long vehicleId) throws DeliveryFeeException {
        final City city = cityRepository.findById(cityId).orElseThrow(
                () -> new DeliveryFeeException("Invalid City ID.", DeliveryFeeException.Reason.INVALID_CITY_ID)
        );
        final Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(
                () -> new DeliveryFeeException("Invalid vehicle ID.", DeliveryFeeException.Reason.INVALID_VEHICLE_ID)
        );
        final WeatherMeasurementDto measurement = weatherService.getLatestMeasurementFromStation(city.getWMOCode());

        if (unfitWeatherConditions(vehicle, measurement)) {
            throw new DeliveryFeeException("Usage of selected vehicle type is forbidden", DeliveryFeeException.Reason.UNFIT_WEATHER_CONDITIONS);
        }

        final BigDecimal totalFee = getBaseFeeAmount(city, vehicle).add(getExtraFees(city, vehicle));
        return new DeliveryFeeDto(cityId, vehicleId, totalFee);
    }

    private boolean unfitWeatherConditions(Vehicle vehicle, WeatherMeasurementDto measurement) {
        final Optional<WeatherCode> windSpeedCodeOptional = getWindSpeedCode(measurement);
        final Optional<WeatherCode> weatherPhenomenonCodeOptional = getWeatherPhenomenonCode(measurement);
        final String vehicleType = vehicle.getType();

        if (windSpeedCodeOptional.isPresent()) {
            final boolean highSpeed = windSpeedCodeOptional.get().equals(WeatherCode.WSEF_ABOVE_TWNENTY);
            final boolean bike = vehicleType.equalsIgnoreCase(VehicleType.BIKE.name());
            if (highSpeed && bike) {
                return true;
            }
        }

        if (weatherPhenomenonCodeOptional.isPresent()) {
            final boolean glazeOrHailOrThunder = weatherPhenomenonCodeOptional.get().equals(WeatherCode.WPEF_GLAZE_HAIL_THUNDER);
            final boolean bikeOrScooter = vehicleType.equalsIgnoreCase(VehicleType.BIKE.name())
                    || vehicleType.equalsIgnoreCase(VehicleType.SCOOTER.name());
            return glazeOrHailOrThunder && bikeOrScooter;
        }

        return false;
    }

    private BigDecimal getBaseFeeAmount(City city, Vehicle vehicle) throws DeliveryFeeException {
        final RegionalBaseFee baseFee = baseFeeRepository.findByCityIdAndVehicleId(city.getId(), vehicle.getId())
                .orElseThrow(() -> new DeliveryFeeException("Base fee for this city and vehicle does not exist",
                        DeliveryFeeException.Reason.BASE_FEE_DOES_NOT_EXIST));
        return baseFee.getFeeAmount();
    }

    private BigDecimal getExtraFees(City city, Vehicle vehicle) throws DeliveryFeeException {
        final WeatherMeasurementDto measurement = weatherService.getLatestMeasurementFromStation(city.getWMOCode());
        final Optional<ExtraFee> airTempFeeOptional = getAirTemperatureFee(vehicle, measurement);
        final Optional<ExtraFee> windSpeedFeeOptional = getWindSpeedFee(vehicle, measurement);
        final Optional<ExtraFee> weatherPhenomenonFeeOptional = getWeatherPhenomenonFee(vehicle, measurement);

        BigDecimal total = BigDecimal.ZERO;

        if (airTempFeeOptional.isPresent()) {
            total = total.add(airTempFeeOptional.get().getFeeAmount());
        }
        if (windSpeedFeeOptional.isPresent()) {
            total = total.add(windSpeedFeeOptional.get().getFeeAmount());
        }
        if (weatherPhenomenonFeeOptional.isPresent()) {
            total = total.add(weatherPhenomenonFeeOptional.get().getFeeAmount());
        }

        return total;
    }

    private Optional<ExtraFee> getAirTemperatureFee(Vehicle vehicle, WeatherMeasurementDto measurement) {
        Optional<WeatherCode> airTempCodeOptional = getAirTemperatureCode(measurement);
        return getExtraFee(vehicle, airTempCodeOptional);
    }

    private Optional<ExtraFee> getWindSpeedFee(Vehicle vehicle, WeatherMeasurementDto measurement) {
        Optional<WeatherCode> windSpeedCodeOptional = getWindSpeedCode(measurement);
        return getExtraFee(vehicle, windSpeedCodeOptional);

    }

    private Optional<ExtraFee> getWeatherPhenomenonFee(Vehicle vehicle, WeatherMeasurementDto measurement) {
        Optional<WeatherCode> phenomenonCodeOptional = getWeatherPhenomenonCode(measurement);
        return getExtraFee(vehicle, phenomenonCodeOptional);
    }

    private Optional<ExtraFee> getExtraFee(Vehicle vehicle, Optional<WeatherCode> weatherCodeOptional) {
        if (weatherCodeOptional.isEmpty()) {
            return Optional.empty();
        }

        ExtraFeeCode code = null;
        try {
            code = getExtraFeeCode(weatherCodeOptional.get());
        } catch (ExtraFeeCodeException e) {
            logExtraFeeCodeException(e);
        }

        if (code == null) {
            return Optional.empty();
        }
        return extraFeeRepository.findByVehicleIdAndCode(vehicle.getId(), code.getCode());
    }

    private Optional<WeatherCode> getWindSpeedCode(WeatherMeasurementDto measurement) {
        if (measurement.getWindSpeed() == null) {
            return Optional.empty();
        }
        final boolean speedTenToTwenty = 10 <= measurement.getWindSpeed() && measurement.getWindSpeed() <= 20;
        final boolean speedAboveTwenty = measurement.getWindSpeed() > 20;
        if (speedTenToTwenty) {
            return Optional.of(WeatherCode.WSEF_TEN_TO_TWENTY);
        } else if (speedAboveTwenty) {
            return Optional.of(WeatherCode.WSEF_ABOVE_TWNENTY);
        }
        return Optional.empty();
    }

    private Optional<WeatherCode> getAirTemperatureCode(WeatherMeasurementDto measurement) {
        if (measurement.getAirTemperature() == null) {
            return Optional.empty();
        }
        final boolean underMinusTenDegrees = measurement.getAirTemperature() < -10;
        final boolean minusTenToZeroDegrees =
                -10 <= measurement.getAirTemperature() && measurement.getAirTemperature() <= 0;

        if (underMinusTenDegrees) {
            return Optional.of(WeatherCode.ATEF_UNDER_MINUS_TEN);
        } else if (minusTenToZeroDegrees) {
            return Optional.of(WeatherCode.ATEF_MINUS_TEN_TO_ZERO);
        }
        return Optional.empty();
    }

    private Optional<WeatherCode> getWeatherPhenomenonCode(WeatherMeasurementDto measurement) {
        if (measurement.getPhenomenon() == null) {
            return Optional.empty();
        }
        final String phenomenon = measurement.getPhenomenon().toLowerCase();
        final boolean rain = phenomenon.contains("rain");
        final boolean snowOrFleet = phenomenon.matches(".*fleet.*|.*snow.*");
        final boolean glazeOrHailOrThunder = phenomenon.matches(".*glaze.*|.*hail.*|.*thunder.*");

        if (rain) {
            return Optional.of(WeatherCode.WPEF_RAIN);
        } else if (snowOrFleet) {
            return Optional.of(WeatherCode.WPEF_SNOW_FLEET);
        } else if (glazeOrHailOrThunder) {
            return Optional.of(WeatherCode.WPEF_GLAZE_HAIL_THUNDER);
        }
        return Optional.empty();
    }

    private ExtraFeeCode getExtraFeeCode(WeatherCode code) throws ExtraFeeCodeException {
        return extraFeeCodeRepository.findByCode(code.name()).orElseThrow(
                () -> new ExtraFeeCodeException("Extra fee code does not exist.",
                        DeliveryFeeException.Reason.EXTRA_FEE_CODE_DOES_NOT_EXIST, code)
        );
    }

    private void logExtraFeeCodeException(ExtraFeeCodeException e) {
        log.error(String.format("%s Code: %s", e.getMessage(), e.getCode().name()));
    }
}
