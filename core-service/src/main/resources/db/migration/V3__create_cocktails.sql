CREATE TABLE cocktails (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    ingredients TEXT NOT NULL,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cocktails_name UNIQUE (name)
);

CREATE TABLE cocktail_event_types (
    cocktail_id BIGINT NOT NULL,
    event_type_id BIGINT NOT NULL,
    PRIMARY KEY (cocktail_id, event_type_id),
    CONSTRAINT fk_cet_cocktail FOREIGN KEY (cocktail_id)
        REFERENCES cocktails (id) ON DELETE CASCADE,
    CONSTRAINT fk_cet_event_type FOREIGN KEY (event_type_id)
        REFERENCES event_types (id) ON DELETE CASCADE
);
