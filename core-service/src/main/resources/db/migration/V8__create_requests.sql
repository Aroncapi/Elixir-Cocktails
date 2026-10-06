CREATE TABLE requests (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    folio VARCHAR(20) NOT NULL,
    public_token VARCHAR(64) NOT NULL,
    event_type_id BIGINT NOT NULL,
    event_date DATE NOT NULL,
    event_time TIME,
    guests INT NOT NULL,
    duration_hours INT NOT NULL,
    location VARCHAR(200),
    notes VARCHAR(1000),
    level VARCHAR(20) NOT NULL DEFAULT 'BASE',
    extra_bartenders INT NOT NULL DEFAULT 0,
    base_amount DECIMAL(12,2) NOT NULL,
    premium_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    personal_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    client_name VARCHAR(120) NOT NULL,
    client_email VARCHAR(150),
    client_whatsapp VARCHAR(30),
    client_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_requests_folio UNIQUE (folio),
    CONSTRAINT uq_requests_token UNIQUE (public_token),
    CONSTRAINT fk_requests_event_type FOREIGN KEY (event_type_id)
        REFERENCES event_types (id),
    CONSTRAINT fk_requests_client FOREIGN KEY (client_id)
        REFERENCES clients (id) ON DELETE SET NULL
);

CREATE TABLE request_cocktails (
    request_id BIGINT NOT NULL,
    cocktail_id BIGINT NOT NULL,
    PRIMARY KEY (request_id, cocktail_id),
    CONSTRAINT fk_rc_request FOREIGN KEY (request_id)
        REFERENCES requests (id) ON DELETE CASCADE,
    CONSTRAINT fk_rc_cocktail FOREIGN KEY (cocktail_id)
        REFERENCES cocktails (id) ON DELETE CASCADE
);
