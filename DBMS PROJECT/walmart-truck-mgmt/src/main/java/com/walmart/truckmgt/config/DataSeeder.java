package com.walmart.truckmgt.config;

import com.walmart.truckmgt.model.entity.*;
import com.walmart.truckmgt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DataSeeder – seeds the database with initial data on startup.
 * Only runs if the load_type table is empty (first run).
 * Seeds: 6 load types, compatibility matrix, 40 parking slots,
 *        6 gates, 15 trucks, and sample dispatches with cleaning logs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final LoadTypeRepository loadTypeRepo;
    private final TruckRepository truckRepo;
    private final ParkingSlotRepository slotRepo;
    private final WarehouseGateRepository gateRepo;
    private final CompatibilityRuleRepository ruleRepo;
    private final DispatchOperationRepository dispatchRepo;
    private final CleaningLogRepository cleaningRepo;

    @Override
    @Transactional
    public void run(String... args) {
        // Only seed if database is empty
        if (loadTypeRepo.count() > 0) {
            log.info("Database already seeded – skipping.");
            return;
        }
        log.info("Seeding database...");

        // 1. Load types
        Map<String, LoadType> ltMap = seedLoadTypes();

        // 2. Compatibility matrix
        seedCompatibilityRules(ltMap);

        // 3. Parking slots (4 rows x 10 cols = 40)
        seedParkingSlots();

        // 4. Gates
        seedGates(ltMap);

        // 5. Trucks
        List<Truck> trucks = seedTrucks(ltMap);

        // 6. Sample dispatches and cleaning logs
        seedSampleDispatches(trucks, ltMap);

        log.info("Database seeding complete!");
    }

    // ---------------------------------------------------------------
    // 1. LOAD TYPES
    // ---------------------------------------------------------------
    private Map<String, LoadType> seedLoadTypes() {
        Map<String, LoadType> map = new LinkedHashMap<>();
        map.put("COAL",        save(new LoadType(null, "COAL",        "INDUSTRIAL",  null)));
        map.put("IRON",        save(new LoadType(null, "IRON",        "INDUSTRIAL",  null)));
        map.put("FOOD",        save(new LoadType(null, "FOOD",        "FOOD_GRADE",  null)));
        map.put("CHEMICAL",    save(new LoadType(null, "CHEMICAL",    "INDUSTRIAL",  null)));
        map.put("TEXTILE",     save(new LoadType(null, "TEXTILE",     "GENERAL",     null)));
        map.put("ELECTRONICS", save(new LoadType(null, "ELECTRONICS", "GENERAL",     null)));
        log.info("  Seeded 6 load types");
        return map;
    }

    private LoadType save(LoadType lt) {
        return loadTypeRepo.save(lt);
    }

    // ---------------------------------------------------------------
    // 2. COMPATIBILITY MATRIX
    // ---------------------------------------------------------------
    private void seedCompatibilityRules(Map<String, LoadType> lt) {
        // COAL -> FOOD: YES 5000 / 60 min
        addRule(lt, "COAL", "FOOD",        true, 5000, 60);
        // COAL -> CHEMICAL: YES 3000 / 45 min
        addRule(lt, "COAL", "CHEMICAL",    true, 3000, 45);
        // COAL -> TEXTILE: YES 2500 / 40 min
        addRule(lt, "COAL", "TEXTILE",     true, 2500, 40);
        // COAL -> ELECTRONICS: YES 3500 / 50 min
        addRule(lt, "COAL", "ELECTRONICS", true, 3500, 50);

        // IRON -> FOOD: YES 4000 / 50 min
        addRule(lt, "IRON", "FOOD",        true, 4000, 50);
        // IRON -> TEXTILE: YES 2000 / 30 min
        addRule(lt, "IRON", "TEXTILE",     true, 2000, 30);
        // IRON -> ELECTRONICS: YES 2500 / 35 min
        addRule(lt, "IRON", "ELECTRONICS", true, 2500, 35);

        // CHEMICAL -> FOOD: YES 8000 / 90 min
        addRule(lt, "CHEMICAL", "FOOD",        true, 8000, 90);
        // CHEMICAL -> TEXTILE: YES 4000 / 60 min
        addRule(lt, "CHEMICAL", "TEXTILE",     true, 4000, 60);
        // CHEMICAL -> ELECTRONICS: YES 5000 / 70 min
        addRule(lt, "CHEMICAL", "ELECTRONICS", true, 5000, 70);

        // TEXTILE -> FOOD: YES 1500 / 25 min
        addRule(lt, "TEXTILE", "FOOD",         true, 1500, 25);

        log.info("  Seeded compatibility matrix (11 rules)");
    }

    private void addRule(Map<String, LoadType> lt, String from, String to,
                         boolean required, int cost, int minutes) {
        ruleRepo.save(CompatibilityRule.builder()
                .fromLoadType(lt.get(from))
                .toLoadType(lt.get(to))
                .cleaningRequired(required)
                .cleaningCost(BigDecimal.valueOf(cost))
                .cleaningMinutes(minutes)
                .build());
    }

    // ---------------------------------------------------------------
    // 3. PARKING SLOTS
    // ---------------------------------------------------------------
    private void seedParkingSlots() {
        String[] rows = {"A", "B", "C", "D"};
        for (String row : rows) {
            for (int col = 1; col <= 10; col++) {
                slotRepo.save(ParkingSlot.builder()
                        .slotNumber(row + col)
                        .rowLabel(row)
                        .colNumber(col)
                        .isOccupied(false)
                        .build());
            }
        }
        log.info("  Seeded 40 parking slots (A1-D10)");
    }

    // ---------------------------------------------------------------
    // 4. GATES
    // ---------------------------------------------------------------
    private void seedGates(Map<String, LoadType> lt) {
        createGate("G1", lt, "COAL", "IRON");
        createGate("G2", lt, "FOOD");
        createGate("G3", lt, "FOOD", "TEXTILE");
        createGate("G4", lt, "CHEMICAL");
        createGate("G5", lt, "ELECTRONICS", "TEXTILE");
        createGate("G6", lt, "COAL", "IRON", "CHEMICAL");
        log.info("  Seeded 6 gates (G1-G6)");
    }

    private void createGate(String number, Map<String, LoadType> lt, String... types) {
        WarehouseGate gate = WarehouseGate.builder()
                .gateNumber(number)
                .gateStatus(GateStatus.IDLE)
                .supportedLoadTypes(new HashSet<>())
                .build();
        for (String t : types) {
            gate.getSupportedLoadTypes().add(lt.get(t));
        }
        gateRepo.save(gate);
    }

    // ---------------------------------------------------------------
    // 5. TRUCKS
    // ---------------------------------------------------------------
    private List<Truck> seedTrucks(Map<String, LoadType> lt) {
        LocalDateTime now = LocalDateTime.now();
        List<Truck> trucks = new ArrayList<>();

        // 12 trucks that will be parked
        trucks.add(makeTruck("MH-12-AB-1234", "Rajesh Kumar",   "Pune",       45,  15, lt.get("COAL"),    TruckStatus.IN_PARKING, now.minusHours(3)));
        trucks.add(makeTruck("MH-14-CD-5678", "Sunil Patil",    "Nashik",    120,  22, lt.get("IRON"),    TruckStatus.IN_PARKING, now.minusHours(2)));
        trucks.add(makeTruck("GJ-05-EF-9012", "Amit Shah",      "Surat",     250,  35, lt.get("FOOD"),    TruckStatus.IN_PARKING, now.minusHours(5)));
        trucks.add(makeTruck("TS-09-GH-3456", "Venkat Reddy",   "Hyderabad", 450,  40, null,              TruckStatus.IN_PARKING, now.minusHours(4)));
        trucks.add(makeTruck("MH-31-IJ-7890", "Pramod Deshmukh","Nagpur",    300,   8, lt.get("CHEMICAL"),TruckStatus.IN_PARKING, now.minusHours(1)));
        trucks.add(makeTruck("MH-04-KL-2345", "Sachin More",    "Pune",       50,  18, lt.get("TEXTILE"), TruckStatus.IN_PARKING, now.minusMinutes(90)));
        trucks.add(makeTruck("GJ-01-MN-6789", "Dharmesh Patel", "Ahmedabad", 350,  28, null,              TruckStatus.IN_PARKING, now.minusMinutes(45)));
        trucks.add(makeTruck("RJ-14-OP-1111", "Ramesh Sharma",  "Jaipur",    400,  30, lt.get("COAL"),    TruckStatus.IN_PARKING, now.minusMinutes(30)));
        trucks.add(makeTruck("KA-01-QR-2222", "Mahesh Gowda",   "Bangalore", 500,  12, lt.get("ELECTRONICS"), TruckStatus.IN_PARKING, now.minusMinutes(20)));
        trucks.add(makeTruck("MH-12-ST-3333", "Vikas Jadhav",   "Pune",       40,  20, null,              TruckStatus.IN_PARKING, now.minusMinutes(15)));
        trucks.add(makeTruck("AP-07-UV-4444", "Ravi Naidu",     "Vizag",     380,   9, lt.get("FOOD"),    TruckStatus.IN_PARKING, now.minusMinutes(10)));
        trucks.add(makeTruck("MH-20-WX-5555", "Ganesh Kulkarni","Aurangabad",200,  25, lt.get("IRON"),    TruckStatus.IN_PARKING, now.minusMinutes(5)));

        // 3 trucks that are dispatched (not in parking)
        trucks.add(makeTruck("TN-01-YZ-6666", "Karthik Rajan",  "Chennai",   600,  32, lt.get("FOOD"),    TruckStatus.DISPATCHED, now.minusHours(8)));
        trucks.add(makeTruck("MH-01-AA-7777", "Anil Sawant",    "Mumbai",     10,  14, lt.get("COAL"),    TruckStatus.DISPATCHED, now.minusHours(7)));
        trucks.add(makeTruck("UP-32-BB-8888", "Pradeep Yadav",  "Lucknow",   700,  38, lt.get("CHEMICAL"),TruckStatus.DISPATCHED, now.minusHours(6)));

        // Assign parking slots to the first 12 trucks
        List<ParkingSlot> slots = slotRepo.findFreeSlotsSorted();
        for (int i = 0; i < 12 && i < slots.size(); i++) {
            ParkingSlot slot = slots.get(i);
            slot.setTruck(trucks.get(i));
            slot.setIsOccupied(true);
            slotRepo.save(slot);
        }

        log.info("  Seeded 15 trucks (12 parked, 3 dispatched)");
        return trucks;
    }

    private Truck makeTruck(String plate, String driver, String city, double dist,
                            double cap, LoadType prevLoad, TruckStatus status, LocalDateTime arrival) {
        Truck t = Truck.builder()
                .licensePlate(plate)
                .driverName(driver)
                .originCity(city)
                .locationDistance(dist)
                .maxWeightCapacity(cap)
                .previousLoadType(prevLoad)
                .currentStatus(status)
                .arrivalTimestamp(arrival)
                .sizeClass(SizeClass.SMALL) // will be recomputed by @PrePersist
                .build();
        return truckRepo.save(t);
    }

    // ---------------------------------------------------------------
    // 6. SAMPLE DISPATCHES AND CLEANING LOGS
    // ---------------------------------------------------------------
    private void seedSampleDispatches(List<Truck> trucks, Map<String, LoadType> lt) {
        LocalDateTime now = LocalDateTime.now();

        // 3 PENDING dispatch requests (one emergency)
        dispatchRepo.save(DispatchOperation.builder()
                .loadType(lt.get("FOOD"))
                .operationType(OperationType.LOADING)
                .loadWeight(12.0)
                .destinationDistance(150.0)
                .isEmergency(false)
                .status(DispatchStatus.PENDING)
                .requestedAt(now.minusMinutes(10))
                .build());

        dispatchRepo.save(DispatchOperation.builder()
                .loadType(lt.get("COAL"))
                .operationType(OperationType.UNLOADING)
                .loadWeight(20.0)
                .destinationDistance(200.0)
                .isEmergency(true)
                .status(DispatchStatus.PENDING)
                .requestedAt(now.minusMinutes(5))
                .build());

        dispatchRepo.save(DispatchOperation.builder()
                .loadType(lt.get("ELECTRONICS"))
                .operationType(OperationType.LOADING)
                .loadWeight(8.0)
                .destinationDistance(300.0)
                .isEmergency(false)
                .status(DispatchStatus.PENDING)
                .requestedAt(now.minusMinutes(2))
                .build());

        // 2 COMPLETED dispatch history rows (using dispatched trucks)
        Truck t13 = trucks.get(12); // Chennai truck, dispatched
        Truck t14 = trucks.get(13); // Mumbai truck, dispatched

        WarehouseGate g1 = gateRepo.findByGateNumber("G1").orElseThrow();
        WarehouseGate g2 = gateRepo.findByGateNumber("G2").orElseThrow();

        DispatchOperation d1 = dispatchRepo.save(DispatchOperation.builder()
                .truck(t13).gate(g2).loadType(lt.get("FOOD"))
                .operationType(OperationType.LOADING)
                .loadWeight(25.0).destinationDistance(600.0).isEmergency(false)
                .status(DispatchStatus.COMPLETED)
                .requestedAt(now.minusHours(9))
                .startedAt(now.minusHours(8).minusMinutes(30))
                .expectedMinutes(50)
                .completedAt(now.minusHours(8))
                .build());

        DispatchOperation d2 = dispatchRepo.save(DispatchOperation.builder()
                .truck(t14).gate(g1).loadType(lt.get("COAL"))
                .operationType(OperationType.UNLOADING)
                .loadWeight(10.0).destinationDistance(10.0).isEmergency(false)
                .status(DispatchStatus.COMPLETED)
                .requestedAt(now.minusHours(8))
                .startedAt(now.minusHours(7).minusMinutes(30))
                .expectedMinutes(20)
                .completedAt(now.minusHours(7))
                .build());

        // Cleaning log for the first completed dispatch (truck previously had CHEMICAL, loaded FOOD)
        cleaningRepo.save(CleaningLog.builder()
                .truck(t13).dispatch(d1)
                .startTime(now.minusHours(9))
                .endTime(now.minusHours(8).minusMinutes(30))
                .cost(BigDecimal.valueOf(8000))
                .status("COMPLETED")
                .build());

        log.info("  Seeded 3 pending + 2 completed dispatches, 1 cleaning log");
    }
}
