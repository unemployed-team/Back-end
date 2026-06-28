CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

CREATE TABLE IF NOT EXISTS official_price (
                                              building_id    BIGINT PRIMARY KEY REFERENCES building_master(building_id),
    official_price BIGINT      NOT NULL,
    price_type     VARCHAR(10) NOT NULL,
    base_year      VARCHAR(4)  NOT NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW()
    );

CREATE TABLE IF NOT EXISTS auction_history (
                                               auction_id         BIGSERIAL PRIMARY KEY,
                                               building_id        BIGINT      NOT NULL REFERENCES building_master(building_id),
    court_name         VARCHAR(100),
    case_number        VARCHAR(100),
    auction_status     VARCHAR(30),
    auction_start_date DATE,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
    );