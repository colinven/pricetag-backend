CREATE TABLE company_pricing (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id uuid NOT NULL UNIQUE REFERENCES companies(id),
    base_sqft_price NUMERIC(5,4),
    story_multiplier NUMERIC(5,4),
    minimum_price INT,
    price_range_buffer INT,
    quote_expiry_days INT NOT NULL DEFAULT 14,
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);