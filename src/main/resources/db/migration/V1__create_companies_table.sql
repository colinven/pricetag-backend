CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE companies (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    service_radius_miles INT,
    service_area_lat NUMERIC(9,6),
    service_area_lng NUMERIC(9,6),
    created_at TIMESTAMP DEFAULT now(),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL
);