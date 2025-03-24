package com.fujitsu.trialtask.deliveryfee.util;

import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherStationDto;
import com.fujitsu.trialtask.deliveryfee.entity.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class TestUtil {
    private TestUtil() {
    }

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
     * Returns all necessary extra fees for given vehicle based on weather conditions (codeItems).
     *
     * @param vehicle   vehicle
     * @param codeItems code items
     * @return List of ExtraFees
     */
    public static List<ExtraFee> getExtraFees(Vehicle vehicle, List<CodeItem> codeItems) {
        List<ExtraFee> fees = new ArrayList<>();
        for (CodeItem codeItem : codeItems) {
            Optional<BigDecimal> feeAmount = getExtraFeeAmount(vehicle, codeItem);
            feeAmount.ifPresent(amount -> fees.add(ExtraFee.builder()
                    .id(idCounter++)
                    .vehicle(vehicle)
                    .codeItem(codeItem)
                    .feeAmount(amount)
                    .build()));
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
        for (CodeItem code : codes) {
            if (code.equals(CodeItemUtil.AT_UNDER_MINUS_TEN)) {
                airTemperature = -20f;
            }
            if (code.equals(CodeItemUtil.AT_MINUS_TEN_TO_ZERO)) {
                airTemperature = -5f;
            }
            if (code.equals(CodeItemUtil.WS_ABOVE_TWENTY)) {
                windSpeed = 30f;
            }
            if (code.equals(CodeItemUtil.WS_TEN_TO_TWENTY)) {
                windSpeed = 15f;
            }
            if (code.equals(CodeItemUtil.WP_SNOW_SLEET)) {
                phenomenon = "some SnoW";
            }
            if (code.equals(CodeItemUtil.WP_RAIN)) {
                phenomenon = "contains rAIN";
            }
            if (code.equals(CodeItemUtil.WP_GLAZE_HAIL_THUNDER)) {
                phenomenon = "BIG THUNDER";
            }
        }

        WeatherStationDto stationDto = WeatherStationDto.builder()
                .WMOcode(station.getWMOcode())
                .name(station.getName())
                .build();

        return WeatherMeasurementDto.builder()
                .id(idCounter++)
                .timestamp(new Timestamp(500000))
                .weatherStation(stationDto)
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
     *
     * @param vehicle  vehicle
     * @param codeItem CodeItem tied to extra fee
     * @return Optional of BigDecimal
     */
    private static Optional<BigDecimal> getExtraFeeAmount(Vehicle vehicle, CodeItem code) {
        String vehicleType = vehicle.getType().toLowerCase();

        if (vehicleType.equals("bike")) {
            if (code.equals(CodeItemUtil.WS_TEN_TO_TWENTY)) {
                return Optional.of(BigDecimal.valueOf(0.5));
            }
        }

        if (vehicleType.equals("scooter") || vehicleType.equals("bike")) {
            if (code.equals(CodeItemUtil.AT_UNDER_MINUS_TEN)) {
                return Optional.of(BigDecimal.ONE);
            }
            if (code.equals(CodeItemUtil.AT_MINUS_TEN_TO_ZERO)) {
                return Optional.of(BigDecimal.valueOf(0.5));
            }
            if (code.equals(CodeItemUtil.WP_SNOW_SLEET)) {
                return Optional.of(BigDecimal.ONE);
            }
            if (code.equals(CodeItemUtil.WP_RAIN)) {
                return Optional.of(BigDecimal.valueOf(0.5));
            }
        }

        return Optional.empty();
    }
}
