CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS specialties (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    average_wait_time INTEGER
);

CREATE TABLE IF NOT EXISTS doctors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100),
    crm VARCHAR(30) UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS nurses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    coren VARCHAR(30) UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    document VARCHAR(50) UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    engagement_score INTEGER DEFAULT 100,
    total_appointments INTEGER DEFAULT 0,
    missed_appointments INTEGER DEFAULT 0,
    cancelled_appointments INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    nurse_id BIGINT,
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AGENDADO',
    criticidade VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    sent_at TIMESTAMP DEFAULT NULL,
    confirmation_deadline TIMESTAMP DEFAULT NULL,
    reallocation_attempts INTEGER DEFAULT 0,
    FOREIGN KEY (patient_id) REFERENCES patients (id),
    FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    FOREIGN KEY (nurse_id) REFERENCES nurses (id)
);

CREATE TABLE IF NOT EXISTS medical_records (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT UNIQUE NOT NULL,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    diagnosis TEXT,
    prescription TEXT,
    observations TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    tracking_token VARCHAR(36) UNIQUE NOT NULL,
    appointment_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    send_at TIMESTAMP,
    responded_at TIMESTAMP,
    expires_at TIMESTAMP,
    fl_expired BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE TABLE IF NOT EXISTS waiting_queues (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    specialty_id BIGINT NOT NULL,
    preferred_doctor_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'AGUARDANDO',
    priority_score INTEGER NOT NULL,
    criticidade VARCHAR(20),
    entered_at TIMESTAMP NOT NULL,
    notified_at TIMESTAMP,
    estimated_wait_time INTEGER,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients (id),
    FOREIGN KEY (specialty_id) REFERENCES specialties (id),
    FOREIGN KEY (preferred_doctor_id) REFERENCES doctors (id)
);

CREATE INDEX IF NOT EXISTS idx_waiting_queue_specialty_status ON waiting_queues (specialty_id, status);
CREATE INDEX IF NOT EXISTS idx_waiting_queue_priority_score ON waiting_queues (priority_score DESC);
CREATE INDEX IF NOT EXISTS idx_notification_tracking_token ON notification (tracking_token);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments (status);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments (appointment_date);
