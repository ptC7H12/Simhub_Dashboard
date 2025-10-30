-- PostgreSQL Initialization Script for Racing Stats

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create ENUM types
DO $$ BEGIN
    CREATE TYPE game_type AS ENUM ('ASSETTO_CORSA', 'F1_2024', 'GRAN_TURISMO_7');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE session_type AS ENUM ('PRACTICE', 'QUALIFYING', 'RACE', 'TIME_TRIAL', 'HOTLAP');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE stat_type AS ENUM (
        'BEST_LAP',
        'AVERAGE_LAP',
        'CONSISTENCY_SCORE',
        'TOTAL_LAPS',
        'VALID_LAPS',
        'MAX_SPEED',
        'TRACK_MASTERY',
        'IMPROVEMENT_RATE',
        'SECTOR_BEST_1',
        'SECTOR_BEST_2',
        'SECTOR_BEST_3'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Tables will be created by Hibernate/JPA
-- But we can create indexes here for optimization

-- Function to create indexes if they don't exist
CREATE OR REPLACE FUNCTION create_index_if_not_exists(index_name TEXT, create_statement TEXT)
RETURNS void AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = index_name
    ) THEN
        EXECUTE create_statement;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Wait for tables to be created by Hibernate, then create indexes
-- This will be executed after application startup
DO $$
BEGIN
    -- Give Hibernate time to create tables
    PERFORM pg_sleep(5);

    -- Create indexes for performance optimization
    PERFORM create_index_if_not_exists('idx_laps_session_id',
        'CREATE INDEX idx_laps_session_id ON laps(session_id)');

    PERFORM create_index_if_not_exists('idx_laps_completed_at',
        'CREATE INDEX idx_laps_completed_at ON laps(completed_at)');

    PERFORM create_index_if_not_exists('idx_laps_is_valid',
        'CREATE INDEX idx_laps_is_valid ON laps(is_valid)');

    PERFORM create_index_if_not_exists('idx_sessions_driver_id',
        'CREATE INDEX idx_sessions_driver_id ON sessions(driver_id)');

    PERFORM create_index_if_not_exists('idx_sessions_track_id',
        'CREATE INDEX idx_sessions_track_id ON sessions(track_id)');

    PERFORM create_index_if_not_exists('idx_sessions_started_at',
        'CREATE INDEX idx_sessions_started_at ON sessions(started_at)');

    PERFORM create_index_if_not_exists('idx_sessions_is_active',
        'CREATE INDEX idx_sessions_is_active ON sessions(is_active)');

    PERFORM create_index_if_not_exists('idx_statistics_driver_track',
        'CREATE INDEX idx_statistics_driver_track ON statistics(driver_id, track_id)');

    PERFORM create_index_if_not_exists('idx_statistics_stat_type',
        'CREATE INDEX idx_statistics_stat_type ON statistics(stat_type)');

    PERFORM create_index_if_not_exists('idx_tracks_game',
        'CREATE INDEX idx_tracks_game ON tracks(game)');

    PERFORM create_index_if_not_exists('idx_drivers_name',
        'CREATE INDEX idx_drivers_name ON drivers(name)');

EXCEPTION
    WHEN OTHERS THEN
        -- Tables might not exist yet, that's ok
        NULL;
END $$;

-- Create a view for leaderboard (will be created after tables exist)
CREATE OR REPLACE VIEW v_track_leaderboard AS
SELECT
    d.id as driver_id,
    d.name as driver_name,
    d.display_name,
    t.id as track_id,
    t.name as track_name,
    s.game,
    s.car_model,
    MIN(l.time_millis) as best_lap_time,
    AVG(l.time_millis) FILTER (WHERE l.is_valid) as avg_lap_time,
    COUNT(l.id) FILTER (WHERE l.is_valid) as total_valid_laps,
    STDDEV(l.time_millis) FILTER (WHERE l.is_valid) as lap_time_stddev,
    MAX(l.max_speed) as max_speed
FROM laps l
JOIN sessions s ON l.session_id = s.id
JOIN drivers d ON s.driver_id = d.id
JOIN tracks t ON s.track_id = t.id
WHERE l.is_valid = true
GROUP BY d.id, d.name, d.display_name, t.id, t.name, s.game, s.car_model;

-- Create a view for driver statistics summary
CREATE OR REPLACE VIEW v_driver_statistics AS
SELECT
    d.id as driver_id,
    d.name as driver_name,
    d.display_name,
    COUNT(DISTINCT s.id) as total_sessions,
    COUNT(DISTINCT s.track_id) as tracks_driven,
    COUNT(l.id) as total_laps,
    COUNT(l.id) FILTER (WHERE l.is_valid) as valid_laps,
    MIN(l.time_millis) FILTER (WHERE l.is_valid) as personal_best_lap,
    AVG(l.time_millis) FILTER (WHERE l.is_valid) as avg_lap_time,
    MAX(l.max_speed) as max_speed_achieved
FROM drivers d
LEFT JOIN sessions s ON d.id = s.driver_id
LEFT JOIN laps l ON s.id = l.session_id
GROUP BY d.id, d.name, d.display_name;

-- Grant permissions (if needed)
GRANT ALL PRIVILEGES ON DATABASE racing_stats TO racinguser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO racinguser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO racinguser;

-- Insert some sample data for testing (optional, can be removed)
-- This will only run if tables don't have data yet
DO $$
BEGIN
    -- Check if we have any drivers
    IF NOT EXISTS (SELECT 1 FROM drivers LIMIT 1) THEN
        -- Insert sample driver
        INSERT INTO drivers (id, name, display_name, created_at)
        VALUES (uuid_generate_v4(), 'TestDriver', 'Test Driver', NOW());

        RAISE NOTICE 'Sample data inserted. You can remove this from init.sql';
    END IF;
EXCEPTION
    WHEN undefined_table THEN
        -- Tables don't exist yet, that's ok
        NULL;
END $$;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Racing Stats Database Initialized!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Database: racing_stats';
    RAISE NOTICE 'User: racinguser';
    RAISE NOTICE 'ENUMs created: game_type, session_type, stat_type';
    RAISE NOTICE 'Views created: v_track_leaderboard, v_driver_statistics';
    RAISE NOTICE '========================================';
END $$;