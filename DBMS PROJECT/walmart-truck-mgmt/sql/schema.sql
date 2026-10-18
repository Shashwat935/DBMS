-- ============================================================
-- Walmart Warehouse Truck Management System – Database Schema
-- Database: walmart_truck_db   |   MySQL 8
-- ============================================================

CREATE DATABASE IF NOT EXISTS walmart_truck_db;
USE walmart_truck_db;

-- -----------------------------------------------
-- 1. LoadType – master list of cargo categories
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS load_type (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    category    VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------
-- 2. Truck – each truck that visits the warehouse
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS truck (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate         VARCHAR(20)  NOT NULL UNIQUE,
    driver_name           VARCHAR(100) NOT NULL,
    origin_city           VARCHAR(100) NOT NULL,
    location_distance     DOUBLE       NOT NULL,          -- km from warehouse
    max_weight_capacity   DOUBLE       NOT NULL,          -- tons
    previous_load_type_id BIGINT       NULL,
    current_status        VARCHAR(30)  NOT NULL,           -- IN_PARKING, IN_CLEANING, AT_GATE, DISPATCHED
    arrival_timestamp     TIMESTAMP    NULL,
    size_class            VARCHAR(10)  NOT NULL,           -- SMALL, MEDIUM, LARGE (computed)
    created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_truck_status      (current_status),
    INDEX idx_truck_arrival     (arrival_timestamp),

    CONSTRAINT fk_truck_prev_load FOREIGN KEY (previous_load_type_id)
        REFERENCES load_type(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------
-- 3. ParkingSlot – 40 slots in a 4x10 grid
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS parking_slot (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(10) NOT NULL UNIQUE,
    row_label   CHAR(1)     NOT NULL,
    col_number  INT         NOT NULL,
    is_occupied BOOLEAN     NOT NULL DEFAULT FALSE,
    truck_id    BIGINT      NULL UNIQUE,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_parking_occupied (is_occupied),

    CONSTRAINT fk_parking_truck FOREIGN KEY (truck_id)
        REFERENCES truck(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------
-- 4. WarehouseGate – loading/unloading gates
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS warehouse_gate (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    gate_number      VARCHAR(10) NOT NULL UNIQUE,
    gate_status      VARCHAR(20) NOT NULL DEFAULT 'IDLE',  -- IDLE, LOADING, UNLOADING
    current_truck_id BIGINT      NULL UNIQUE,
    version          INT         NOT NULL DEFAULT 0,        -- optimistic locking
    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gate_truck FOREIGN KEY (current_truck_id)
        REFERENCES truck(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------
-- 5. gate_load_type – many-to-many gate ↔ load type
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS gate_load_type (
    gate_id     BIGINT NOT NULL,
    load_type_id BIGINT NOT NULL,
    PRIMARY KEY (gate_id, load_type_id),
    CONSTRAINT fk_glt_gate FOREIGN KEY (gate_id)      REFERENCES warehouse_gate(id) ON DELETE CASCADE,
    CONSTRAINT fk_glt_lt   FOREIGN KEY (load_type_id)  REFERENCES load_type(id)      ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------
-- 6. CompatibilityRule – cleaning rules between load types
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS compatibility_rule (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_load_type_id BIGINT        NOT NULL,
    to_load_type_id   BIGINT        NOT NULL,
    cleaning_required BOOLEAN       NOT NULL DEFAULT FALSE,
    cleaning_cost     DECIMAL(10,2) NOT NULL DEFAULT 0,
    cleaning_minutes  INT           NOT NULL DEFAULT 0,
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uq_compat (from_load_type_id, to_load_type_id),

    CONSTRAINT fk_compat_from FOREIGN KEY (from_load_type_id)
        REFERENCES load_type(id) ON DELETE CASCADE,
    CONSTRAINT fk_compat_to   FOREIGN KEY (to_load_type_id)
        REFERENCES load_type(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------
-- 7. DispatchOperation – a loading/unloading job
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_operation (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    truck_id           BIGINT       NULL,
    gate_id            BIGINT       NULL,
    load_type_id       BIGINT       NOT NULL,
    operation_type     VARCHAR(20)  NOT NULL,              -- LOADING / UNLOADING
    load_weight        DOUBLE       NOT NULL,
    destination_distance DOUBLE     NOT NULL DEFAULT 0,
    is_emergency       BOOLEAN      NOT NULL DEFAULT FALSE,
    status             VARCHAR(30)  NOT NULL,               -- PENDING, ASSIGNED, IN_CLEANING, IN_PROGRESS, COMPLETED, CANCELLED
    requested_at       TIMESTAMP    NULL,
    started_at         TIMESTAMP    NULL,
    expected_minutes   INT          NULL,
    completed_at       TIMESTAMP    NULL,
    created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_dispatch_status (status),

    CONSTRAINT fk_dispatch_truck    FOREIGN KEY (truck_id)     REFERENCES truck(id)          ON DELETE SET NULL,
    CONSTRAINT fk_dispatch_gate     FOREIGN KEY (gate_id)      REFERENCES warehouse_gate(id) ON DELETE SET NULL,
    CONSTRAINT fk_dispatch_loadtype FOREIGN KEY (load_type_id) REFERENCES load_type(id)      ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------
-- 8. CleaningLog – cleaning history
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS cleaning_log (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    truck_id     BIGINT        NOT NULL,
    dispatch_id  BIGINT        NULL,
    start_time   TIMESTAMP     NOT NULL,
    end_time     TIMESTAMP     NULL,
    cost         DECIMAL(10,2) NOT NULL DEFAULT 0,
    status       VARCHAR(20)   NOT NULL DEFAULT 'IN_PROGRESS',  -- IN_PROGRESS / COMPLETED
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cl_truck    FOREIGN KEY (truck_id)    REFERENCES truck(id)              ON DELETE CASCADE,
    CONSTRAINT fk_cl_dispatch FOREIGN KEY (dispatch_id) REFERENCES dispatch_operation(id)  ON DELETE SET NULL
) ENGINE=InnoDB;


-- ============================================================
-- VIEWS
-- ============================================================

-- Gate overview: joins gate → truck → active dispatch → load type
CREATE OR REPLACE VIEW v_gate_overview AS
SELECT
    g.id            AS gate_id,
    g.gate_number,
    g.gate_status,
    t.id            AS truck_id,
    t.license_plate,
    t.driver_name,
    d.id            AS dispatch_id,
    d.operation_type,
    d.load_weight,
    d.status        AS dispatch_status,
    d.started_at,
    d.expected_minutes,
    lt.name         AS load_type_name,
    lt.category     AS load_type_category
FROM warehouse_gate g
LEFT JOIN truck t             ON g.current_truck_id = t.id
LEFT JOIN dispatch_operation d ON d.gate_id = g.id
                               AND d.status IN ('IN_PROGRESS','IN_CLEANING','ASSIGNED')
LEFT JOIN load_type lt        ON d.load_type_id = lt.id;

-- Truck queue: trucks currently parked
CREATE OR REPLACE VIEW v_truck_queue AS
SELECT
    t.id,
    t.license_plate,
    t.driver_name,
    t.origin_city,
    t.location_distance,
    t.max_weight_capacity,
    t.size_class,
    t.arrival_timestamp,
    lt.name AS previous_load_type,
    ps.slot_number
FROM truck t
LEFT JOIN load_type lt   ON t.previous_load_type_id = lt.id
LEFT JOIN parking_slot ps ON ps.truck_id = t.id
WHERE t.current_status = 'IN_PARKING';


-- ============================================================
-- TRIGGER: auto-mark CleaningLog as COMPLETED when end_time is set
-- ============================================================
DELIMITER $$
CREATE TRIGGER trg_cleaning_complete
BEFORE UPDATE ON cleaning_log
FOR EACH ROW
BEGIN
    IF NEW.end_time IS NOT NULL AND OLD.end_time IS NULL THEN
        SET NEW.status = 'COMPLETED';
    END IF;
END$$
DELIMITER ;
