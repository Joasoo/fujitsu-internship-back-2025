package com.fujitsu.trialtask.deliveryfee.util;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.*;
import com.fujitsu.trialtask.deliveryfee.util.enums.WeatherCode;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class TestUtil {
    private TestUtil() {}
    private static long idCounter = 10000L;

    public static RegionalBaseFee getRegionalBaseFee(Vehicle vehicle, City city) {
        return RegionalBaseFee.builder()
                .id(idCounter++)
                .vehicle(vehicle)
                .city(city)
                .feeAmount(getBaseFee(vehicle, city))
                .build();
    }

    /**
     * Return a map of Optional ExtraFees. Key is the CodeItem that is used to request an ExtraFee.
     * Value is Optional.empty() if no extra fee is available for vehicle and code item combination.
     * @param vehicle   vehicle
     * @param codeItems code items
     * @return Map of ExtraFees
     */
    public static Map<CodeItem, Optional<ExtraFee>> getExtraFees(Vehicle vehicle, List<CodeItem> codeItems) {
        Map<CodeItem, Optional<ExtraFee>> fees = new HashMap<>();
        for (CodeItem codeItem : codeItems) {
            Optional<BigDecimal> feeAmount = getExtraFeeAmount(vehicle, codeItem);
            if (feeAmount.isEmpty()) {
                fees.put(codeItem, Optional.empty());
            } else {
                fees.put(codeItem, Optional.of(ExtraFee.builder()
                        .id(idCounter++)
                        .vehicle(vehicle)
                        .codeItem(codeItem)
                        .feeAmount(feeAmount.get())
                        .build())
                );
            }
        }
        return fees;
    }

    /**
     * Returns a weatherMeasurementDto without any special code(s) or with code(s).
     *
     * @param station weather station
     * @return normal weather dto
     */
    public static WeatherMeasurementDto getWeatherMeasurementDto(WeatherStation station, CodeItem... codes) {
        return getWeatherMeasurementDto(station, List.of(codes));
    }

    /**
     * Returns a WeatherMeasurementDto with data according to provided codes.
     *
     * @param station Weather station
     * @param codes   List of code items (weather codes)
     * @return Corresponding WeatherMeasurementDto
     */
    public static WeatherMeasurementDto getWeatherMeasurementDto(WeatherStation station, List<CodeItem> codes) {
        float airTemperature = 10F;
        float windSpeed = 0F;
        String phenomenon = "";
        for (CodeItem codeItem : codes) {
            String code = codeItem.getCode();
            if (code.equals(WeatherCode.AT_UNDER_MINUS_TEN.name())) {
                airTemperature = -20f;
            }
            if (code.equals(WeatherCode.AT_MINUS_TEN_TO_ZERO.name())) {
                airTemperature = -5f;
            }
            if (code.equals(WeatherCode.WS_ABOVE_TWENTY.name())) {
                windSpeed = 30f;
            }
            if (code.equals(WeatherCode.WS_TEN_TO_TWENTY.name())) {
                windSpeed = 15f;
            }
            if (code.equals(WeatherCode.WP_SNOW_SLEET.name())) {
                phenomenon = "snow";
            }
            if (code.equals(WeatherCode.WP_RAIN.name())) {
                phenomenon = "rain";
            }
            if (code.equals(WeatherCode.WP_GLAZE_HAIL_THUNDER.name())) {
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
                    return BigDecimal.valueOf(2);
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Return Optional of extra fee amount if it is available for given vehicle and code, else Optional.empty()
     * @param vehicle vehicle
     * @param codeItem CodeItem tied to extra fee
     * @return Optional of BigDecimal
     */
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
