-- V1.0.0__Init_Schema.sql

-- 1. Notification Records
CREATE TABLE notifications (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    template_name VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    payload JSONB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_recipient ON notifications(recipient);

-- 2. Notification Attempts (History)
CREATE TABLE notification_attempts (
    id UUID NOT NULL PRIMARY KEY,
    notification_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message VARCHAR(1024),
    provider_response VARCHAR(1024),
    attempted_at TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id)
);

-- 3. Templates
CREATE TABLE templates (
    id UUID NOT NULL PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL,
    locale VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_template_locale UNIQUE (template_name, locale)
);