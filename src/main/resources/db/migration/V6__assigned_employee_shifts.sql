ALTER TABLE shifts
    ADD COLUMN assigned_employee_id BIGINT;

UPDATE shifts
SET assigned_employee_id = owner_id
WHERE assigned_employee_id IS NULL;

ALTER TABLE shifts
    ADD CONSTRAINT fk_shifts_assigned_employee
    FOREIGN KEY (assigned_employee_id) REFERENCES users(id);
