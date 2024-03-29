package com.fujitsu.trialtask.deliveryfee.util;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.*;
import com.fujitsu.trialtask.deliveryfee.util.enums.WeatherCode;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

public class TestUtil {
    private TestUtil() {}
    private static long idCounter = 0L;

    public static WorkProhibition getWorkProhibition(Vehicle vehicle, CodeItem codeItem) {
        return new WorkProhibition(idCounter++, vehicle, codeItem);
    }

    public static RegionalBaseFee getRegionalBaseFee(Vehicle vehicle, City city) {
        return RegionalBaseFee.builder()
                .id(idCounter++)
                .vehicle(vehicle)
                .city(city)
                .feeAmount(getBaseFee(vehicle, city))
                .build();
    }

    public static Optional<ExtraFee> getExtraFee(Vehicle vehicle, CodeItem codeItem) {
        Optional<BigDecimal> feeAmount = getExtraFeeAmount(vehicle, codeItem);
        if (feeAmount.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ExtraFee.builder()
                .id(idCounter++)
                .vehicle(vehicle)
                .codeItem(codeItem)
                .feeAmount(feeAmount.get())
                .build());
    }

    public static WeatherMeasurementDto getWeatherMeasurementDto(WeatherStation station, WeatherCode... codes) {
        float airTemperature = 10F;
        float windSpeed = 0F;
        String phenomenon = "";
        for (WeatherCode code : codes) {
            if (code.equals(WeatherCode.AT_UNDER_MINUS_TEN)) {
                airTemperature = -20f;
            }
            if (code.equals(WeatherCode.AT_MINUS_TEN_TO_ZERO)) {
                airTemperature = -5f;
            }
            if (code.equals(WeatherCode.WS_ABOVE_TWENTY)) {
                windSpeed = 30f;
            }
            if (code.equals(WeatherCode.WS_TEN_TO_TWENTY)) {
                windSpeed = 15f;
            }
            if (code.equals(WeatherCode.WP_SNOW_SLEET)) {
                phenomenon = "snow";
            }
            if (code.equals(WeatherCode.WP_RAIN)) {
                phenomenon = "rain";
            }
            if (code.equals(WeatherCode.WP_GLAZE_HAIL_THUNDER)) {
                phenomenon = "thunder";
            }
        }
        return WeatherMeasurementDto.builder()
                .id(idCounter++)
                .timestamp(new Timestamp(500000))
                .weatherStation(station)
                .airTemperature(airTemperature)
                .windSpeed(windSpeed)
                .phenomenon(phenomenon)
                .build();
    }

    private static BigDecimal getBaseFee(Vehicle vehicle, City city) {
        String cityName = city.getName().toLowerCase();
        String vehicleType = vehicle.getType().toLowerCase();
        if (vehicleType.equals("car")) {
            switch (cityName) {
                case "tallinn":
                    return BigDecimal.valueOf(4);
                case "tartu":
                    return BigDecimal.valueOf(3.5);
                case "pärnu":
                    return BigDecimal.valueOf(3);
            }
        }
        if (vehicleType.equals("scooter")) {
            switch (cityName) {
                case "tallinn":
                    return BigDecimal.valueOf(3.5);
                case "tartu":
                    return BigDecimal.valueOf(3);
                case "pärnu":
                    return BigDecimal.valueOf(2.5);
            }
        }
        if (vehicleType.equals("bike")) {
            switch (cityName) {
                case "tallinn":
                    return BigDecimal.valueOf(3);
                case "tartu":
                    return BigDecimal.valueOf(2.5);
                case "pärnu":
                    return BigDecimal.TWO;
            }
        }
        return BigDecimal.ZERO;
    }

    private static Optional<BigDecimal> getExtraFeeAmount(Vehicle vehicle, CodeItem codeItem) {
        String vehicleType = vehicle.getType().toLowerCase();
        String code = codeItem.getCode();
        if (vehicleType.equals("scooter") || vehicleType.equals("bike")) {
            if (code.equals(WeatherCode.AT_UNDER_MINUS_TEN.name())) {
                return Optional.of(BigDecimal.ONE);
            }
            if (code.equals(WeatherCode.AT_MINUS_TEN_TO_ZERO.name())) {
                return Optional.of(BigDecimal.valueOf(0.5));
            }
            if (code.equals(WeatherCode.WP_SNOW_SLEET.name())) {
                return Optional.of(BigDecimal.ONE);
            }
            if (code.equals(WeatherCode.WP_RAIN.name())) {
                return Optional.of(BigDecimal.valueOf(0.5));
            }
        }
        if (vehicleType.equals("bike")) {
            if (code.equals(WeatherCode.WS_TEN_TO_TWENTY.name())) {
                return Optional.of(BigDecimal.valueOf(0.5));
            }
        }
        return Optional.empty();
    }
}
