CREATE TABLE service_tiers (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    tier_type VARCHAR(30) NOT NULL,
    price DECIMAL(12,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_service_tiers_code UNIQUE (code)
);
