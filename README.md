# Oops-Project Parking Lot (Java)

A Java console application that manages a parking lot, issues tickets, calculates fees, and writes visit logs as JSON files.

## Design

The code follows the class diagram with the following key classes:

- `GateSystem` (orchestration / UI actions)
- `ParkingManager` (slot and active-ticket management)
- `FeeCalculator` (billing rules)
- `StorageService` (log persistence + daily summary)
- `Ticket`
- `Slot`
- `Vehicle`, `TwoWheeler`, `FourWheeler`
- `VehicleType`

## Build

```bash
javac src/*.java
```

## Run

```bash
java -cp src Main
```

## Notes

- **License plate format**: expects values like `AA11AA1111` (optional spaces/hyphens allowed by validator).
- **Test mode**: `y` scales time so `1 second = 5 minutes`.
- **Logs**: written under `logs/log_YYYY_MM_DD.json`.
