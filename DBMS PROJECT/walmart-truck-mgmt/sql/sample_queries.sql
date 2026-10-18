-- ============================================================
-- Walmart Warehouse Truck Management System – Sample Queries
-- Run these in MySQL Workbench against walmart_truck_db
-- ============================================================

USE walmart_truck_db;

-- ============================================================
-- 1. JOIN: Gate overview – gates with current truck and dispatch info
--    Shows every gate, the truck currently at it (if any),
--    and the active dispatch operation details.
-- ============================================================
SELECT
    g.gate_number,
    g.gate_status,
    t.license_plate,
    t.driver_name,
    d.operation_type,
    d.load_weight,
    d.status        AS dispatch_status,
    d.started_at,
    d.expected_minutes,
    lt.name         AS load_type
FROM warehouse_gate g
LEFT JOIN truck t             ON g.current_truck_id = t.id
LEFT JOIN dispatch_operation d ON d.gate_id = g.id
                               AND d.status IN ('IN_PROGRESS','IN_CLEANING','ASSIGNED')
LEFT JOIN load_type lt        ON d.load_type_id = lt.id
ORDER BY g.gate_number;


-- ============================================================
-- 2. JOIN: Dispatch history – completed dispatches with all details
--    Lists every completed dispatch along with the truck, gate,
--    and load type used.
-- ============================================================
SELECT
    d.id            AS dispatch_id,
    t.license_plate,
    t.driver_name,
    g.gate_number,
    lt.name         AS load_type,
    d.operation_type,
    d.load_weight,
    d.destination_distance,
    d.is_emergency,
    d.requested_at,
    d.started_at,
    d.completed_at,
    d.expected_minutes
FROM dispatch_operation d
JOIN truck t          ON d.truck_id     = t.id
JOIN warehouse_gate g ON d.gate_id      = g.id
JOIN load_type lt     ON d.load_type_id = lt.id
WHERE d.status = 'COMPLETED'
ORDER BY d.completed_at DESC;


-- ============================================================
-- 3. JOIN: Gates that can handle a specific load type
--    Finds all gates whose allowed load types include 'FOOD'.
-- ============================================================
SELECT
    g.gate_number,
    g.gate_status,
    lt.name AS supported_load_type
FROM warehouse_gate g
JOIN gate_load_type glt ON g.id = glt.gate_id
JOIN load_type lt       ON glt.load_type_id = lt.id
WHERE lt.name = 'FOOD';


-- ============================================================
-- 4. JOIN: Cleaning report – all cleaning logs with truck and load info
--    Shows which trucks were cleaned, what their previous load was,
--    and how much it cost.
-- ============================================================
SELECT
    cl.id           AS cleaning_id,
    t.license_plate,
    t.driver_name,
    lt.name         AS previous_load,
    cl.start_time,
    cl.end_time,
    cl.cost,
    cl.status       AS cleaning_status
FROM cleaning_log cl
JOIN truck t         ON cl.truck_id = t.id
LEFT JOIN load_type lt ON t.previous_load_type_id = lt.id
ORDER BY cl.start_time DESC;


-- ============================================================
-- 5. JOIN: Parking grid view – all slots with any parked truck
--    Displays every parking slot and the truck in it (if occupied).
-- ============================================================
SELECT
    ps.slot_number,
    ps.row_label,
    ps.col_number,
    ps.is_occupied,
    t.license_plate,
    t.driver_name,
    t.origin_city,
    t.size_class
FROM parking_slot ps
LEFT JOIN truck t ON ps.truck_id = t.id
ORDER BY ps.row_label, ps.col_number;


-- ============================================================
-- 6. UNION: Combined ledger – trucks IN_CLEANING + trucks IN_PARKING
--    Merges two different groups of trucks into one result set
--    with a 'source' column to tell them apart.
-- ============================================================
SELECT t.id AS truck_id, t.license_plate, t.current_status,
       t.location_distance, t.arrival_timestamp, 'PARKING' AS source
FROM truck t
WHERE t.current_status = 'IN_PARKING'

UNION

SELECT t.id AS truck_id, t.license_plate, t.current_status,
       t.location_distance, t.arrival_timestamp, 'CLEANING' AS source
