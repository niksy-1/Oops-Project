# Oops-Project Parking Lot (Java)

A Java parking-lot application with a desktop GUI (Swing), plus a console fallback for headless environments. It manages parking slots, issues tickets, calculates fees, and writes visit logs as JSON files.

## Design

The code follows the class diagram with the following key classes:

- `GateSystem` (orchestration / business operations)
- `ParkingManager` (slot and active-ticket management)
- `FeeCalculator` (billing rules with pluggable fee policies)
- `StorageService` (log persistence + daily summary)
- `ParkingLotGUI` (desktop user interface)
- `Ticket`
- `Slot`
- `Vehicle`, `TwoWheeler`, `FourWheeler`
- `VehicleType`

## Build

```bash
javac *.java
```

## Run

```bash
java Main
```

## Notes

- **License plate format**: expects values like `AA11AA1111` (optional spaces/hyphens allowed by validator).
- **Test mode**: when enabled, `1 second = 5 minutes`.
- **Logs**: written under `logs/log_YYYY_MM_DD.json`.
- **Environment behavior**:
  - On systems with a display, the Swing GUI opens.
  - In headless environments, the app automatically falls back to console mode.

- **Extending pricing**: register additional `FeePolicy` implementations in `FeeCalculator` without modifying existing billing logic.
