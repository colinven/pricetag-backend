CREATE TABLE properties (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    address_full VARCHAR(255) NOT NULL UNIQUE,
    street VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip VARCHAR(10) NOT NULL,
    sqft INT NOT NULL,
    stories INT NOT NULL,
    year_built INT NOT NULL,
    garage_size_cars INT,
    property_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT now()
);