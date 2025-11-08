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

    -- Fill level as percentage (0 to 100)
    status INT CHECK (status >= 0 AND status <= 100) DEFAULT 0,

    -- Optional routing priority (for route optimization)
    route_priority INT DEFAULT 0,

    -- Timestamp management
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
--  Subscriptions Table
-- ===========================
CREATE TABLE smart_trashcan.subscriptions (
    trashcan_id INT REFERENCES smart_trashcan.trashcans(id) ON DELETE CASCADE,
    phone TEXT NOT NULL,
	PRIMARY KEY (trashcan_id, phone)
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