FROM truck t
WHERE t.current_status = 'IN_CLEANING'

ORDER BY source, truck_id;


-- ============================================================
-- 7. UNION: All active trucks – at gates + in cleaning
--    Shows every truck that is currently busy (either at a gate
--    or being cleaned).
-- ============================================================
SELECT t.id AS truck_id, t.license_plate, t.current_status,
       t.location_distance, t.arrival_timestamp, 'AT_GATE' AS source
FROM truck t
WHERE t.current_status = 'AT_GATE'

UNION

SELECT t.id AS truck_id, t.license_plate, t.current_status,
       t.location_distance, t.arrival_timestamp, 'IN_CLEANING' AS source
FROM truck t
WHERE t.current_status = 'IN_CLEANING'

ORDER BY source, truck_id;


-- ============================================================
-- 8. AGGREGATE + HAVING: Total cleaning cost per truck
--    Groups cleaning logs by truck and keeps only trucks whose
--    total cleaning cost exceeds 2000 INR.
-- ============================================================
SELECT
    t.license_plate,
    t.driver_name,
    COUNT(cl.id)   AS cleaning_count,
    SUM(cl.cost)   AS total_cost
FROM cleaning_log cl
JOIN truck t ON cl.truck_id = t.id
GROUP BY t.id, t.license_plate, t.driver_name
HAVING SUM(cl.cost) > 2000
ORDER BY total_cost DESC;


-- ============================================================
-- 9. AGGREGATE: Total weight dispatched per load type (completed only)
--    Sums up the load_weight for every completed dispatch,
--    grouped by load type.
-- ============================================================
SELECT
    lt.name         AS load_type,
    COUNT(d.id)     AS dispatch_count,
    SUM(d.load_weight) AS total_weight_tons
FROM dispatch_operation d
JOIN load_type lt ON d.load_type_id = lt.id
WHERE d.status = 'COMPLETED'
GROUP BY lt.id, lt.name
ORDER BY total_weight_tons DESC;


-- ============================================================
-- 10. AGGREGATE: Dispatches per gate and average duration in minutes
--     For each gate, counts how many dispatches have been completed
--     and computes the average time from start to completion.
-- ============================================================
SELECT
    g.gate_number,
    COUNT(d.id) AS total_dispatches,
    ROUND(AVG(TIMESTAMPDIFF(MINUTE, d.started_at, d.completed_at)), 1) AS avg_duration_min
FROM dispatch_operation d
JOIN warehouse_gate g ON d.gate_id = g.id
WHERE d.status = 'COMPLETED'
  AND d.started_at IS NOT NULL
  AND d.completed_at IS NOT NULL
GROUP BY g.id, g.gate_number
ORDER BY g.gate_number;


-- ============================================================
-- 11. AGGREGATE: Trucks per size_class currently in parking
--     Quick count of SMALL / MEDIUM / LARGE trucks parked now.
-- ============================================================
SELECT
    t.size_class,
    COUNT(*) AS truck_count
FROM truck t
WHERE t.current_status = 'IN_PARKING'
GROUP BY t.size_class
ORDER BY FIELD(t.size_class, 'LARGE','MEDIUM','SMALL');


-- ============================================================
-- 12. AGGREGATE: Daily totals – dispatches and weight per date
--     Groups completed dispatches by date so we can see
--     throughput trends.
-- ============================================================
SELECT
    DATE(d.completed_at) AS dispatch_date,
    COUNT(d.id)          AS dispatch_count,
    SUM(d.load_weight)   AS total_weight_tons
FROM dispatch_operation d
WHERE d.status = 'COMPLETED'
  AND d.completed_at IS NOT NULL
GROUP BY DATE(d.completed_at)
ORDER BY dispatch_date DESC;


-- ============================================================
-- 13. SUBQUERY: Trucks whose capacity is above the average
--     Uses a subquery to find the mean capacity, then filters.
-- ============================================================
SELECT
    t.license_plate,
    t.driver_name,
    t.max_weight_capacity,
    t.size_class
FROM truck t
WHERE t.max_weight_capacity > (
    SELECT AVG(t2.max_weight_capacity) FROM truck t2
)
ORDER BY t.max_weight_capacity DESC;
