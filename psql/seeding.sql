-- ===========================
-- Set schema search path
-- ===========================
SET search_path TO smart_trashcan;

-- ===========================
-- Clear existing data
-- ===========================
-- Check if table exists, then clear it
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables 
               WHERE table_schema = 'smart_trashcan' 
               AND table_name = 'trashcans') THEN
        TRUNCATE TABLE smart_trashcan.trashcans RESTART IDENTITY CASCADE;
        RAISE NOTICE 'Table truncated successfully';
    ELSE
        RAISE EXCEPTION 'Table smart_trashcan.trashcans does not exist. Please run schema.sql first.';
    END IF;
END $$;

-- ===========================
-- Seed Data by Quadrant
-- ===========================

-- Quadrant: NW (Buildings 1–6)
INSERT INTO smart_trashcan.trashcans (name, location, building, floor, latitude, longitude, status) VALUES
('NW1-F1-Entrance-Organic', 'NW1 floor 1, near entrance', 'NW1', 1, 49.2820, -123.1207, 0),
('NW1-F1-Entrance-Mixed', 'NW1 floor 1, near entrance', 'NW1', 1, 49.2821, -123.1208, 50),
('NW1-F1-Entrance-Garbage', 'NW1 floor 1, near entrance', 'NW1', 1, 49.2822, -123.1209, 100),
('NW2-F2-Labs-Organic', 'NW2 floor 2, near labs', 'NW2', 2, 49.2830, -123.1215, 50),
('NW2-F2-Labs-Mixed', 'NW2 floor 2, near labs', 'NW2', 2, 49.2831, -123.1216, 0),
('NW2-F2-Labs-Garbage', 'NW2 floor 2, near labs', 'NW2', 2, 49.2832, -123.1217, 100),
('NW3-F1-Cafeteria-Organic', 'NW3 floor 1, near cafeteria', 'NW3', 1, 49.2840, -123.1225, 100),
('NW3-F1-Cafeteria-Mixed', 'NW3 floor 1, near cafeteria', 'NW3', 1, 49.2841, -123.1226, 0),
('NW3-F1-Cafeteria-Garbage', 'NW3 floor 1, near cafeteria', 'NW3', 1, 49.2842, -123.1227, 50);

-- Quadrant: NE (Buildings 1–10 for brevity)
INSERT INTO smart_trashcan.trashcans (name, location, building, floor, latitude, longitude, status) VALUES
('NE1-F1-Entrance-Organic', 'NE1 floor 1, near entrance', 'NE1', 1, 49.2820, -123.1007, 0),
('NE1-F1-Entrance-Mixed', 'NE1 floor 1, near entrance', 'NE1', 1, 49.2821, -123.1008, 50),
('NE1-F1-Entrance-Garbage', 'NE1 floor 1, near entrance', 'NE1', 1, 49.2822, -123.1009, 100),
('NE2-F2-Labs-Organic', 'NE2 floor 2, near labs', 'NE2', 2, 49.2830, -123.1015, 50),
('NE2-F2-Labs-Mixed', 'NE2 floor 2, near labs', 'NE2', 2, 49.2831, -123.1016, 100),
('NE2-F2-Labs-Garbage', 'NE2 floor 2, near labs', 'NE2', 2, 49.2832, -123.1017, 0),
('NE3-F1-Cafeteria-Organic', 'NE3 floor 1, near cafeteria', 'NE3', 1, 49.2840, -123.1025, 100),
('NE3-F1-Cafeteria-Mixed', 'NE3 floor 1, near cafeteria', 'NE3', 1, 49.2841, -123.1026, 0),
('NE3-F1-Cafeteria-Garbage', 'NE3 floor 1, near cafeteria', 'NE3', 1, 49.2842, -123.1027, 50);

-- Quadrant: SW (Buildings 1–5 for brevity)
INSERT INTO smart_trashcan.trashcans (name, location, building, floor, latitude, longitude, status) VALUES
('SW1-F1-Entrance-Organic', 'SW1 floor 1, near entrance', 'SW1', 1, 49.2700, -123.1307, 100),
('SW1-F1-Entrance-Mixed', 'SW1 floor 1, near entrance', 'SW1', 1, 49.2701, -123.1308, 50),
('SW1-F1-Entrance-Garbage', 'SW1 floor 1, near entrance', 'SW1', 1, 49.2702, -123.1309, 0),
('SW2-F2-Labs-Organic', 'SW2 floor 2, near labs', 'SW2', 2, 49.2710, -123.1315, 50),
('SW2-F2-Labs-Mixed', 'SW2 floor 2, near labs', 'SW2', 2, 49.2711, -123.1316, 100),
('SW2-F2-Labs-Garbage', 'SW2 floor 2, near labs', 'SW2', 2, 49.2712, -123.1317, 0);

-- Quadrant: SE (Buildings 1–10 for brevity)
INSERT INTO smart_trashcan.trashcans (name, location, building, floor, latitude, longitude, status) VALUES
('SE1-F1-Entrance-Organic', 'SE1 floor 1, near entrance', 'SE1', 1, 49.2700, -123.1007, 0),
('SE1-F1-Entrance-Mixed', 'SE1 floor 1, near entrance', 'SE1', 1, 49.2701, -123.1008, 50),
('SE1-F1-Entrance-Garbage', 'SE1 floor 1, near entrance', 'SE1', 1, 49.2702, -123.1009, 100),
('SE2-F2-Labs-Organic', 'SE2 floor 2, near labs', 'SE2', 2, 49.2710, -123.1015, 50),
('SE2-F2-Labs-Mixed', 'SE2 floor 2, near labs', 'SE2', 2, 49.2711, -123.1016, 0),
('SE2-F2-Labs-Garbage', 'SE2 floor 2, near labs', 'SE2', 2, 49.2712, -123.1017, 100),
('SE3-F1-Cafeteria-Organic', 'SE3 floor 1, near cafeteria', 'SE3', 1, 49.2720, -123.1025, 100),
('SE3-F1-Cafeteria-Mixed', 'SE3 floor 1, near cafeteria', 'SE3', 1, 49.2721, -123.1026, 0),
('SE3-F1-Cafeteria-Garbage', 'SE3 floor 1, near cafeteria', 'SE3', 1, 49.2722, -123.1027, 50);

-- ===========================
-- Verify data insertion
-- ===========================
SELECT COUNT(*) AS total_inserted FROM smart_trashcan.trashcans;
