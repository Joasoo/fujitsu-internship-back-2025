package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.DeliveryFeeDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.*;
import com.fujitsu.trialtask.deliveryfee.repository.*;
import com.fujitsu.trialtask.deliveryfee.util.CodeItemUtil;
import com.fujitsu.trialtask.deliveryfee.util.TestUtil;
import com.fujitsu.trialtask.deliveryfee.util.enums.WeatherCode;
import com.fujitsu.trialtask.deliveryfee.util.exception.DeliveryFeeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class DeliveryFeeServiceTest {
    @Mock
    private ExtraFeeRepository extraFeeRepository;
    @Mock
    private CodeItemRepository codeItemRepository;
    @Mock
    private RegionalBaseFeeRepository regionalBaseFeeRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private WorkProhibitionRepository prohibitionRepository;
    @Mock
    private WeatherService weatherService;
    @InjectMocks
    private DeliveryFeeService deliveryService;

    private static WeatherStation station;
    private static City tallinn;
    private static City tartu;
    private static City parnu;
    private static Vehicle car;
    private static Vehicle scooter;
    private static Vehicle bike;
    private static WeatherMeasurementDto normalWeather;

    @BeforeAll
    static void setUp() {
        station = WeatherStation.builder()
                .WMOcode(12345)
                .name("Test station")
                .build();
        tallinn = City.builder()
                .id(1L)
                .name("Tallinn")
                .weatherStation(station)
                .build();
        tartu = City.builder()
                .id(2L)
                .name("Tallinn")
                .weatherStation(station)
                .build();
        parnu = City.builder()
                .id(3L)
                .name("Pärnu")
                .weatherStation(station)
                .build();
        car = Vehicle.builder()
                .id(1L)
                .type("car")
                .build();
        scooter = Vehicle.builder()
                .id(2L)
                .type("scooter")
                .build();
        bike = Vehicle.builder()
                .id(3L)
                .type("bike")
                .build();
        normalWeather = TestUtil.getWeatherMeasurementDto(station);
    }

    @Test
    void getDeliveryFee_InvalidVehicleId_DeliveryFeeException() {
        // given
        given(vehicleRepository.existsById(5L)).willReturn(false);

        // when
        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(tallinn.getId(), 5L));

        // then
        assertEquals("Invalid vehicle ID", thrown.getMessage());
    }

    @Test
    void getDeliveryFee_InvalidCityId_DeliveryFeeException() {
        given(vehicleRepository.existsById(car.getId())).willReturn(true);
        given(cityRepository.findById(1L)).willReturn(Optional.empty());

        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(1L, car.getId()));

        assertEquals("Invalid city ID", thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForUnfitWeatherConditionsTest")
    void getDeliveryFee_UnfitWeatherConditionsForVehicle_DeliveryFeeException(
            Vehicle vehicle, CodeItem code, WeatherMeasurementDto weather) {
        WorkProhibition prohibition = TestUtil.getWorkProhibition(vehicle, code);
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(tallinn.getId())).willReturn(Optional.of(tallinn));
        given(weatherService.getLatestMeasurementFromStation(tallinn.getWeatherStation())).willReturn(weather);
        given(codeItemRepository.findByCode(code.getCode())).willReturn(Optional.of(code));
        given(prohibitionRepository.findByVehicleIdAndCodeItemCode(vehicle.getId(), code.getCode()))
                .willReturn(Optional.of(prohibition));

        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(tallinn.getId(), vehicle.getId()));

        assertEquals("Usage of selected vehicle type is forbidden", thrown.getMessage());
    }

    private static Stream<Arguments> provideArgumentsForUnfitWeatherConditionsTest() {
        WeatherMeasurementDto windSpeedOver20 = TestUtil.getWeatherMeasurementDto(station, WeatherCode.WS_ABOVE_TWENTY);
        WeatherMeasurementDto hail = WeatherMeasurementDto.builder()
                .id(1L)
                .timestamp(new Timestamp(500000))
                .weatherStation(station)
                .phenomenon("contains HaiL")
                .build();
        WeatherMeasurementDto glaze = WeatherMeasurementDto.builder()
                .id(2L)
                .timestamp(new Timestamp(500000))
                .weatherStation(station)
                .phenomenon("contains gLaZe")
                .build();
        WeatherMeasurementDto thunder = WeatherMeasurementDto.builder()
                .id(3L)
                .timestamp(new Timestamp(500000))
                .weatherStation(station)
                .phenomenon("contains THUNDER")
                .build();
        return Stream.of(
                Arguments.of(bike, CodeItemUtil.WS_ABOVE_TWENTY, windSpeedOver20),
                Arguments.of(bike, CodeItemUtil.WP_GLAZE_HAIL_THUNDER, glaze),
                Arguments.of(bike, CodeItemUtil.WP_GLAZE_HAIL_THUNDER, hail),
                Arguments.of(bike, CodeItemUtil.WP_GLAZE_HAIL_THUNDER, thunder),
                Arguments.of(scooter, CodeItemUtil.WP_GLAZE_HAIL_THUNDER, hail),
                Arguments.of(scooter, CodeItemUtil.WP_GLAZE_HAIL_THUNDER, glaze),
                Arguments.of(scooter, CodeItemUtil.WP_GLAZE_HAIL_THUNDER, thunder)
                );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForBaseFeeTest")
    void getDeliveryFee_NoExtraFees_CorrectDeliveryFeeDto(City city, Vehicle vehicle) {
        RegionalBaseFee baseFee = TestUtil.getRegionalBaseFee(vehicle, city);
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(city.getId())).willReturn(Optional.of(city));
        given(weatherService.getLatestMeasurementFromStation(city.getWeatherStation())).willReturn(normalWeather);
        given(regionalBaseFeeRepository.findByCityIdAndVehicleId(city.getId(), vehicle.getId()))
                .willReturn(Optional.of(baseFee));

        DeliveryFeeDto result = deliveryService.getDeliveryFee(city.getId(), vehicle.getId());

        DeliveryFeeDto expected = DeliveryFeeDto.builder()
                .cityId(city.getId())
                .vehicleId(vehicle.getId())
                .baseFee(baseFee.getFeeAmount())
                .extraFee(BigDecimal.ZERO)
                .totalFee(baseFee.getFeeAmount())
                .build();
        assertDeliveryFeeDto(expected, result);
    }

    private static Stream<Arguments> provideArgumentsForBaseFeeTest() {
        return Stream.of(
                Arguments.of(tallinn, car),
                Arguments.of(tallinn, scooter),
                Arguments.of(tallinn, bike),
                Arguments.of(tartu, car),
                Arguments.of(tartu, scooter),
                Arguments.of(tartu, bike),
                Arguments.of(parnu, car),
                Arguments.of(parnu, scooter),
                Arguments.of(parnu, bike)
        );
    }

    /**
     * At maximum a single extra fee is present from weather conditions depending on the vehicle type.
     * @param vehicle Vehicle
     * @param codeItem CodeItem (WeatherCode)
     * @param weather WeatherMeasurementDto (Corresponding to CodeItem)
     */
    @ParameterizedTest
    @MethodSource("provideArgumentsForSingleExtraFeeWeatherConditionTest")
    void getDeliveryFee_SingleExtraFeeWeatherCondition_CorrectDeliveryFeeDto(
            Vehicle vehicle, CodeItem codeItem, WeatherMeasurementDto weather) {
        RegionalBaseFee baseFee = TestUtil.getRegionalBaseFee(vehicle, tallinn);
        Optional<ExtraFee> extraFee = TestUtil.getExtraFee(vehicle, codeItem);
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(tallinn.getId())).willReturn(Optional.of(tallinn));
        given(weatherService.getLatestMeasurementFromStation(tallinn.getWeatherStation())).willReturn(weather);
        given(codeItemRepository.findByCode(codeItem.getCode())).willReturn(Optional.of(codeItem));
        given(regionalBaseFeeRepository.findByCityIdAndVehicleId(tallinn.getId(), vehicle.getId())).willReturn(Optional.of(baseFee));
        given(extraFeeRepository.findByVehicleIdAndCodeItemCode(vehicle.getId(), codeItem.getCode())).willReturn(extraFee);

        DeliveryFeeDto result = deliveryService.getDeliveryFee(tallinn.getId(), vehicle.getId());

        BigDecimal extraFeeAmount = BigDecimal.ZERO;
        if (extraFee.isPresent()) {
            extraFeeAmount = extraFee.get().getFeeAmount();
        }
        DeliveryFeeDto expected = DeliveryFeeDto.builder()
                .cityId(tallinn.getId())
                .vehicleId(vehicle.getId())
                .baseFee(baseFee.getFeeAmount())
                .extraFee(extraFeeAmount)
                .totalFee(baseFee.getFeeAmount().add(extraFeeAmount))
                .build();
        assertDeliveryFeeDto(expected, result);
    }

    private static Stream<Arguments> provideArgumentsForSingleExtraFeeWeatherConditionTest() {
        WeatherMeasurementDto airTempUnderMinus10 = TestUtil.getWeatherMeasurementDto(station, WeatherCode.AT_UNDER_MINUS_TEN);
        WeatherMeasurementDto airTempMinus10to0 = TestUtil.getWeatherMeasurementDto(station, WeatherCode.AT_MINUS_TEN_TO_ZERO);
        WeatherMeasurementDto windSpeed10to20 = TestUtil.getWeatherMeasurementDto(station, WeatherCode.WS_TEN_TO_TWENTY);
        WeatherMeasurementDto snowSleet = TestUtil.getWeatherMeasurementDto(station, WeatherCode.WP_SNOW_SLEET);
        WeatherMeasurementDto rain = TestUtil.getWeatherMeasurementDto(station, WeatherCode.WP_RAIN);

        return Stream.of(
                Arguments.of(car, CodeItemUtil.AT_UNDER_MINUS_TEN, airTempUnderMinus10),
                Arguments.of(scooter, CodeItemUtil.AT_UNDER_MINUS_TEN, airTempUnderMinus10),
                Arguments.of(bike, CodeItemUtil.AT_UNDER_MINUS_TEN, airTempUnderMinus10),
                Arguments.of(car, CodeItemUtil.AT_MINUS_TEN_TO_ZERO, airTempMinus10to0),
                Arguments.of(scooter, CodeItemUtil.AT_MINUS_TEN_TO_ZERO, airTempMinus10to0),
                Arguments.of(bike, CodeItemUtil.AT_MINUS_TEN_TO_ZERO, airTempMinus10to0),
                Arguments.of(car, CodeItemUtil.WS_TEN_TO_TWENTY, windSpeed10to20),
                Arguments.of(scooter, CodeItemUtil.WS_TEN_TO_TWENTY, windSpeed10to20),
                Arguments.of(bike, CodeItemUtil.WS_TEN_TO_TWENTY, windSpeed10to20),
                Arguments.of(car, CodeItemUtil.WP_SNOW_SLEET, snowSleet),
                Arguments.of(scooter, CodeItemUtil.WP_SNOW_SLEET, snowSleet),
                Arguments.of(bike, CodeItemUtil.WP_SNOW_SLEET, snowSleet),
                Arguments.of(car, CodeItemUtil.WP_RAIN, rain),
                Arguments.of(scooter, CodeItemUtil.WP_RAIN, rain),
                Arguments.of(bike, CodeItemUtil.WP_RAIN, rain)
                );
    }

    private void assertDeliveryFeeDto(DeliveryFeeDto expected, DeliveryFeeDto result) {
        assertEquals(expected.getCityId(), result.getCityId());
        assertEquals(expected.getVehicleId(), result.getVehicleId());
        assertEquals(0, expected.getBaseFee().compareTo(result.getBaseFee()));
        assertEquals(0, expected.getExtraFee().compareTo(result.getExtraFee()));
        assertEquals(0, expected.getTotalFee().compareTo(result.getTotalFee()));
    }

}
