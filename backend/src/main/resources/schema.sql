-- Hotel booking portal: MySQL 8 schema. Every statement is safe to run more than once.

CREATE TABLE IF NOT EXISTS hotels (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(120)  NOT NULL,
    city            VARCHAR(80)   NOT NULL,
    address         VARCHAR(200)  NOT NULL,
    description     VARCHAR(500)  NOT NULL,
    stars           TINYINT       NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    total_rooms     INT           NOT NULL,
    CONSTRAINT uq_hotels_name   UNIQUE (name),
    CONSTRAINT ck_hotels_stars  CHECK (stars BETWEEN 1 AND 5),
    CONSTRAINT ck_hotels_price  CHECK (price_per_night > 0),
    CONSTRAINT ck_hotels_rooms  CHECK (total_rooms > 0),
    INDEX idx_hotels_city (city)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS hotel_amenities (
    hotel_id BIGINT      NOT NULL,
    amenity  VARCHAR(60) NOT NULL,
    PRIMARY KEY (hotel_id, amenity),
    CONSTRAINT fk_amenities_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- One row per hotel per night that has at least one booking.
-- A night with no row means "0 rooms booked".
CREATE TABLE IF NOT EXISTS room_availability (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    hotel_id     BIGINT NOT NULL,
    stay_date    DATE   NOT NULL,
    booked_rooms INT    NOT NULL DEFAULT 0,
    CONSTRAINT uq_availability_hotel_date UNIQUE (hotel_id, stay_date),
    CONSTRAINT ck_availability_booked     CHECK (booked_rooms >= 0),
    CONSTRAINT fk_availability_hotel      FOREIGN KEY (hotel_id) REFERENCES hotels (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference    VARCHAR(12)   NOT NULL,
    hotel_id     BIGINT        NOT NULL,
    guest_name   VARCHAR(100)  NOT NULL,
    guest_email  VARCHAR(150)  NOT NULL,
    check_in     DATE          NOT NULL,
    check_out    DATE          NOT NULL,
    nights       INT           NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status       VARCHAR(12)   NOT NULL DEFAULT 'CONFIRMED',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at DATETIME      NULL,
    CONSTRAINT uq_bookings_reference UNIQUE (reference),
    CONSTRAINT ck_bookings_dates     CHECK (check_out > check_in),
    CONSTRAINT ck_bookings_status    CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    CONSTRAINT fk_bookings_hotel     FOREIGN KEY (hotel_id) REFERENCES hotels (id),
    INDEX idx_bookings_lookup (reference, guest_email)
) ENGINE=InnoDB;
