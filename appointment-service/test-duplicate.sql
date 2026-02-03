-- Script para testar se os appointments são realmente únicos
-- Execute este script no banco após iniciar a aplicação múltiplas vezes

-- Verificar se há duplicatas de appointments
SELECT
    patient_id,
    doctor_id,
    appointment_date,
    COUNT(*) as total_registros
FROM appointments
GROUP BY patient_id, doctor_id, appointment_date
HAVING COUNT(*) > 1;

-- Se retornar vazio, não há duplicatas! ✅

-- Ver todos os appointments agrupados
SELECT
    p.document as patient_doc,
    d.crm as doctor_crm,
    a.appointment_date,
    a.status,
    a.criticidade,
    COUNT(*) OVER (PARTITION BY a.patient_id, a.doctor_id, a.appointment_date) as duplicates
FROM appointments a
JOIN patients p ON a.patient_id = p.id
JOIN doctors d ON a.doctor_id = d.id
ORDER BY p.document, d.crm, a.appointment_date;

