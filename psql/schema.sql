-- ===========================
--  Reset and Recreate Schema
-- ===========================
DROP SCHEMA IF EXISTS smart_trashcan CASCADE;
CREATE SCHEMA IF NOT EXISTS smart_trashcan;

-- ===========================
--  Trashcans Table
-- ===========================
CREATE TABLE smart_trashcan.trashcans (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    
    -- Human-readable location (for display and backward compatibility)
    location TEXT,
    
    -- Machine-readable fields for scripts and routing
    building TEXT,
    floor INT,
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),

    -- Trashcan fill status
    status TEXT CHECK (status IN ('empty', 'half', 'full')) DEFAULT 'empty',

    -- Optional routing priority (for route optimization)
    route_priority INT DEFAULT 0,

    -- Timestamp management
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
--  Subscriptions Table
-- ===========================
CREATE TABLE smart_trashcan.subscriptions (
    id SERIAL PRIMARY KEY,
    trashcan_id INT REFERENCES smart_trashcan.trashcans(id) ON DELETE CASCADE,
    phone TEXT NOT NULL
);

-- ===========================
--  Trigger to auto-update timestamp
-- ===========================
CREATE OR REPLACE FUNCTION update_last_updated()
RETURNS TRIGGER AS $$
BEGIN
  NEW.last_updated = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_last_updated
BEFORE UPDATE ON smart_trashcan.trashcans
FOR EACH ROW
EXECUTE FUNCTION update_last_updated();
