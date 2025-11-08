-- Create schema (optional, you can use public)
CREATE SCHEMA IF NOT EXISTS smart_trashcan;

-- Create trashcans table
CREATE TABLE IF NOT EXISTS smart_trashcan.trashcans (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    location TEXT,
    status TEXT CHECK (status IN ('empty','half','full')) DEFAULT 'empty',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Optional: create a subscriptions table if multiple subscribers per trashcan
CREATE TABLE IF NOT EXISTS smart_trashcan.subscriptions (
    id SERIAL PRIMARY KEY,
    trashcan_id INT REFERENCES smart_trashcan.trashcans(id) ON DELETE CASCADE,
    phone TEXT NOT NULL
);

-- Insert some sample data
INSERT INTO smart_trashcan.trashcans (name, location, status)
VALUES
('Trashcan A', 'Main Street', 'empty'),
('Trashcan B', 'Park', 'half'),
('Trashcan C', 'Library', 'full');
