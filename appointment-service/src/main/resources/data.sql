-- Inserir usuários de teste
INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('admin', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'ADMIN', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('joao.silva', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'DOCTOR', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('maria.souza', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'NURSE', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('ana.lima', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'PATIENT', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('gustavo.lima', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'DOCTOR', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('renato.ds', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'PATIENT', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('erick', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'PATIENT', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

-- Inserir especialidades
INSERT INTO specialties (name, description, average_wait_time)
VALUES ('Cardiologia', 'Especialidade focada em doenças do coração e sistema cardiovascular', 30)
ON CONFLICT (name) DO NOTHING;

INSERT INTO specialties (name, description, average_wait_time)
VALUES ('Dermatologia', 'Especialidade focada em doenças da pele, cabelo e unhas', 20)
ON CONFLICT (name) DO NOTHING;

INSERT INTO specialties (name, description, average_wait_time)
VALUES ('Ginecologia', 'Especialidade focada na saúde da mulher', 25)
ON CONFLICT (name) DO NOTHING;

INSERT INTO specialties (name, description, average_wait_time)
VALUES ('Pediatria', 'Especialidade focada na saúde de crianças e adolescentes', 15)
ON CONFLICT (name) DO NOTHING;

INSERT INTO specialties (name, description, average_wait_time)
VALUES ('Ortopedia', 'Especialidade focada em doenças do sistema musculoesquelético', 35)
ON CONFLICT (name) DO NOTHING;

-- Inserir médicos
INSERT INTO doctors (user_id, name, crm)
SELECT (SELECT id FROM users WHERE username = 'joao.silva'), 'João Silva', 'CRM12345'
WHERE NOT EXISTS (
    SELECT 1 FROM doctors WHERE crm = 'CRM12345'
);

INSERT INTO doctors (user_id, name, crm)
SELECT (SELECT id FROM users WHERE username = 'gustavo.lima'), 'Gustavo Lima', 'CRM66666'
WHERE NOT EXISTS (
    SELECT 1 FROM doctors WHERE crm = 'CRM66666'
);

INSERT INTO doctor_specialties (doctor_id, specialty_id)
SELECT (SELECT id FROM doctors WHERE crm = 'CRM12345'), (SELECT id FROM specialties WHERE name = 'Cardiologia')
WHERE NOT EXISTS (
    SELECT 1 FROM doctor_specialties ds
    WHERE ds.doctor_id = (SELECT id FROM doctors WHERE crm = 'CRM12345')
      AND ds.specialty_id = (SELECT id FROM specialties WHERE name = 'Cardiologia')
);

INSERT INTO doctor_specialties (doctor_id, specialty_id)
SELECT (SELECT id FROM doctors WHERE crm = 'CRM66666'), (SELECT id FROM specialties WHERE name = 'Dermatologia')
WHERE NOT EXISTS (
    SELECT 1 FROM doctor_specialties ds
    WHERE ds.doctor_id = (SELECT id FROM doctors WHERE crm = 'CRM66666')
      AND ds.specialty_id = (SELECT id FROM specialties WHERE name = 'Dermatologia')
);

-- Inserir enfermeiras
INSERT INTO nurses (user_id, name, coren)
SELECT (SELECT id FROM users WHERE username = 'maria.souza'), 'Maria Souza', 'COREN67890'
WHERE NOT EXISTS (
    SELECT 1 FROM nurses WHERE coren = 'COREN67890'
);

-- Inserir pacientes (SEM criticidade - agora está no appointment)
INSERT INTO patients (user_id, name, date_of_birth, document, phone, email, address, engagement_score)
SELECT (SELECT id FROM users WHERE username = 'ana.lima'), 'Paciente Ana Lima', '1990-01-01', 'DOC123456', '11987654321', 'lucas.llopes99@gmail.com', 'Rua das Flores, 123', 100
WHERE NOT EXISTS (
    SELECT 1 FROM patients WHERE document = 'DOC123456'
);

INSERT INTO patients (user_id, name, date_of_birth, document, phone, email, address, engagement_score)
SELECT (SELECT id FROM users WHERE username = 'renato.ds'), 'Paciente Renato DS', '1990-01-01', 'DOC66666', '11987656666', 'lucas.llopes99@gmail.com', 'Rua das Flores, 123', 100
WHERE NOT EXISTS (
    SELECT 1 FROM patients WHERE document = 'DOC66666'
);

INSERT INTO patients (user_id, name, date_of_birth, document, phone, email, address, engagement_score)
SELECT (SELECT id FROM users WHERE username = 'erick'), 'Paciente Erick', '1990-01-01', '12345678902', '11987656666', 'lucas.llopes99@gmail.com', 'Rua das Flores, 123', 100
WHERE NOT EXISTS (
    SELECT 1 FROM patients WHERE document = '12345678902'
);

-- Adicionar mais pacientes para testes
INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('maria.santos', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'PATIENT', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO patients (user_id, name, date_of_birth, document, phone, email, address, engagement_score)
SELECT (SELECT id FROM users WHERE username = 'maria.santos'), 'Maria Santos', '1985-05-15', 'DOC789456', '11987654322', 'lucas.llopes99@gmail.com', 'Av. Paulista, 1000', 120
WHERE NOT EXISTS (
    SELECT 1 FROM patients WHERE document = 'DOC789456'
);

INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('pedro.costa', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'PATIENT', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO patients (user_id, name, date_of_birth, document, phone, email, address, engagement_score)
SELECT (SELECT id FROM users WHERE username = 'pedro.costa'), 'Pedro Costa', '1992-08-20', 'DOC321654', '11987654323', 'lucas.llopes99@gmail.com', 'Rua Augusta, 500', 95
WHERE NOT EXISTS (
    SELECT 1 FROM patients WHERE document = 'DOC321654'
);

INSERT INTO users (username, password, role, enabled, created_at)
VALUES ('julia.oliveira', '$2a$10$qh2BLer14kkg58hXI2nWjOYAHn3/YapvsEPLdBvCnBPhQtXlWtTwu', 'PATIENT', TRUE, NOW())
ON CONFLICT (username) DO NOTHING;

INSERT INTO patients (user_id, name, date_of_birth, document, phone, email, address, engagement_score)
SELECT (SELECT id FROM users WHERE username = 'julia.oliveira'), 'Julia Oliveira', '1988-03-10', 'DOC987321', '11987654324', 'julia.oliveira@exemplo.com', 'Rua Oscar Freire, 200', 110
WHERE NOT EXISTS (
    SELECT 1 FROM patients WHERE document = 'DOC987321'
);

-- Inserir agendamentos COM criticidade (protegidos contra duplicação com ON CONFLICT)
WITH appt AS (SELECT NOW() + INTERVAL '30 days' AS dt)
INSERT INTO appointments (patient_id, doctor_id, nurse_id, appointment_date, status, criticidade, notes, created_at)
VALUES (
    (SELECT id FROM patients WHERE document = '12345678902'),
    (SELECT id FROM doctors WHERE crm = 'CRM66666'),
    (SELECT id FROM nurses WHERE coren = 'COREN67890'),
    (SELECT dt FROM appt),
    'AGENDADO',
    'NORMAL',
    'Consulta de rotina',
    NOW()
)
ON CONFLICT (patient_id, doctor_id, appointment_date) DO NOTHING;

WITH appt AS (SELECT NOW() + INTERVAL '15 days' AS dt)
INSERT INTO appointments (patient_id, doctor_id, nurse_id, appointment_date, status, criticidade, notes, created_at)
VALUES (
    (SELECT id FROM patients WHERE document = 'DOC66666'),
    (SELECT id FROM doctors WHERE crm = 'CRM66666'),
    (SELECT id FROM nurses WHERE coren = 'COREN67890'),
    (SELECT dt FROM appt),
    'AGENDADO',
    'ALTA',
    'Acompanhamento urgente',
    NOW()
)
ON CONFLICT (patient_id, doctor_id, appointment_date) DO NOTHING;

WITH appt AS (SELECT NOW() + INTERVAL '7 days' AS dt)
INSERT INTO appointments (patient_id, doctor_id, nurse_id, appointment_date, status, criticidade, notes, created_at)
VALUES (
    (SELECT id FROM patients WHERE document = 'DOC321654'),
    (SELECT id FROM doctors WHERE crm = 'CRM12345'),
    (SELECT id FROM nurses WHERE coren = 'COREN67890'),
    (SELECT dt FROM appt),
    'AGENDADO',
    'URGENTE',
    'Caso urgente - prioridade máxima',
    NOW()
)
ON CONFLICT (patient_id, doctor_id, appointment_date) DO NOTHING;

-- Inserir agendamentos concluídos para histórico (protegidos contra duplicação com ON CONFLICT)
WITH appt AS (SELECT NOW() - INTERVAL '10 days' AS dt)
INSERT INTO appointments (patient_id, doctor_id, nurse_id, appointment_date, status, criticidade, notes, created_at)
VALUES (
    (SELECT id FROM patients WHERE document = '12345678902'),
    (SELECT id FROM doctors WHERE crm = 'CRM66666'),
    (SELECT id FROM nurses WHERE coren = 'COREN67890'),
    (SELECT dt FROM appt),
    'REALIZADO',
    'NORMAL',
    'Consulta realizada com sucesso',
    (SELECT dt FROM appt)
)
ON CONFLICT (patient_id, doctor_id, appointment_date) DO NOTHING;

WITH appt AS (SELECT NOW() - INTERVAL '20 days' AS dt)
INSERT INTO appointments (patient_id, doctor_id, nurse_id, appointment_date, status, criticidade, notes, created_at)
VALUES (
    (SELECT id FROM patients WHERE document = 'DOC66666'),
    (SELECT id FROM doctors WHERE crm = 'CRM66666'),
    (SELECT id FROM nurses WHERE coren = 'COREN67890'),
    (SELECT dt FROM appt),
    'REALIZADO',
    'ALTA',
    'Consulta realizada - evolução positiva',
    (SELECT dt FROM appt)
)
ON CONFLICT (patient_id, doctor_id, appointment_date) DO NOTHING;

-- Inserir registros médicos para agendamentos concluídos
WITH target AS (
    SELECT a.id AS appt_id,
           a.doctor_id,
           a.patient_id
    FROM appointments a
    WHERE a.patient_id = (SELECT id FROM patients WHERE document = '12345678902')
      AND a.doctor_id = (SELECT id FROM doctors WHERE crm = 'CRM66666')
      AND a.nurse_id = (SELECT id FROM nurses WHERE coren = 'COREN67890')
      AND a.status = 'REALIZADO'
)
INSERT INTO medical_records (
    appointment_id, doctor_id, patient_id,
    diagnosis, prescription, observations,
    created_at, updated_at
)
SELECT
    t.appt_id, t.doctor_id, t.patient_id,
    'Diagnóstico: alta após evolução favorável.',
    'Prescrição: manter orientações e retorno se necessário.',
    'Consulta concluída sem intercorrências.',
    now(), NULL
FROM target t
WHERE t.appt_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM medical_records mr WHERE mr.appointment_id = t.appt_id
);

WITH target AS (
    SELECT a.id AS appt_id,
           a.doctor_id,
           a.patient_id
    FROM appointments a
    WHERE a.patient_id = (SELECT id FROM patients WHERE document = 'DOC66666')
      AND a.doctor_id = (SELECT id FROM doctors WHERE crm = 'CRM66666')
      AND a.nurse_id = (SELECT id FROM nurses WHERE coren = 'COREN67890')
      AND a.status = 'REALIZADO'
)
INSERT INTO medical_records (
    appointment_id, doctor_id, patient_id,
    diagnosis, prescription, observations,
    created_at, updated_at
)
SELECT
    t.appt_id, t.doctor_id, t.patient_id,
    'Diagnóstico: quadro resolvido.',
    'Prescrição: suspender medicação; retornar em 30 dias.',
    'Evolução satisfatória; sem queixas atuais.',
    now(), NULL
FROM target t
WHERE t.appt_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM medical_records mr WHERE mr.appointment_id = t.appt_id
);
