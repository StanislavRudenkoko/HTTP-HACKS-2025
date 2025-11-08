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
    last_updated,
    CASE
        WHEN status::INT >= 75 THEN 'full'
        WHEN status::INT >= 40 THEN 'half'
        ELSE 'empty'
    END AS status_text
FROM smart_trashcan.trashcans
ORDER BY
    status::INT DESC,  -- fullest first
    route_priority DESC,
    building,
    floor;
