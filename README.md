# Oops-Project Parking Lot

A simple C++ console application that manages a parking lot, issues tickets, calculates fees, and stores visit logs as JSON files. The program supports two- and four-wheeler pricing, test time scaling, and basic lookup by license plate.

## Features

- Park and unpark vehicles with unique ticket IDs.
- Two-wheeler vs. four-wheeler billing tiers.
- Status view of occupied vs. free slots.
- Lookup active tickets by license plate.
- Optional test mode that scales time (1 second = 5 minutes).
- Daily JSON log files under `logs/` for completed visits.

## Project Structure

- `main.cpp`: CLI menu and user flow.
- `parkingLot.h/.cpp`: Parking lot logic, ticket handling, billing, and logging.
- `parkingUtils.h/.cpp`: Time utilities, license plate validation, and JSON escaping.

## Build

```bash
g++ -std=c++17 -O2 -Wall -Wextra -pedantic -o parking_lot main.cpp parkingLot.cpp parkingUtils.cpp
```

## Run

```bash
./parking_lot
```

## Usage Notes

- **License plate format**: The app expects a pattern like `AA11AA1111` (spaces or hyphens are allowed by the validator).
- **Ticket ID**: On park, a ticket like `T1` is issued. You must provide it to unpark.
- **Logs**: When a vehicle is unparked, a JSON entry is appended to `logs/log_YYYY_MM_DD.json`.
- **Test mode**: Answer `y` at startup to enable time scaling for easier billing tests.
