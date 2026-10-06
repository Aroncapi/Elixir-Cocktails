CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(40) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(150) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    reference_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    transport VARCHAR(20) NULL,
    error VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    sent_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_notifications_status (status),
    KEY idx_notifications_reference (reference_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
