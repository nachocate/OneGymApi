-- OneGym test data
-- Run this script after onegym_schema.sql on an empty database.
-- Test password for all users: password
-- The stored value is a BCrypt hash, never the plain password.

BEGIN;

-- ============================================================
-- ROLES
-- ============================================================

INSERT INTO user_roles (id, name) OVERRIDING SYSTEM VALUE VALUES
    (1, 'User'),
    (2, 'Admin'),
    (3, 'SuperAdmin');

-- ============================================================
-- COACH TYPES
-- ============================================================

INSERT INTO coach_types (id, name) OVERRIDING SYSTEM VALUE VALUES
    (1, 'headCoach'),
    (2, 'coach'),
    (3, 'assistant');

-- ============================================================
-- USERS
-- BCrypt hash of: password
-- ============================================================

INSERT INTO users (id, email, password, avatar_url, firstname, lastname, phone, address)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'sofia@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Sofia', 'Martinez', '+54 11 5555-0101', 'Av. Siempre Viva 123'),
    (2, 'lucas@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Lucas', 'Fernandez', '+54 11 5555-0102', 'Av. Siempre Viva 124'),
    (3, 'valentina@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Valentina', 'Gomez', '+54 11 5555-0103', 'Av. Siempre Viva 125'),
    (4, 'admin@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Martin', 'Administrador', '+54 11 5555-0104', 'Av. Siempre Viva 126'),
    (5, 'camila@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Camila', 'Rodriguez', '+54 11 5555-0105', 'Av. Siempre Viva 127'),
    (6, 'tomas@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Tomas', 'Lopez', '+54 11 5555-0106', 'Av. Siempre Viva 128'),
    (7, 'julieta@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Julieta', 'Sanchez', '+54 11 5555-0107', 'Av. Siempre Viva 129'),
    (8, 'diego@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Diego', 'Romero', '+54 11 5555-0108', 'Av. Siempre Viva 130'),
    (9, 'paula@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Paula', 'Diaz', '+54 11 5555-0109', 'Av. Siempre Viva 131'),
    (10, 'nicolas@onegym.test', '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6', NULL, 'Nicolas', 'Silva', '+54 11 5555-0110', 'Av. Siempre Viva 132');

-- ============================================================
-- GYM
-- ============================================================

INSERT INTO gyms (id, name, description, schedule, address, phone, logo_url, banner_url)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'Forze Gym', 'Gimnasio de entrenamiento funcional y fuerza.', 'Lunes a viernes 07:00-22:00; sábados 09:00-14:00', 'Av. Siempre Viva 123', '+54 11 5555-0101', NULL, NULL);

INSERT INTO user_gyms (id, id_user, id_gym, id_user_rol, start_date, end_date)
OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 1, 1, CURRENT_DATE - 90, NULL),
    (2, 2, 1, 1, CURRENT_DATE - 60, NULL),
    (3, 3, 1, 1, CURRENT_DATE - 45, NULL),
    (4, 4, 1, 3, CURRENT_DATE - 365, NULL),
    (5, 5, 1, 2, CURRENT_DATE - 300, NULL),
    (6, 6, 1, 1, CURRENT_DATE - 240, NULL),
    (7, 7, 1, 1, CURRENT_DATE - 180, NULL),
    (8, 8, 1, 1, CURRENT_DATE - 150, NULL),
    (9, 9, 1, 1, CURRENT_DATE - 120, NULL),
    (10, 10, 1, 1, CURRENT_DATE - 90, NULL);

INSERT INTO gym_coaches (id, id_user, id_gym, id_coach_type, start_date, end_date)
OVERRIDING SYSTEM VALUE VALUES
    (1, 2, 1, 1, CURRENT_DATE - 180, NULL),
    (2, 3, 1, 2, CURRENT_DATE - 120, NULL),
    (3, 5, 1, 1, CURRENT_DATE - 300, NULL),
    (4, 6, 1, 2, CURRENT_DATE - 240, NULL),
    (5, 7, 1, 2, CURRENT_DATE - 180, NULL),
    (6, 8, 1, 3, CURRENT_DATE - 150, NULL),
    (7, 9, 1, 3, CURRENT_DATE - 120, NULL),
    -- Historical assignment: it remains in the database but is excluded from /{gymId}/home.
    (8, 10, 1, 3, CURRENT_DATE - 180, CURRENT_DATE - 10);

-- ============================================================
-- NEWS
-- ============================================================

