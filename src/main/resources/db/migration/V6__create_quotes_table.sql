CREATE TABLE quotes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id uuid NOT NULL REFERENCES companies(id),
    customer_id uuid NOT NULL REFERENCES customers(id),
    property_id uuid NOT NULL REFERENCES properties(id),
    price_low INT NOT NULL,
    price_high INT NOT NULL,
    final_price INT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK ( status IN ('PENDING', 'REVIEWED', 'ACCEPTED', 'DECLINED')),
    created_at TIMESTAMP DEFAULT now(),
    viewed_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    accepted_at TIMESTAMP,
    declined_at TIMESTAMP,
    expires_at TIMESTAMP
);