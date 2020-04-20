CREATE TABLE IF NOT EXISTS product (
    id serial PRIMARY KEY,
    name VARCHAR NOT NULL,
    price NUMERIC (5, 2)
);