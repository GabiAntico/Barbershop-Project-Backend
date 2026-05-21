INSERT INTO users (email, password, role)
SELECT 'admin@barbershop.com', '$2a$10$U0SQ1Av8zuTrf3rIzPN8lurhQ0DAeVvf1T4OtsY7NrXL1R0HvAFIG', 'USER'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@barbershop.com'
);

INSERT INTO clients (email, phone_number, first_name, last_name, document_number, notes, owner_id)
SELECT 'juan.perez@email.com', '3513727258', 'Juan', 'Perez', '30111222', 'Prefiere degradado bajo y barba prolija.', u.id
FROM users u
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM clients c WHERE c.phone_number = '3513727258' AND c.owner_id = u.id);

INSERT INTO clients (email, phone_number, first_name, last_name, document_number, notes, owner_id)
SELECT 'martin.gomez@email.com', '3513339999', 'Martin', 'Gomez', '32444555', 'Suele venir despues del trabajo.', u.id
FROM users u
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM clients c WHERE c.phone_number = '3513339999' AND c.owner_id = u.id);

INSERT INTO clients (email, phone_number, first_name, last_name, document_number, notes, owner_id)
SELECT NULL, '3511117777', NULL, 'Antico', NULL, 'Cliente cargado solo con apellido y telefono.', u.id
FROM users u
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM clients c WHERE c.phone_number = '3511117777' AND c.owner_id = u.id);

INSERT INTO clients (email, phone_number, first_name, last_name, document_number, notes, owner_id)
SELECT 'gabiantico@gmail.com', '3512228888', NULL, NULL, NULL, 'Sin nombre guardado, contactar por telefono.', u.id
FROM users u
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM clients c WHERE c.phone_number = '3512228888' AND c.owner_id = u.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id)
SELECT '2026-05-20 10:00:00', c.id, 'PENDING', 12500.00, u.id
FROM users u
JOIN clients c ON c.phone_number = '3513727258' AND c.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-20 10:00:00' AND s.owner_id = u.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id)
SELECT '2026-05-20 11:30:00', c.id, 'PENDING', 12500.00, u.id
FROM users u
JOIN clients c ON c.phone_number = '3513339999' AND c.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-20 11:30:00' AND s.owner_id = u.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id)
SELECT '2026-05-19 18:30:00', c.id, 'COMPLETED', 12500.00, u.id
FROM users u
JOIN clients c ON c.phone_number = '3513727258' AND c.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-19 18:30:00' AND s.owner_id = u.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id)
SELECT '2026-05-19 19:00:00', c.id, 'CANCELLED', 12500.00, u.id
FROM users u
JOIN clients c ON c.phone_number = '3513339999' AND c.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-19 19:00:00' AND s.owner_id = u.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id)
SELECT '2026-05-18 16:30:00', c.id, 'COMPLETED', 15000.00, u.id
FROM users u
JOIN clients c ON c.phone_number = '3511117777' AND c.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-18 16:30:00' AND s.owner_id = u.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id)
SELECT '2026-05-17 17:00:00', c.id, 'CANCELLED', 12500.00, u.id
FROM users u
JOIN clients c ON c.phone_number = '3512228888' AND c.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-17 17:00:00' AND s.owner_id = u.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id)
SELECT '2026-04-25 12:30:00', c.id, 'COMPLETED', 14000.00, u.id
FROM users u
JOIN clients c ON c.phone_number = '3512228888' AND c.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-04-25 12:30:00' AND s.owner_id = u.id);

INSERT INTO visits (shift_id, total_amount, currency, payment_status, paid_at, payment_method)
SELECT s.id, 12500.00, 'ARS', 'PAID', '2026-05-19 18:45:00', 'CASH'
FROM users u
JOIN shifts s ON s.datetime = '2026-05-19 18:30:00' AND s.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM visits v WHERE v.shift_id = s.id);

INSERT INTO visits (shift_id, total_amount, currency, payment_status, paid_at, payment_method)
SELECT s.id, 15000.00, 'ARS', 'PAID', '2026-05-18 16:50:00', 'TRANSFER'
FROM users u
JOIN shifts s ON s.datetime = '2026-05-18 16:30:00' AND s.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM visits v WHERE v.shift_id = s.id);

INSERT INTO visits (shift_id, total_amount, currency, payment_status, paid_at, payment_method)
SELECT s.id, 14000.00, 'ARS', 'PENDING', NULL, NULL
FROM users u
JOIN shifts s ON s.datetime = '2026-04-25 12:30:00' AND s.owner_id = u.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM visits v WHERE v.shift_id = s.id);