INSERT INTO news (id, id_gym, title, description, image_url, "date")
OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 'Nueva zona de peso libre', 'Incorporamos nuevas barras, discos y mancuernas para tus entrenamientos.', NULL, CURRENT_TIMESTAMP - INTERVAL '12 days'),
    (2, 1, 'Clase especial de movilidad', 'El próximo sábado habrá una clase abierta de movilidad y recuperación.', NULL, CURRENT_TIMESTAMP - INTERVAL '9 days'),
    (3, 1, 'Desafío Forze de agosto', 'Participá del desafío mensual y registrá tus mejores marcas.', NULL, CURRENT_TIMESTAMP - INTERVAL '6 days'),
    (4, 1, 'Nuevos horarios de funcional', 'Agregamos turnos de entrenamiento funcional por la mañana.', NULL, CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (5, 1, 'Mantenimiento de vestuarios', 'Los vestuarios permanecerán cerrados el domingo de 08:00 a 12:00.', NULL, CURRENT_TIMESTAMP - INTERVAL '1 day');

-- ============================================================
-- EXERCISES
-- ============================================================

INSERT INTO exercises (id, name, description, video_url, image_url, id_gym, is_visible_global)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'Sentadilla con barra', 'Sentadilla trasera con barra.', NULL, NULL, NULL, TRUE),
    (2, 'Press de banca', 'Press horizontal con barra.', NULL, NULL, NULL, TRUE),
    (3, 'Peso muerto', 'Peso muerto convencional.', NULL, NULL, NULL, TRUE),
    (4, 'Dominadas', 'Dominadas con agarre prono.', NULL, NULL, NULL, TRUE),
    (5, 'Burpees', 'Burpee completo con salto.', NULL, NULL, NULL, TRUE),
    (6, 'Zancadas', 'Zancadas alternadas con peso corporal.', NULL, NULL, NULL, TRUE),
    (7, 'Remo con mancuerna', 'Remo unilateral con mancuerna.', NULL, NULL, 1, FALSE),
    (8, 'Plancha', 'Plancha isométrica frontal.', NULL, NULL, NULL, TRUE),
    (9, 'Kettlebell swing', 'Balanceo de kettlebell.', NULL, NULL, 1, FALSE),
    (10, 'Carrera', 'Carrera continua o por intervalos.', NULL, NULL, NULL, TRUE);

-- ============================================================
-- QUANTITY TYPES
-- ============================================================

INSERT INTO quantity_types (name) VALUES
    ('SECONDS'),
    ('MINUTES'),
    ('METERS'),
    ('KILOMETERS'),
    ('KG')
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- PLAN TYPES AND PLANS
-- Two plans per user: one generic and one personalized.
-- ============================================================

INSERT INTO plan_types (id, name) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Generica'),
    (2, 'Personalizada');

INSERT INTO plans (id, name, id_gym, id_plan_type, id_plan_root)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'Sofia - Fuerza inicial', 1, 1, NULL),
    (2, 'Sofia - Progresion personal', 1, 2, NULL),
    (3, 'Lucas - Fuerza inicial', 1, 1, NULL),
    (4, 'Lucas - Progresion personal', 1, 2, NULL),
    (5, 'Valentina - Fuerza inicial', 1, 1, NULL),
    (6, 'Valentina - Progresion personal', 1, 2, NULL),
    (7, 'Martin - Fuerza inicial', 1, 1, NULL),
    (8, 'Martin - Progresion personal', 1, 2, NULL),
    (9, 'Camila - Fuerza inicial', 1, 1, NULL),
    (10, 'Camila - Progresion personal', 1, 2, NULL),
    (11, 'Tomas - Fuerza inicial', 1, 1, NULL),
    (12, 'Tomas - Progresion personal', 1, 2, NULL),
    (13, 'Julieta - Fuerza inicial', 1, 1, NULL),
    (14, 'Julieta - Progresion personal', 1, 2, NULL),
    (15, 'Diego - Fuerza inicial', 1, 1, NULL),
    (16, 'Diego - Progresion personal', 1, 2, NULL),
    (17, 'Paula - Fuerza inicial', 1, 1, NULL),
    (18, 'Paula - Progresion personal', 1, 2, NULL),
    (19, 'Nicolas - Fuerza inicial', 1, 1, NULL),
    (20, 'Nicolas - Progresion personal', 1, 2, NULL);

INSERT INTO user_plans (id, id_user, id_plan) OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 1),
    (2, 1, 2),
    (3, 2, 3),
    (4, 2, 4),
    (5, 3, 5),
    (6, 3, 6),
    (7, 4, 7),
    (8, 4, 8),
    (9, 5, 9),
    (10, 5, 10),
    (11, 6, 11),
    (12, 6, 12),
    (13, 7, 13),
    (14, 7, 14),
    (15, 8, 15),
    (16, 8, 16),
    (17, 9, 17),
    (18, 9, 18),
    (19, 10, 19),
    (20, 10, 20);

