package com.fujitsu.trialtask.deliveryfee.service;

import com.fujitsu.trialtask.deliveryfee.dto.DeliveryFeeDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherMeasurementDto;
import com.fujitsu.trialtask.deliveryfee.dto.WeatherStationDto;
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
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class DeliveryFeeServiceTest {
    @Mock
    private ExtraFeeService extraFeeService;
    @Mock
    private SevereWeatherConditionService weatherConditionService;
    @Mock
    private RegionalBaseFeeService baseFeeService;
    @Mock
    private WorkProhibitionService prohibitionService;
    @Mock
    private WeatherService weatherService;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private CityRepository cityRepository;
    @InjectMocks
    private DeliveryFeeService deliveryService;

    private static WeatherStation station;
    private static City tallinn;
    private static City tartu;
    private static City parnu;
    private static Vehicle car;
    private static Vehicle scooter;
    private static Vehicle bike;
    private static List<Vehicle> allVehicles;
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
        allVehicles = List.of(car, scooter, bike);
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
        // given
        given(vehicleRepository.existsById(car.getId())).willReturn(true);
        given(cityRepository.findById(1L)).willReturn(Optional.empty());

        // when
        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(1L, car.getId()));

        // then
        then(vehicleRepository).should().existsById(car.getId());
        then(cityRepository).should().findById(1L);
        assertEquals("Invalid city ID", thrown.getMessage());
    }

    @Test
    void getDeliveryFee_InvalidVehicleAndCityCombination_DeliveryFeeException() {
        // given
        given(vehicleRepository.existsById(car.getId())).willReturn(true);
        given(cityRepository.findById(tallinn.getId())).willReturn(Optional.of(tallinn));
        given(baseFeeService.getBaseFee(tallinn.getId(), car.getId())).willReturn(Optional.empty());
        given(weatherService.getLatestMeasurementFromStation(tallinn.getWeatherStation())).willReturn(normalWeather);

        // when
        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(tallinn.getId(), car.getId()));

        // then
        assertEquals("This type of vehicle is not allowed in this city", thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideForUnfitWeatherConditionsTest")
    void getDeliveryFee_UnfitWeatherConditionsForVehicle_DeliveryFeeException(
            Vehicle vehicle, CodeItem codeItem, WeatherMeasurementDto weather) {
        // given
        WorkProhibition prohibition = new WorkProhibition(10L, vehicle, codeItem);
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(tallinn.getId())).willReturn(Optional.of(tallinn));
        given(weatherService.getLatestMeasurementFromStation(tallinn.getWeatherStation())).willReturn(weather);
        given(weatherConditionService.getCodeItemsFromWeatherMeasurementDto(weather)).willReturn(List.of(codeItem));
        given(prohibitionService.findWeatherProhibitionForVehicle(List.of(codeItem.getCode()), vehicle.getId()))
                .willReturn(Optional.of(prohibition));

        // when
        DeliveryFeeException thrown = assertThrows(DeliveryFeeException.class,
                () -> deliveryService.getDeliveryFee(tallinn.getId(), vehicle.getId()));

        // then
        then(vehicleRepository).should().existsById(vehicle.getId());
        then(cityRepository).should().findById(tallinn.getId());
        then(weatherService).should().getLatestMeasurementFromStation(tallinn.getWeatherStation());
        then(prohibitionService).should().findWeatherProhibitionForVehicle(List.of(codeItem.getCode()), vehicle.getId());
        assertEquals("Usage of selected vehicle type is forbidden", thrown.getMessage());
    }

    private static Stream<Arguments> provideForUnfitWeatherConditionsTest() {
        WeatherMeasurementDto windSpeedOver20 = TestUtil.getWeatherMeasurementDto(station, CodeItemUtil.WS_ABOVE_TWENTY);
        WeatherStationDto stationDto = WeatherStationDto.builder()
                .WMOcode(station.getWMOcode())
                .name(station.getName())
                .build();
        WeatherMeasurementDto hail = WeatherMeasurementDto.builder()
                .id(1L)
                .timestamp(new Timestamp(500000))
                .weatherStation(stationDto)
                .phenomenon("contains HaiL")
                .build();
        WeatherMeasurementDto glaze = WeatherMeasurementDto.builder()
                .id(2L)
                .timestamp(new Timestamp(500000))
                .weatherStation(stationDto)
                .phenomenon("contains gLaZe")
                .build();
        WeatherMeasurementDto thunder = WeatherMeasurementDto.builder()
                .id(3L)
                .timestamp(new Timestamp(500000))
                .weatherStation(stationDto)
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
    void getDeliveryFee_NoExtraFeesFromWeather_CorrectDeliveryFeeDto(City city, Vehicle vehicle) {
        // given
        RegionalBaseFee baseFee = TestUtil.getRegionalBaseFee(vehicle, city);
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(city.getId())).willReturn(Optional.of(city));
        given(weatherService.getLatestMeasurementFromStation(city.getWeatherStation())).willReturn(normalWeather);
        given(baseFeeService.getBaseFee(city.getId(), vehicle.getId()))
                .willReturn(Optional.of(baseFee));

        // when
        DeliveryFeeDto result = deliveryService.getDeliveryFee(city.getId(), vehicle.getId());

        // then
        then(vehicleRepository).should().existsById(vehicle.getId());
        then(cityRepository).should().findById(city.getId());
        then(weatherService).should().getLatestMeasurementFromStation(city.getWeatherStation());
        then(baseFeeService).should().getBaseFee(city.getId(), vehicle.getId());
        DeliveryFeeDto expected = getExpectedDeliveryFeeDto(vehicle, baseFee, List.of());
        assertDeliveryFeeDto(expected, result);
    }

    private static Stream<Arguments> provideForBaseFeeTest() {
        List<City> cities = List.of(tallinn, tartu, parnu);

        return cities.stream()
                .flatMap(city -> allVehicles.stream()
                        .map(vehicle -> Arguments.of(city, vehicle))
                );
    }

    @ParameterizedTest
    @MethodSource("provideForAirTempUnderMinusTenDeliveryFeeTest")
    void getDeliveryFee_AirTempUnderMinusTen_CorrectDeliveryFeeDto(Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
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
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
    }

    private static Stream<Arguments> provideForAirTempMinusTenToZeroDeliveryFeeTest() {
        return Stream.of(
                Arguments.of(car, CodeItemUtil.AT_MINUS_TEN_TO_ZERO),
                Arguments.of(scooter, CodeItemUtil.AT_MINUS_TEN_TO_ZERO),
                Arguments.of(bike, CodeItemUtil.AT_MINUS_TEN_TO_ZERO)
        );
    }

    @ParameterizedTest
    @MethodSource("provideForWindSpeedTenToTwentyDeliveryFeeTest")
    void getDeliveryFee_WindSpeedTenToTwenty_CorrectDeliveryFeeDto(Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
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
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
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
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItem);
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
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItems);
    }

    private static Stream<Arguments> provideForLowAirTempAndHighWindSpeedDeliveryFeeTest() {
        List<List<CodeItem>> conditions = List.of(
                List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY),
                List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY)
        );

        return allVehicles.stream()
                .flatMap(vehicle -> conditions.stream()
                        .map(condition -> Arguments.of(vehicle, condition))
                );
    }


    @ParameterizedTest
    @MethodSource("provideForLowAirTempAndBadPhenomenonDeliveryFeeTest")
    void getDeliveryFee_LowAirTempAndBadPhenomenon_CorrectDeliveryFeeDto(Vehicle vehicle, List<CodeItem> codeItems) {
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItems);
    }

    private static Stream<Arguments> provideForLowAirTempAndBadPhenomenonDeliveryFeeTest() {
        List<List<CodeItem>> conditions = List.of(
                List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WP_SNOW_SLEET),
                List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WP_RAIN),
                List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WP_SNOW_SLEET),
                List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WP_RAIN)
        );

        return allVehicles.stream()
                .flatMap(vehicle -> conditions.stream()
                        .map(condition -> Arguments.of(vehicle, condition))
                );
    }

    @ParameterizedTest
    @MethodSource("provideForHighWindSpeedAndBadPhenomenonDeliveryFeeTest")
    void getDeliveryFee_HighWindSpeedAndBadPhenomenon_CorrectDeliveryFeeDto(Vehicle vehicle, List<CodeItem> codeItems) {
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItems);
    }

    private static Stream<Arguments> provideForHighWindSpeedAndBadPhenomenonDeliveryFeeTest() {
        List<List<CodeItem>> conditions = List.of(
                List.of(CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET),
                List.of(CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)
        );

        return allVehicles.stream()
                .flatMap(vehicle -> conditions.stream()
                        .map(condition -> Arguments.of(vehicle, condition))
                );
    }

    @ParameterizedTest
    @MethodSource("provideForLowAirTempAndHighWindSpeedAndBadPhenomenonTest")
    void getDeliveryFee_LowAirTempAndHighWindSpeedAndBadPhenomenon_CorrectDeliveryFeeDto(Vehicle vehicle, List<CodeItem> codeItems) {
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, codeItems);
    }

    private static Stream<Arguments> provideForLowAirTempAndHighWindSpeedAndBadPhenomenonTest() {
        List<Vehicle> vehicles = List.of(car, scooter, bike);
        List<List<CodeItem>> conditions = List.of(
                List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET),
                List.of(CodeItemUtil.AT_UNDER_MINUS_TEN, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN),
                List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_SNOW_SLEET),
                List.of(CodeItemUtil.AT_MINUS_TEN_TO_ZERO, CodeItemUtil.WS_TEN_TO_TWENTY, CodeItemUtil.WP_RAIN)
        );

        return vehicles.stream()
                .flatMap(vehicle -> conditions.stream().map(condition -> Arguments.of(vehicle, condition)));
    }


    /**
     * Tests if the deliveryFeeDto matches the expected deliveryFeeDto for a given single severe weather condition.
     * Zero or one extra fee may be present.
     *
     * @param vehicle  Vehicle
     * @param codeItem CodeItems (WeatherCode)
     */
    private void testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(
            Vehicle vehicle, CodeItem codeItem) {
        testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(vehicle, List.of(codeItem));
    }

    /**
     * Tests if the deliveryFeeDto matches the expected deliveryFeeDto for given special weather conditions.
     * Zero or more extra fees may be present based on given vehicle and weather conditions.
     * Constant Tallinn is used since the purpose is to test extra fees.
     *
     * @param vehicle   Vehicle
     * @param codeItems List of CodeItems (WeatherCodes)
     */
    private void testGetDeliveryFee_SevereWeatherConditions_CorrectDeliveryFeeDto(
            Vehicle vehicle, List<CodeItem> codeItems) {
        // given
        // Get the corresponding weatherMeasurementDto for given code items (weather codes)
        WeatherMeasurementDto weather = TestUtil.getWeatherMeasurementDto(station, codeItems);
        RegionalBaseFee baseFee = TestUtil.getRegionalBaseFee(vehicle, tallinn);
        List<ExtraFee> extraFees = TestUtil.getExtraFees(vehicle, codeItems);
        setUpMocks(vehicle, codeItems, weather, baseFee, extraFees);

        // when
        DeliveryFeeDto result = deliveryService.getDeliveryFee(tallinn.getId(), vehicle.getId());

        // then
        verifyMockInteractions(vehicle, codeItems, weather, extraFees);
        DeliveryFeeDto expected = getExpectedDeliveryFeeDto(vehicle, baseFee, extraFees);
        assertDeliveryFeeDto(expected, result);
    }

    private void setUpMocks(Vehicle vehicle, List<CodeItem> codeItems, WeatherMeasurementDto weather,
                            RegionalBaseFee baseFee, List<ExtraFee> extraFees) {
        given(vehicleRepository.existsById(vehicle.getId())).willReturn(true);
        given(cityRepository.findById(tallinn.getId())).willReturn(Optional.of(tallinn));
        given(weatherService.getLatestMeasurementFromStation(tallinn.getWeatherStation())).willReturn(weather);
        given(weatherConditionService.getCodeItemsFromWeatherMeasurementDto(weather)).willReturn(codeItems);
        given(baseFeeService.getBaseFee(tallinn.getId(), vehicle.getId())).willReturn(Optional.of(baseFee));
        given(extraFeeService.getWeatherExtraFees(CodeItemUtil.asStrings(codeItems), vehicle.getId())).willReturn(extraFees);
    }

    private void verifyMockInteractions(Vehicle vehicle, List<CodeItem> codeItems, WeatherMeasurementDto weather, List<ExtraFee> extraFees) {
        then(vehicleRepository).should().existsById(vehicle.getId());
        then(cityRepository).should().findById(tallinn.getId());
        then(weatherService).should().getLatestMeasurementFromStation(tallinn.getWeatherStation());
        then(weatherConditionService).should().getCodeItemsFromWeatherMeasurementDto(weather);
        then(baseFeeService).should().getBaseFee(tallinn.getId(), vehicle.getId());
        then(extraFeeService).should().getWeatherExtraFees(CodeItemUtil.asStrings(codeItems), vehicle.getId());
    }

    private DeliveryFeeDto getExpectedDeliveryFeeDto(Vehicle vehicle, RegionalBaseFee baseFee, List<ExtraFee> extraFees) {
        BigDecimal extraFeeAmount = extraFees.stream()
                .map(ExtraFee::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DeliveryFeeDto.builder()
                .cityId(baseFee.getCity().getId())
                .vehicleId(vehicle.getId())
                .baseFee(baseFee.getFeeAmount())
                .extraFee(extraFeeAmount)
                .totalFee(baseFee.getFeeAmount().add(extraFeeAmount))
                .build();
    }

    private void assertDeliveryFeeDto(DeliveryFeeDto expected, DeliveryFeeDto result) {
        assertEquals(expected.getCityId(), result.getCityId());
        assertEquals(expected.getVehicleId(), result.getVehicleId());
        assertEquals(0, expected.getBaseFee().compareTo(result.getBaseFee()));
        assertEquals(0, expected.getExtraFee().compareTo(result.getExtraFee()));
        assertEquals(0, expected.getTotalFee().compareTo(result.getTotalFee()));
    }
}
