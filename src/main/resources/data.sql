INSERT INTO users (email, password, role)
SELECT 'admin@barbershop.com', '$2a$10$U0SQ1Av8zuTrf3rIzPN8lurhQ0DAeVvf1T4OtsY7NrXL1R0HvAFIG', 'USER'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@barbershop.com'
);