-- ============================================================
-- WEEKS, DAYS, BLOCKS AND BLOCK EXERCISES
-- Every plan has 2 weeks, every week has 2 days, every day has 2 blocks,
-- and every block has 2 exercises.
-- ============================================================

DO $$
DECLARE
    plan_id_value BIGINT;
    week_id_value BIGINT;
    day_id_value BIGINT;
    block_id_value BIGINT;
    exercise_id_value BIGINT;
    week_number INTEGER;
    day_number INTEGER;
    block_position INTEGER;
    exercise_position INTEGER;
BEGIN
    FOR plan_id_value IN 1..20 LOOP
        FOR week_number IN 1..2 LOOP
            week_id_value := (plan_id_value - 1) * 2 + week_number;

            INSERT INTO weeks (id, number, id_plan)
            OVERRIDING SYSTEM VALUE VALUES (week_id_value, week_number, plan_id_value);

            FOR day_number IN 1..2 LOOP
                day_id_value := (week_id_value - 1) * 2 + day_number;

                INSERT INTO days (id, number, id_week)
                OVERRIDING SYSTEM VALUE VALUES (day_id_value, day_number, week_id_value);

                FOR block_position IN 0..1 LOOP
                    block_id_value := (day_id_value - 1) * 2 + block_position + 1;

                    INSERT INTO blocks (id, position, name, id_day, laps)
                    OVERRIDING SYSTEM VALUE VALUES (
                        block_id_value,
                        block_position,
                        CASE WHEN block_position = 0 THEN 'Fuerza' ELSE 'Condicionamiento' END,
                        day_id_value,
                        CASE WHEN block_position = 0 THEN 4 ELSE 3 END
                    );

                    FOR exercise_position IN 0..1 LOOP
                        exercise_id_value := ((block_id_value + exercise_position - 1) % 10) + 1;

                        IF exercise_position = 0 THEN
                            INSERT INTO block_exercises (
                                id, id_block, id_exercise, id_quantity_type,
                                position, repetitions, quantity
                            ) OVERRIDING SYSTEM VALUE VALUES (
                                (block_id_value - 1) * 2 + exercise_position + 1,
                                block_id_value,
                                exercise_id_value,
                                NULL,
                                exercise_position,
                                8 + (block_id_value % 5),
                                NULL
                            );
                        ELSE
                            INSERT INTO block_exercises (
                                id, id_block, id_exercise, id_quantity_type,
                                position, repetitions, quantity
                            ) OVERRIDING SYSTEM VALUE VALUES (
                                (block_id_value - 1) * 2 + exercise_position + 1,
                                block_id_value,
                                exercise_id_value,
                                ((block_id_value - 1) % 5) + 1,
                                exercise_position,
                                NULL,
                                5 + (block_id_value % 4)
                            );
                        END IF;
                    END LOOP;
                END LOOP;
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

-- ============================================================
-- CURRENT PLAN ACTIVITIES
-- ============================================================

INSERT INTO current_plan_activities
    (id, "date", id_user_plan, id_week_active, id_day_active, is_active)
OVERRIDING SYSTEM VALUE VALUES
    (1, CURRENT_TIMESTAMP, 1, 1, 1, TRUE),
    (2, CURRENT_TIMESTAMP, 3, 5, 9, TRUE),
    (3, CURRENT_TIMESTAMP, 5, 9, 17, TRUE),
    (4, CURRENT_TIMESTAMP, 7, 13, 25, TRUE),
    (5, CURRENT_TIMESTAMP, 9, 17, 33, TRUE),
    (6, CURRENT_TIMESTAMP, 11, 21, 41, TRUE),
    (7, CURRENT_TIMESTAMP, 13, 25, 49, TRUE),
    (8, CURRENT_TIMESTAMP, 15, 29, 57, TRUE),
    (9, CURRENT_TIMESTAMP, 17, 33, 65, TRUE),
    (10, CURRENT_TIMESTAMP, 19, 37, 73, TRUE);

-- ============================================================
-- PROGRAMMED WEIGHTS / REGISTERS
-- ============================================================

INSERT INTO registers (id, id_user_plan, id_exercise, id_block, weight)
OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 1, 1, 40.00),
    (2, 2, 2, 17, 45.00),
    (3, 3, 3, 33, 50.00),
    (4, 4, 4, 49, 55.00),
    (5, 5, 5, 9, 60.00),
    (6, 6, 6, 25, 65.00),
    (7, 7, 7, 41, 70.00),
    (8, 8, 8, 57, 75.00);

-- ============================================================
-- USER TESTS
-- ============================================================

