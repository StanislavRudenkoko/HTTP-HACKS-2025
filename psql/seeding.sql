-- Clear existing data
TRUNCATE TABLE smart_trashcan.trashcans RESTART IDENTITY CASCADE;


-- Quadrant: NW (Buildings 1-6)
INSERT INTO smart_trashcan.trashcans (name, location, status) VALUES
-- NW1, Floor 1, Entrance
('NW1-F1-Entrance-Organic', 'NW1 floor 1, near entrance', 'empty'),
('NW1-F1-Entrance-Mixed', 'NW1 floor 1, near entrance', 'half'),
('NW1-F1-Entrance-Garbage', 'NW1 floor 1, near entrance', 'full'),
-- NW2, Floor 2, Labs
('NW2-F2-Labs-Organic', 'NW2 floor 2, near labs', 'half'),
('NW2-F2-Labs-Mixed', 'NW2 floor 2, near labs', 'empty'),
('NW2-F2-Labs-Garbage', 'NW2 floor 2, near labs', 'full'),
-- NW3, Floor 1, Cafeteria
('NW3-F1-Cafeteria-Organic', 'NW3 floor 1, near cafeteria', 'full'),
('NW3-F1-Cafeteria-Mixed', 'NW3 floor 1, near cafeteria', 'empty'),
('NW3-F1-Cafeteria-Garbage', 'NW3 floor 1, near cafeteria', 'half');

-- Quadrant: NE (Buildings 1-10 for brevity)
INSERT INTO smart_trashcan.trashcans (name, location, status) VALUES
('NE1-F1-Entrance-Organic', 'NE1 floor 1, near entrance', 'empty'),
('NE1-F1-Entrance-Mixed', 'NE1 floor 1, near entrance', 'half'),
('NE1-F1-Entrance-Garbage', 'NE1 floor 1, near entrance', 'full'),
('NE2-F2-Labs-Organic', 'NE2 floor 2, near labs', 'half'),
('NE2-F2-Labs-Mixed', 'NE2 floor 2, near labs', 'full'),
('NE2-F2-Labs-Garbage', 'NE2 floor 2, near labs', 'empty'),
('NE3-F1-Cafeteria-Organic', 'NE3 floor 1, near cafeteria', 'full'),
('NE3-F1-Cafeteria-Mixed', 'NE3 floor 1, near cafeteria', 'empty'),
('NE3-F1-Cafeteria-Garbage', 'NE3 floor 1, near cafeteria', 'half');

-- Quadrant: SW (Buildings 1-5 for brevity)
INSERT INTO smart_trashcan.trashcans (name, location, status) VALUES
('SW1-F1-Entrance-Organic', 'SW1 floor 1, near entrance', 'full'),
('SW1-F1-Entrance-Mixed', 'SW1 floor 1, near entrance', 'half'),
('SW1-F1-Entrance-Garbage', 'SW1 floor 1, near entrance', 'empty'),
('SW2-F2-Labs-Organic', 'SW2 floor 2, near labs', 'half'),
('SW2-F2-Labs-Mixed', 'SW2 floor 2, near labs', 'full'),
('SW2-F2-Labs-Garbage', 'SW2 floor 2, near labs', 'empty');

-- Quadrant: SE (Buildings 1-10 for brevity)
INSERT INTO smart_trashcan.trashcans (name, location, status) VALUES
('SE1-F1-Entrance-Organic', 'SE1 floor 1, near entrance', 'empty'),
('SE1-F1-Entrance-Mixed', 'SE1 floor 1, near entrance', 'half'),
('SE1-F1-Entrance-Garbage', 'SE1 floor 1, near entrance', 'full'),
('SE2-F2-Labs-Organic', 'SE2 floor 2, near labs', 'half'),
('SE2-F2-Labs-Mixed', 'SE2 floor 2, near labs', 'empty'),
('SE2-F2-Labs-Garbage', 'SE2 floor 2, near labs', 'full'),
('SE3-F1-Cafeteria-Organic', 'SE3 floor 1, near cafeteria', 'full'),
('SE3-F1-Cafeteria-Mixed', 'SE3 floor 1, near cafeteria', 'empty'),
('SE3-F1-Cafeteria-Garbage', 'SE3 floor 1, near cafeteria', 'half');