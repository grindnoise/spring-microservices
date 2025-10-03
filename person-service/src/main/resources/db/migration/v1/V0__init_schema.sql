CREATE SCHEMA IF NOT EXISTS person;
CREATE SCHEMA IF NOT EXISTS person_history;
-- More generation functions: uuid_generate_v1(), uuid_generate_v3(), uuid_generate_v4(), uuid_generate_v5()
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- This command sets the schema search path for the current database session.
-- PostgreSQL will look for database objects (tables, functions, etc.) in this order: person, person_history, public
-- Without search_path:
--      SELECT * FROM person.users;
--      Fully qualified name
-- With search_path set:
--      SELECT * FROM users;
--      PostgreSQL looks in: person → person_history → public
SET search_path to person, person_history, public;