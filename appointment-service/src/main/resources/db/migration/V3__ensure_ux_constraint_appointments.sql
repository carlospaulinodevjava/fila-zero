-- Flyway migration: ensure there is a named UNIQUE CONSTRAINT for (patient_id, doctor_id, appointment_date)
-- If an index with the same name exists, drop it and replace with a proper constraint (idempotent)

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ux_appointments_patient_doctor_date'
    ) THEN
        -- If an index exists with this name, drop it (safe even if created previously)
        IF EXISTS (
            SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE c.relname = 'ux_appointments_patient_doctor_date' AND c.relkind = 'i'
        ) THEN
            EXECUTE 'DROP INDEX IF EXISTS ux_appointments_patient_doctor_date';
        END IF;
        -- Add named unique constraint
        EXECUTE 'ALTER TABLE appointments ADD CONSTRAINT ux_appointments_patient_doctor_date UNIQUE (patient_id, doctor_id, appointment_date)';
    END IF;
END$$;

