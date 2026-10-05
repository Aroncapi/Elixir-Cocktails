CREATE TABLE event_types (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_event_types_code UNIQUE (code)
);
