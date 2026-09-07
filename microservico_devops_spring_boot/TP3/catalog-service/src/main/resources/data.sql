INSERT INTO products (name, price)
VALUES
    ('Spring Reactive Fundamentals', 89.90),
    ('Docker para Microsserviços', 64.90)
ON CONFLICT DO NOTHING;
