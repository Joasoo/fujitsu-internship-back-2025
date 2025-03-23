--liquibase formatted sql

--changeset Markus Joasoo:23-03-2025 Create tables

CREATE TABLE IF NOT EXISTS weather_station
(
    wmo_code INTEGER PRIMARY KEY,
    name     CHARACTER VARYING NOT NULL
);

CREATE TABLE IF NOT EXISTS city
(
    id                       BIGINT PRIMARY KEY,
    name                     CHARACTER VARYING NOT NULL,
    weather_station_wmo_code INTEGER           NOT NULL,

    CONSTRAINT fk_city_station_wmo_code FOREIGN KEY (weather_station_wmo_code) REFERENCES weather_station (wmo_code),
    CONSTRAINT ak_city_name_station_wmo_code UNIQUE (name, weather_station_wmo_code)
);

CREATE TABLE IF NOT EXISTS vehicle
(
    id   BIGINT PRIMARY KEY,
    type CHARACTER VARYING NOT NULL,

    CONSTRAINT ak_vehicle_type UNIQUE (type)
);

CREATE TABLE IF NOT EXISTS regional_base_fee
(
    id         BIGSERIAL PRIMARY KEY,
    city_id    BIGINT         NOT NULL,
    vehicle_id BIGINT         NOT NULL,
    fee_amount NUMERIC(16, 2) NOT NULL,

    CONSTRAINT fk_regional_base_fee_city_id FOREIGN KEY (city_id) REFERENCES city (id),
    CONSTRAINT fk_regional_base_fee_vehicle_id FOREIGN KEY (vehicle_id) REFERENCES vehicle (id),
    CONSTRAINT ak_regional_base_fee_vehicle_id_city_id UNIQUE (vehicle_id, city_id),
    CONSTRAINT chk_regional_base_fee_fee_amount CHECK (fee_amount > 0)
);

CREATE TABLE IF NOT EXISTS code_item
(
    code       CHARACTER VARYING PRIMARY KEY,
    code_class CHARACTER VARYING NOT NULL
);

CREATE TABLE IF NOT EXISTS extra_fee
(
    id         BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT            NOT NULL,
    code_item  CHARACTER VARYING NOT NULL,
    fee_amount NUMERIC(16, 2)    NOT NULL,

    CONSTRAINT fk_extra_fee_vehicle_id FOREIGN KEY (vehicle_id) REFERENCES vehicle (id),
    CONSTRAINT fk_extra_fee_code_item FOREIGN KEY (code_item) REFERENCES code_item (code),
    CONSTRAINT ak_extra_fee_vehicle_id_code UNIQUE (vehicle_id, code_item),
    CONSTRAINT chk_extra_fee_fee_amount CHECK (fee_amount > 0)
);

CREATE TABLE IF NOT EXISTS work_prohibition
(
    id         BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT            NOT NULL,
    code_item  CHARACTER VARYING NOT NULL,

    CONSTRAINT fk_work_prohibition_vehicle_id FOREIGN KEY (vehicle_id) REFERENCES vehicle (id),
    CONSTRAINT fk_work_prohibition_code_item FOREIGN KEY (code_item) REFERENCES code_item (code),
    CONSTRAINT ak_work_prohibition_vehicle_id_code_item UNIQUE (vehicle_id, code_item)
);

CREATE TABLE IF NOT EXISTS weather_measurement
(
    id                       BIGSERIAL PRIMARY KEY,
    timestamp                TIMESTAMP NOT NULL,
    weather_station_wmo_code INTEGER   NOT NULL,
    air_temperature          DOUBLE PRECISION,
    wind_speed               DOUBLE PRECISION,
    phenomenon               CHARACTER VARYING,

    CONSTRAINT fk_weather_measurement_weather_station_wmo_code FOREIGN KEY (weather_station_wmo_code) REFERENCES weather_station (wmo_code)
);

CREATE TABLE IF NOT EXISTS severe_weather_condition
(
    id              BIGSERIAL PRIMARY KEY,
    code_item       CHARACTER VARYING NOT NULL,
    min_measurement DOUBLE PRECISION,
    max_measurement DOUBLE PRECISION,

    CONSTRAINT fk_severe_weather_condition_code_item FOREIGN KEY (code_item) REFERENCES code_item (code),
    CONSTRAINT ak_severe_weather_condition_code_item UNIQUE (code_item)

);

