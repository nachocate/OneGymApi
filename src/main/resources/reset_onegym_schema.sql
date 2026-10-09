-- WARNING: This permanently deletes all OneGym tables and their data.
-- It does not execute automatically. Run it manually against the intended
-- PostgreSQL database, then run onegym_schema.sql to create a clean schema.

BEGIN;

-- CASCADE makes this safe regardless of foreign-key creation order.
DROP TABLE IF EXISTS user_refresh_tokens CASCADE;
DROP TABLE IF EXISTS current_plan_activities CASCADE;
DROP TABLE IF EXISTS registers CASCADE;
DROP TABLE IF EXISTS user_tests CASCADE;
DROP TABLE IF EXISTS evaluations CASCADE;
DROP TABLE IF EXISTS block_exercises CASCADE;
DROP TABLE IF EXISTS blocks CASCADE;
DROP TABLE IF EXISTS days CASCADE;
DROP TABLE IF EXISTS weeks CASCADE;
DROP TABLE IF EXISTS user_plans CASCADE;
DROP TABLE IF EXISTS plans CASCADE;
DROP TABLE IF EXISTS news CASCADE;
DROP TABLE IF EXISTS gym_coaches CASCADE;
DROP TABLE IF EXISTS user_gyms CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS gyms CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS coach_types CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS quantity_types CASCADE;
DROP TABLE IF EXISTS plan_types CASCADE;

COMMIT;