INSERT INTO evaluations (id, name, description, creation_date)
OVERRIDING SYSTEM VALUE VALUES
    (1, 'Evaluacion inicial', 'Mediciones de ingreso de los usuarios.', CURRENT_TIMESTAMP - INTERVAL '30 days'),
    (2, 'Evaluacion de seguimiento', 'Mediciones para controlar el progreso.', CURRENT_TIMESTAMP - INTERVAL '10 days');

INSERT INTO user_tests (
    id, id_exercise, id_user, id_evaluation, id_quantity_type,
    repetitions, quantity, "date", description
) OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 1, 1, 5, 5, 60.00, CURRENT_TIMESTAMP - INTERVAL '30 days', 'Test inicial de sentadilla.'),
    (2, 2, 1, 1, 5, 5, 40.00, CURRENT_TIMESTAMP - INTERVAL '25 days', 'Test inicial de press de banca.'),
    (3, 3, 2, 1, 5, 5, 80.00, CURRENT_TIMESTAMP - INTERVAL '20 days', 'Test inicial de peso muerto.'),
    (4, 4, 2, 1, NULL, 8, NULL, CURRENT_TIMESTAMP - INTERVAL '15 days', 'Cantidad maxima de dominadas.'),
    (5, 5, 3, 2, NULL, 20, NULL, CURRENT_TIMESTAMP - INTERVAL '12 days', 'Test de burpees en un minuto.'),
    (6, 6, 3, 2, 5, 10, 20.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'Test de zancadas con carga.'),
    (7, 9, 4, 2, 5, 10, 16.00, CURRENT_TIMESTAMP - INTERVAL '8 days', 'Test de kettlebell swing.'),
    (8, 10, 4, 2, 4, NULL, 5.00, CURRENT_TIMESTAMP - INTERVAL '5 days', 'Test de carrera de cinco kilometros.');

-- Keep identity sequences ahead of the explicit test IDs.
SELECT setval(pg_get_serial_sequence('user_roles', 'id'), COALESCE((SELECT MAX(id) FROM user_roles), 1), true);
SELECT setval(pg_get_serial_sequence('coach_types', 'id'), COALESCE((SELECT MAX(id) FROM coach_types), 1), true);
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('gyms', 'id'), COALESCE((SELECT MAX(id) FROM gyms), 1), true);
SELECT setval(pg_get_serial_sequence('user_gyms', 'id'), COALESCE((SELECT MAX(id) FROM user_gyms), 1), true);
SELECT setval(pg_get_serial_sequence('gym_coaches', 'id'), COALESCE((SELECT MAX(id) FROM gym_coaches), 1), true);
SELECT setval(pg_get_serial_sequence('news', 'id'), COALESCE((SELECT MAX(id) FROM news), 1), true);
SELECT setval(pg_get_serial_sequence('exercises', 'id'), COALESCE((SELECT MAX(id) FROM exercises), 1), true);
SELECT setval(pg_get_serial_sequence('quantity_types', 'id'), COALESCE((SELECT MAX(id) FROM quantity_types), 1), true);
SELECT setval(pg_get_serial_sequence('plan_types', 'id'), COALESCE((SELECT MAX(id) FROM plan_types), 1), true);
SELECT setval(pg_get_serial_sequence('plans', 'id'), COALESCE((SELECT MAX(id) FROM plans), 1), true);
SELECT setval(pg_get_serial_sequence('user_plans', 'id'), COALESCE((SELECT MAX(id) FROM user_plans), 1), true);
SELECT setval(pg_get_serial_sequence('weeks', 'id'), COALESCE((SELECT MAX(id) FROM weeks), 1), true);
SELECT setval(pg_get_serial_sequence('days', 'id'), COALESCE((SELECT MAX(id) FROM days), 1), true);
SELECT setval(pg_get_serial_sequence('blocks', 'id'), COALESCE((SELECT MAX(id) FROM blocks), 1), true);
SELECT setval(pg_get_serial_sequence('block_exercises', 'id'), COALESCE((SELECT MAX(id) FROM block_exercises), 1), true);
SELECT setval(pg_get_serial_sequence('current_plan_activities', 'id'), COALESCE((SELECT MAX(id) FROM current_plan_activities), 1), true);
SELECT setval(pg_get_serial_sequence('registers', 'id'), COALESCE((SELECT MAX(id) FROM registers), 1), true);
SELECT setval(pg_get_serial_sequence('evaluations', 'id'), COALESCE((SELECT MAX(id) FROM evaluations), 1), true);
SELECT setval(pg_get_serial_sequence('user_tests', 'id'), COALESCE((SELECT MAX(id) FROM user_tests), 1), true);

COMMIT;
