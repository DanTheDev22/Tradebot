CREATE SEQUENCE user_seq START 1;

CREATE TABLE users (
    user_id BIGINT PRIMARY KEY DEFAULT nextval('user_seq'),
    user_name VARCHAR(255) NOT NULL,
    has_access BOOLEAN NOT NULL DEFAULT FALSE,
    registered_at TIMESTAMP NOT NULL
);

CREATE TABLE crypto_alert (
    alert_id SERIAL PRIMARY KEY,
    telegram_user_id BIGINT,
    symbol VARCHAR(255),
    target_price DOUBLE PRECISION,
    notified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE invoices (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT,
    payload VARCHAR(255),
    provider VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);
