CREATE UNIQUE INDEX accounts_email_normalized_uk ON accounts (lower(btrim(email)));

CREATE TABLE customers (
    account_id BIGINT PRIMARY KEY REFERENCES accounts(id),
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    profile_image_url VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE store_owners (
    account_id BIGINT PRIMARY KEY REFERENCES accounts(id),
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE riders (
    account_id BIGINT PRIMARY KEY REFERENCES accounts(id),
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    vehicle_info VARCHAR(255),
    approval_status VARCHAR(255) NOT NULL
        CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    reject_reason VARCHAR(255),
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

