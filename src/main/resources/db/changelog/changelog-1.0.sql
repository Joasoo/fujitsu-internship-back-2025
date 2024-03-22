--liquibase formatted sql

--changeset Markus Joasoo:20-03-2024 Create initial tables
CREATE TABLE IF NOT EXISTS city (
    id BIGINT PRIMARY KEY,
    name CHARACTER VARYING NOT NULL,
    wmo_code INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicle (
    id BIGINT PRIMARY KEY,
    type CHARACTER VARYING NOT NULL,

    CONSTRAINT ak_vehicle_type UNIQUE(type)
);

CREATE TABLE IF NOT EXISTS regional_base_fee (
    id BIGINT PRIMARY KEY,
    city_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    fee_amount NUMERIC NOT NULL,

    CONSTRAINT fk_regional_base_fee_city_id FOREIGN KEY (city_id) REFERENCES city (id),
    CONSTRAINT fk_regional_base_fee_vehicle_id FOREIGN KEY (vehicle_id) REFERENCES vehicle (id),
    CONSTRAINT chk_regional_base_fee_fee_amount CHECK (fee_amount > 0)
);

CREATE TABLE IF NOT EXISTS code_item (
    code CHARACTER VARYING PRIMARY KEY,
    code_class CHARACTER VARYING NOT NULL
);

CREATE TABLE IF NOT EXISTS extra_fee (
    id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    code_item CHARACTER VARYING NOT NULL,
    fee_amount NUMERIC NOT NULL,

    CONSTRAINT fk_extra_fee_vehicle_id FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT fk_extra_fee_code_item FOREIGN KEY (code_item) REFERENCES code_item(code),
    CONSTRAINT ak_extra_fee_vehicle_id_code UNIQUE (vehicle_id, code_item),
    CONSTRAINT chk_extra_fee_fee_amount CHECK (fee_amount > 0)
);

CREATE TABLE IF NOT EXISTS work_prohibition (
    id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    code_item CHARACTER VARYING NOT NULL,

    CONSTRAINT fk_work_prohibition_vehicle_id FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT fk_work_prohibition_code_item FOREIGN KEY (code_item) REFERENCES code_item(code)
);

CREATE TABLE IF NOT EXISTS weather_measurement (
    id BIGINT PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    station_name CHARACTER VARYING NOT NULL,
    wmo_code INTEGER NOT NULL,
    air_temperature DOUBLE PRECISION,
    wind_speed DOUBLE PRECISION,
    phenomenon CHARACTER VARYING,

    CONSTRAINT ak_weather_measurement_timestamp_wmo_code UNIQUE (timestamp, wmo_code)
);