CREATE TABLE IF NOT EXISTS weather_phenomenon
(
    severe_weather_condition_id  BIGINT NOT NULL,
    phenomenon CHARACTER VARYING NOT NULL,
    CONSTRAINT pk_weather_phenomenon PRIMARY KEY (severe_weather_condition_id, phenomenon),
    CONSTRAINT fk_weather_phenomenon_severe_weather_condition_id
    FOREIGN KEY (severe_weather_condition_id) REFERENCES severe_weather_condition (id)
);

INSERT INTO weather_station (wmo_code, name)
VALUES (26038, 'Tallinn-Harku'),
       (26242, 'Tartu-Tõravere'),
       (41803, 'Pärnu');


INSERT INTO city (id, name, weather_station_wmo_code)
VALUES (1, 'Tallinn', 26038),
       (2, 'Tartu', 26242),
       (3, 'Pärnu', 41803);

INSERT INTO vehicle (id, type)
VALUES (1, 'car'),
       (2, 'scooter'),
       (3, 'bike');

INSERT INTO code_item (code, code_class)
VALUES ('AT_UNDER_MINUS_TEN', 'AT'),
       ('AT_MINUS_TEN_TO_ZERO', 'AT'),
       ('WS_TEN_TO_TWENTY', 'WS'),
       ('WS_ABOVE_TWENTY', 'WS'),
       ('WP_SNOW_SLEET', 'WP'),
       ('WP_RAIN', 'WP'),
       ('WP_GLAZE_HAIL_THUNDER', 'WP');

INSERT INTO severe_weather_condition (code_item, min_measurement, max_measurement)
VALUES ('AT_UNDER_MINUS_TEN', null, -10),
       ('AT_MINUS_TEN_TO_ZERO', -10, 0),
       ('WS_TEN_TO_TWENTY', 10, 20),
       ('WS_ABOVE_TWENTY', 20, null),
       ('WP_SNOW_SLEET', null, null),
       ('WP_RAIN', null, null),
       ('WP_GLAZE_HAIL_THUNDER', null, null);

INSERT INTO weather_phenomenon (severe_weather_condition_id, phenomenon)
VALUES
    ((SELECT id FROM severe_weather_condition WHERE code_item = 'WP_SNOW_SLEET'), 'snow'),
    ((SELECT id FROM severe_weather_condition WHERE code_item = 'WP_SNOW_SLEET'), 'sleet'),
    ((SELECT id FROM severe_weather_condition WHERE code_item = 'WP_RAIN'), 'rain'),
    ((SELECT id FROM severe_weather_condition WHERE code_item = 'WP_GLAZE_HAIL_THUNDER'), 'glaze'),
    ((SELECT id FROM severe_weather_condition WHERE code_item = 'WP_GLAZE_HAIL_THUNDER'), 'hail'),
    ((SELECT id FROM severe_weather_condition WHERE code_item = 'WP_GLAZE_HAIL_THUNDER'), 'thunder');

INSERT INTO work_prohibition (vehicle_id, code_item)
VALUES (3, 'WS_ABOVE_TWENTY'),
       (3, 'WP_GLAZE_HAIL_THUNDER'),
       (2, 'WP_GLAZE_HAIL_THUNDER');

INSERT INTO regional_base_fee (city_id, vehicle_id, fee_amount)
VALUES (1, 1, 4),   -- Car in Tallinn
       (1, 2, 3.5), -- Scooter in Tallinn
       (1, 3, 3),   -- Bike in Tallinn
       (2, 1, 3.5), -- Car in Tartu
       (2, 2, 3),   -- Scooter in Tartu
       (2, 3, 2.5), -- Bike in Tartu
       (3, 1, 3),   -- Car in Pärnu
       (3, 2, 2.5), -- Scooter in Pärnu
       (3, 3, 2); -- Bike in Pärnu

INSERT INTO extra_fee (vehicle_id, code_item, fee_amount)
VALUES (2, 'AT_UNDER_MINUS_TEN', 1),
       (3, 'AT_UNDER_MINUS_TEN', 1),
       (2, 'AT_MINUS_TEN_TO_ZERO', 0.5),
       (3, 'AT_MINUS_TEN_TO_ZERO', 0.5),
       (3, 'WS_TEN_TO_TWENTY', 0.5),
       (2, 'WP_SNOW_SLEET', 1),
       (3, 'WP_SNOW_SLEET', 1),
       (2, 'WP_RAIN', 0.5),
       (3, 'WP_RAIN', 0.5);
