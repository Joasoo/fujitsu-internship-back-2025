package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.DeliveryFeeDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.entity.*;
import com.fujitsu.trialtask.deliveryfee.repository.*;
import com.fujitsu.trialtask.deliveryfee.util.CodeItemUtil;
import com.fujitsu.trialtask.deliveryfee.util.TestUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
                .id(378L)
                .name("Tallinn")
                .weatherStation(station)
                .build();
        tartu = City.builder()
                .id(534L)
                .name("Tallinn")
                .weatherStation(station)
                .build();
        parnu = City.builder()
                .id(394L)
                .name("Pärnu")
                .weatherStation(station)
                .build();
        car = Vehicle.builder()
                .id(101L)
                .type("car")
                .build();
        scooter = Vehicle.builder()
                .id(504L)
                .type("scooter")
                .build();
        bike = Vehicle.builder()
                .id(26L)
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
        then(vehicleRepository).should().existsById(5L);
        assertEquals("Invalid vehicle ID", thrown.getMessage());
    }

    @Test
    void getDeliveryFee_InvalidCityId_DeliveryFeeException() {
        given(vehicleRepository.existsById(car.getId())).willReturn(true);
        given(cityRepository.findById(1L)).willReturn(Optional.empty());

        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(1L, car.getId()));

        then(vehicleRepository).should().existsById(car.getId());
        then(cityRepository).should().findById(1L);
        assertEquals("Invalid city ID", thrown.getMessage());
    }

    @Test
    void getDeliveryFee_InvalidVehicleAndCityCombination_DeliveryFeeException() {
        given(vehicleRepository.existsById(car.getId())).willReturn(true);
        given(cityRepository.findById(tallinn.getId())).willReturn(Optional.of(tallinn));
        given(regionalBaseFeeRepository.findByCityIdAndVehicleId(tallinn.getId(), car.getId())).willReturn(Optional.empty());
        given(weatherService.getLatestMeasurementFromStation(tallinn.getWeatherStation())).willReturn(normalWeather);

        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(tallinn.getId(), car.getId()));

        assertEquals("This type of vehicle is not allowed in this city", thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideForUnfitWeatherConditionsTest")
    void getDeliveryFee_UnfitWeatherConditionsForVehicle_DeliveryFeeException(
            Vehicle vehicle, CodeItem codeItem, WeatherMeasurementDto weather) {
        WorkProhibition prohibition = new WorkProhibition(10L, vehicle, codeItem);
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(tallinn.getId())).willReturn(Optional.of(tallinn));
        given(weatherService.getLatestMeasurementFromStation(tallinn.getWeatherStation())).willReturn(weather);
        given(codeItemRepository.findByCode(codeItem.getCode())).willReturn(Optional.of(codeItem));
        given(prohibitionRepository.findByVehicleIdAndCodeItemCode(vehicle.getId(), codeItem.getCode()))
                .willReturn(Optional.of(prohibition));

        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(tallinn.getId(), vehicle.getId()));

        then(vehicleRepository).should().existsById(vehicle.getId());
        then(cityRepository).should().findById(tallinn.getId());
        then(weatherService).should().getLatestMeasurementFromStation(tallinn.getWeatherStation());
        then(codeItemRepository).should().findByCode(codeItem.getCode());
        then(prohibitionRepository).should().findByVehicleIdAndCodeItemCode(vehicle.getId(), codeItem.getCode());
        assertEquals("Usage of selected vehicle type is forbidden", thrown.getMessage());
    }

    private static Stream<Arguments> provideForUnfitWeatherConditionsTest() {
        WeatherMeasurementDto windSpeedOver20 = TestUtil.getWeatherMeasurementDto(station, CodeItemUtil.WS_ABOVE_TWENTY);
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
    @MethodSource("provideForBaseFeeTest")
    void getDeliveryFee_NoExtraFees_CorrectDeliveryFeeDto(City city, Vehicle vehicle) {
        RegionalBaseFee baseFee = TestUtil.getRegionalBaseFee(vehicle, city);
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(city.getId())).willReturn(Optional.of(city));
        given(weatherService.getLatestMeasurementFromStation(city.getWeatherStation())).willReturn(normalWeather);
        given(regionalBaseFeeRepository.findByCityIdAndVehicleId(city.getId(), vehicle.getId()))
                .willReturn(Optional.of(baseFee));

        DeliveryFeeDto result = deliveryService.getDeliveryFee(city.getId(), vehicle.getId());

        then(vehicleRepository).should().existsById(vehicle.getId());
        then(cityRepository).should().findById(city.getId());
        then(weatherService).should().getLatestMeasurementFromStation(city.getWeatherStation());
        then(regionalBaseFeeRepository).should().findByCityIdAndVehicleId(city.getId(), vehicle.getId());
        DeliveryFeeDto expected = DeliveryFeeDto.builder()
                .cityId(city.getId())
                .vehicleId(vehicle.getId())
                .baseFee(baseFee.getFeeAmount())
                .extraFee(BigDecimal.ZERO)
                .totalFee(baseFee.getFeeAmount())
                .build();
        assertDeliveryFeeDto(expected, result);
    }

    private static Stream<Arguments> provideForBaseFeeTest() {
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

    @ParameterizedTest
    @MethodSource("provideForAirTempUnderMinusTenDeliveryFeeTest")
    void getDeliveryFee_AirTempUnderMinusTen_CorrectDeliveryFeeDto(Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
    }

    private static Stream<Arguments> provideForAirTempUnderMinusTenDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, CodeItemUtil.AT_UNDER_MINUS_TEN),
                Arguments.of(scooter, CodeItemUtil.AT_UNDER_MINUS_TEN),
                Arguments.of(bike, CodeItemUtil.AT_UNDER_MINUS_TEN)
        );
    }

    @ParameterizedTest
    @MethodSource("provideForAirTempMinusTenToZeroDeliveryFeeTest")
    void getDeliveryFee_AirTempMinusTenToZero_CorrectDeliveryFeeDto(Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
    }

    private static Stream<Arguments> provideForAirTempMinusTenToZeroDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, CodeItemUtil.AT_MINUS_TEN_TO_ZERO),
                Arguments.of(scooter, CodeItemUtil.AT_MINUS_TEN_TO_ZERO),
                Arguments.of(bike, CodeItemUtil.WP_GLAZE_HAIL_THUNDER)
        );
    }

    @ParameterizedTest
    @MethodSource("provideForWindSpeedTenToTwentyDeliveryFeeTest")
    void getDeliveryFee_WindSpeedTenToTwenty_CorrectDeliveryFeeDto(Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
    }

    private static Stream<Arguments> provideForWindSpeedTenToTwentyDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, CodeItemUtil.WS_TEN_TO_TWENTY),
                Arguments.of(scooter, CodeItemUtil.WS_TEN_TO_TWENTY),
                Arguments.of(bike, CodeItemUtil.WS_TEN_TO_TWENTY)
        );
    }

    @ParameterizedTest
    @MethodSource("provideForSnowOrSleetDeliveryFeeTest")
    void getDeliveryFee_WeatherPhenomenonSnowOrSleet_CorrectDeliveryFeeDto(Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
    }

    private static Stream<Arguments> provideForSnowOrSleetDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, CodeItemUtil.WP_SNOW_SLEET),
                Arguments.of(scooter, CodeItemUtil.WP_SNOW_SLEET),
                Arguments.of(bike, CodeItemUtil.WP_SNOW_SLEET)
        );
    }

    @ParameterizedTest
    @MethodSource("provideForRainDeliveryFeeTest")
    void getDeliveryFee_WeatherPhenomenonRain_CorrectDeliveryFeeDto(Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
    }

    private static Stream<Arguments> provideForRainDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, CodeItemUtil.WP_RAIN),
                Arguments.of(scooter, CodeItemUtil.WP_RAIN),
                Arguments.of(bike, CodeItemUtil.WP_RAIN)
        );
    }

    @ParameterizedTest
    @MethodSource("provideForLowAirTempAndHighWindSpeedDeliveryFeeTest")
    void getDeliveryFee_LowAirTempAndHighWindSpeed_CorrectDeliveryFeeDto(Vehicle vehicle, List<CodeItem> codeItems) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItems);
    }

    private static Stream<Arguments> provideForLowAirTempAndHighWindSpeedDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY)),

                Arguments.of(car, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY))
        );
    }


    @ParameterizedTest
    @MethodSource("provideForLowAirTempAndBadPhenomenonDeliveryFeeTest")
    void getDeliveryFee_LowAirTempAndBadPhenomenon_CorrectDeliveryFeeDto(Vehicle vehicle, List<CodeItem> codeItems) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItems);
    }

    private static Stream<Arguments> provideForLowAirTempAndBadPhenomenonDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WP_SNOW_SLEET)),

                Arguments.of(car, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WP_RAIN)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WP_RAIN)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WP_RAIN)),

                Arguments.of(car, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WP_SNOW_SLEET)),

                Arguments.of(car, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WP_RAIN)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WP_RAIN)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WP_RAIN))
        );
    }

    @ParameterizedTest
    @MethodSource("provideForHighWindSpeedAndBadPhenomenonDeliveryFeeTest")
    void getDeliveryFee_HighWindSpeedAndBadPhenomenon_CorrectDeliveryFeeDto(Vehicle vehicle, List<CodeItem> codeItems) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItems);
    }

    private static Stream<Arguments> provideForHighWindSpeedAndBadPhenomenonDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, List.of(CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(scooter, List.of(CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(bike, List.of(CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),

                Arguments.of(car, List.of(CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)),
                Arguments.of(scooter, List.of(CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)),
                Arguments.of(bike, List.of(CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN))
        );
    }

    @ParameterizedTest
    @MethodSource("provideForLowAirTempAndHighWindSpeedAndBadPhenomenonTest")
    void getDeliveryFee_LowAirTempAndHighWindSpeedAndBadPhenomenon_CorrectDeliveryFeeDto(Vehicle vehicle, List<CodeItem> codeItems) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItems);
    }

    private static Stream<Arguments> provideForLowAirTempAndHighWindSpeedAndBadPhenomenonTest() {
        return Stream.of(
                Arguments.of(car, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),

                Arguments.of(car, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)),

                Arguments.of(car, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET)),

                Arguments.of(car, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)),
                Arguments.of(scooter, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)),
                Arguments.of(bike, List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN))
        );
    }


    /**
     * Tests if the deliveryFeeDto matches the expected deliveryFeeDto for a given single special weather condition.
     * Zero or one extra fee may be present.
     *
     * @param vehicle  Vehicle
     * @param codeItem CodeItems (WeatherCode)
     */
    private void testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(
            Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(vehicle, List.of(codeItem));
    }

    /**
     * Tests if the deliveryFeeDto matches the expected deliveryFeeDto for given special weather conditions.
     * Zero or more extra fees may be present based on given vehicle.
     * Constant Tallinn is used since the purpose is to test extra fees.
     *
     * @param vehicle   Vehicle
     * @param codeItems List of CodeItems (WeatherCodes)
     */
    private void testGetDeliveryFee_SpecialWeatherConditions_CorrectDeliveryFeeDto(
            Vehicle vehicle, List<CodeItem> codeItems) {
        // given
        // Get the corresponding weatherMeasurementDto for given code items (weather codes)
        WeatherMeasurementDto weather = TestUtil.getWeatherMeasurementDto(station, codeItems);
        RegionalBaseFee baseFee = TestUtil.getRegionalBaseFee(vehicle, tallinn);
        // Key: Code that was used for request, Value is empty if there is no extra fee rule for vehicle and code combination.
        Map<CodeItem, Optional<ExtraFee>> extraFeeMap = TestUtil.getExtraFees(vehicle, codeItems);
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(tallinn.getId())).willReturn(Optional.of(tallinn));
        given(weatherService.getLatestMeasurementFromStation(tallinn.getWeatherStation())).willReturn(weather);
        given(regionalBaseFeeRepository.findByCityIdAndVehicleId(tallinn.getId(), vehicle.getId())).willReturn(Optional.of(baseFee));
        for (CodeItem codeItem : codeItems) {
            given(codeItemRepository.findByCode(codeItem.getCode())).willReturn(Optional.of(codeItem));
            given(extraFeeRepository.findByVehicleIdAndCodeItemCode(vehicle.getId(), codeItem.getCode()))
                    .willReturn(extraFeeMap.get(codeItem));
        }

        // when
        DeliveryFeeDto result = deliveryService.getDeliveryFee(tallinn.getId(), vehicle.getId());

        // then
        then(vehicleRepository).should().existsById(vehicle.getId());
        then(cityRepository).should().findById(tallinn.getId());
        then(weatherService).should().getLatestMeasurementFromStation(tallinn.getWeatherStation());
        then(regionalBaseFeeRepository).should().findByCityIdAndVehicleId(tallinn.getId(), vehicle.getId());
        for (CodeItem codeItem : codeItems) {
            then(codeItemRepository).should().findByCode(codeItem.getCode());
            then(extraFeeRepository).should().findByVehicleIdAndCodeItemCode(vehicle.getId(), codeItem.getCode());
        }
        BigDecimal extraFeeAmount = BigDecimal.ZERO;
        for (Optional<ExtraFee> extraFee : extraFeeMap.values()) {
            if (extraFee.isPresent()) {
                extraFeeAmount = extraFeeAmount.add(extraFee.get().getFeeAmount());
            }
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

    private void assertDeliveryFeeDto(DeliveryFeeDto expected, DeliveryFeeDto result) {
        assertEquals(expected.getCityId(), result.getCityId());
        assertEquals(expected.getVehicleId(), result.getVehicleId());
        assertEquals(0, expected.getBaseFee().compareTo(result.getBaseFee()));
        assertEquals(0, expected.getExtraFee().compareTo(result.getExtraFee()));
        assertEquals(0, expected.getTotalFee().compareTo(result.getTotalFee()));
    }
}
