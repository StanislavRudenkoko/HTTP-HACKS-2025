-- =====================================================
-- View: smart_trashcan.trashcan_overview
-- Shows all trashcans with routing-friendly info
-- =====================================================

CREATE OR REPLACE VIEW smart_trashcan.trashcan_overview AS
SELECT
    id,
    name,
    building,
    floor,
    location,
    latitude,
    longitude,
    status,
    route_priority,
    last_updated
FROM
    smart_trashcan.trashcans
ORDER BY
    CASE status
        WHEN 'full' THEN 1
        WHEN 'half' THEN 2
        ELSE 3
    END,
    route_priority DESC,
    building,
    floor;
