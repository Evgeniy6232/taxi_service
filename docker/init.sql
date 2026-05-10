-- Схемы
CREATE SCHEMA users;
CREATE SCHEMA trips;
CREATE SCHEMA notifications;

-- Пользователи с паролями
CREATE USER user_service WITH PASSWORD 'user_pass';
CREATE USER trip_service WITH PASSWORD 'trip_pass';
CREATE USER notification_service WITH PASSWORD 'notif_pass';

-- Права: каждый пользователь видит только свою схему
GRANT ALL ON SCHEMA users TO user_service;
GRANT ALL ON SCHEMA trips TO trip_service;
GRANT ALL ON SCHEMA notifications TO notification_service;

-- Путь по умолчанию: таблицы создаются в своей схеме
ALTER USER user_service SET search_path TO users;
ALTER USER trip_service SET search_path TO trips;
ALTER USER notification_service SET search_path TO notifications;

-- Схема users
CREATE TABLE users.users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    user_type   VARCHAR(20) NOT NULL,
    ref_id      BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE users.passengers (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    phone       VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE users.drivers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    license_number  VARCHAR(50),
    status          VARCHAR(20) DEFAULT 'FREE',
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_drivers_status ON users.drivers(status);

-- Схема trips
CREATE TABLE trips.trips (
    id              BIGSERIAL PRIMARY KEY,
    passenger_id    BIGINT NOT NULL,
    driver_id       BIGINT,
    origin          VARCHAR(500) NOT NULL,
    origin_lat      DOUBLE PRECISION NOT NULL,
    origin_lng      DOUBLE PRECISION NOT NULL,
    destination     VARCHAR(500) NOT NULL,
    dest_lat        DOUBLE PRECISION NOT NULL,
    dest_lng        DOUBLE PRECISION NOT NULL,
    status          VARCHAR(30) DEFAULT 'WAITING',
    price           DECIMAL(10,2),
    rating          INT,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_trips_status ON trips.trips(status);
CREATE INDEX idx_trips_passenger ON trips.trips(passenger_id);
CREATE INDEX idx_trips_driver ON trips.trips(driver_id);

-- Схема notifications
CREATE TABLE notifications.notification_tasks (
    id              BIGSERIAL PRIMARY KEY,
    trip_id         BIGINT NOT NULL,
    recipient_id    BIGINT NOT NULL,
    recipient_type  VARCHAR(20) NOT NULL,
    type            VARCHAR(50) NOT NULL,
    message         TEXT NOT NULL,
    status          VARCHAR(20) DEFAULT 'PENDING',
    is_read         BOOLEAN DEFAULT FALSE,
    attempts        INT DEFAULT 0,
    version         BIGINT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_nt_status ON notifications.notification_tasks(status);
CREATE INDEX idx_nt_recipient ON notifications.notification_tasks(recipient_id);
