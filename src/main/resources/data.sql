INSERT INTO barbershops (name)
SELECT 'Barberia Demo'
WHERE NOT EXISTS (
    SELECT 1 FROM barbershops WHERE name = 'Barberia Demo'
);

INSERT INTO branches (name, address, barbershop_id)
SELECT 'Sucursal Centro', 'Av. Siempre Viva 123', b.id
FROM barbershops b
WHERE b.name = 'Barberia Demo'
  AND NOT EXISTS (
      SELECT 1 FROM branches br WHERE br.name = 'Sucursal Centro' AND br.barbershop_id = b.id
  );

INSERT INTO users (email, password, role, display_name, temporary_password, barbershop_id)
SELECT 'admin@barbershop.com', '$2a$10$U0SQ1Av8zuTrf3rIzPN8lurhQ0DAeVvf1T4OtsY7NrXL1R0HvAFIG', 'ADMIN', 'Admin Demo', FALSE, b.id
FROM barbershops b
WHERE b.name = 'Barberia Demo'
  AND NOT EXISTS (
      SELECT 1 FROM users WHERE email = 'admin@barbershop.com'
  );

UPDATE users
SET role = 'ADMIN',
    display_name = COALESCE(display_name, 'Admin Demo'),
    temporary_password = FALSE,
    barbershop_id = (SELECT id FROM barbershops WHERE name = 'Barberia Demo')
WHERE email = 'admin@barbershop.com';

INSERT INTO user_branches (user_id, branch_id)
SELECT u.id, br.id
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro'
JOIN barbershops b ON b.id = br.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND b.name = 'Barberia Demo'
  AND NOT EXISTS (
      SELECT 1 FROM user_branches ub WHERE ub.user_id = u.id AND ub.branch_id = br.id
  );

INSERT INTO clients (email, phone_number, first_name, last_name, document_number, notes, owner_id, barbershop_id)
SELECT 'juan.perez@email.com', '3513727258', 'Juan', 'Perez', '30111222', 'Prefiere degradado bajo y barba prolija.', u.id, b.id
FROM users u
JOIN barbershops b ON b.id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM clients c WHERE c.phone_number = '3513727258' AND c.barbershop_id = b.id);

INSERT INTO clients (email, phone_number, first_name, last_name, document_number, notes, owner_id, barbershop_id)
SELECT 'martin.gomez@email.com', '3513339999', 'Martin', 'Gomez', '32444555', 'Suele venir despues del trabajo.', u.id, b.id
FROM users u
JOIN barbershops b ON b.id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM clients c WHERE c.phone_number = '3513339999' AND c.barbershop_id = b.id);

INSERT INTO clients (email, phone_number, first_name, last_name, document_number, notes, owner_id, barbershop_id)
SELECT NULL, '3511117777', NULL, 'Antico', NULL, 'Cliente cargado solo con apellido y telefono.', u.id, b.id
FROM users u
JOIN barbershops b ON b.id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM clients c WHERE c.phone_number = '3511117777' AND c.barbershop_id = b.id);

INSERT INTO clients (email, phone_number, first_name, last_name, document_number, notes, owner_id, barbershop_id)
SELECT 'gabiantico@gmail.com', '3512228888', NULL, NULL, NULL, 'Sin nombre guardado, contactar por telefono.', u.id, b.id
FROM users u
JOIN barbershops b ON b.id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM clients c WHERE c.phone_number = '3512228888' AND c.barbershop_id = b.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id, branch_id)
SELECT '2026-05-20 10:00:00', c.id, 'PENDING', 12500.00, u.id, br.id
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN clients c ON c.phone_number = '3513727258' AND c.barbershop_id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-20 10:00:00' AND s.branch_id = br.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id, branch_id)
SELECT '2026-05-20 11:30:00', c.id, 'PENDING', 12500.00, u.id, br.id
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN clients c ON c.phone_number = '3513339999' AND c.barbershop_id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-20 11:30:00' AND s.branch_id = br.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id, branch_id)
SELECT '2026-05-19 18:30:00', c.id, 'COMPLETED', 12500.00, u.id, br.id
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN clients c ON c.phone_number = '3513727258' AND c.barbershop_id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-19 18:30:00' AND s.branch_id = br.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id, branch_id)
SELECT '2026-05-19 19:00:00', c.id, 'CANCELLED', 12500.00, u.id, br.id
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN clients c ON c.phone_number = '3513339999' AND c.barbershop_id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-19 19:00:00' AND s.branch_id = br.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id, branch_id)
SELECT '2026-05-18 16:30:00', c.id, 'COMPLETED', 15000.00, u.id, br.id
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN clients c ON c.phone_number = '3511117777' AND c.barbershop_id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-18 16:30:00' AND s.branch_id = br.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id, branch_id)
SELECT '2026-05-17 17:00:00', c.id, 'CANCELLED', 12500.00, u.id, br.id
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN clients c ON c.phone_number = '3512228888' AND c.barbershop_id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-05-17 17:00:00' AND s.branch_id = br.id);

INSERT INTO shifts (datetime, client_id, status, estimated_amount, owner_id, branch_id)
SELECT '2026-04-25 12:30:00', c.id, 'COMPLETED', 14000.00, u.id, br.id
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN clients c ON c.phone_number = '3512228888' AND c.barbershop_id = u.barbershop_id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM shifts s WHERE s.datetime = '2026-04-25 12:30:00' AND s.branch_id = br.id);

INSERT INTO visits (shift_id, attended_by_user_id, total_amount, currency, payment_status, paid_at, payment_method)
SELECT s.id, u.id, 12500.00, 'ARS', 'PAID', '2026-05-19 18:45:00', 'CASH'
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN shifts s ON s.datetime = '2026-05-19 18:30:00' AND s.branch_id = br.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM visits v WHERE v.shift_id = s.id);

INSERT INTO visits (shift_id, attended_by_user_id, total_amount, currency, payment_status, paid_at, payment_method)
SELECT s.id, u.id, 15000.00, 'ARS', 'PAID', '2026-05-18 16:50:00', 'TRANSFER'
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN shifts s ON s.datetime = '2026-05-18 16:30:00' AND s.branch_id = br.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM visits v WHERE v.shift_id = s.id);

INSERT INTO visits (shift_id, attended_by_user_id, total_amount, currency, payment_status, paid_at, payment_method)
SELECT s.id, u.id, 14000.00, 'ARS', 'PENDING', NULL, NULL
FROM users u
JOIN branches br ON br.name = 'Sucursal Centro' AND br.barbershop_id = u.barbershop_id
JOIN shifts s ON s.datetime = '2026-04-25 12:30:00' AND s.branch_id = br.id
WHERE u.email = 'admin@barbershop.com'
  AND NOT EXISTS (SELECT 1 FROM visits v WHERE v.shift_id = s.id);

UPDATE visits
SET attended_by_user_id = (SELECT id FROM users WHERE email = 'admin@barbershop.com')
WHERE attended_by_user_id IS NULL
  AND shift_id IN (
      SELECT s.id
      FROM shifts s
      JOIN branches br ON br.id = s.branch_id
      JOIN barbershops b ON b.id = br.barbershop_id
      WHERE b.name = 'Barberia Demo'
  );
