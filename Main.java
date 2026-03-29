import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        char testChoice;
        while (true) {
            System.out.print("Test mode? (y/n): ");
            String value = scanner.nextLine().trim().toLowerCase();
            if (value.equals("y") || value.equals("n")) {
                testChoice = value.charAt(0);
                break;
            }
            System.out.println("Invalid. Enter y or n.");
        }

        if (testChoice == 'y') {
            ParkingUtils.setTimeScale(300);
            System.out.println("Test mode enabled: 1 second = 5 minutes.");
        } else {
            ParkingUtils.setTimeScale(1);
        }

        int lotSize;
        while (true) {
            System.out.print("Enter number of parking slots: ");
            try {
                lotSize = Integer.parseInt(scanner.nextLine().trim());
                if (lotSize > 0) break;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid. Enter a positive number of slots.");
        }

        GateSystem gateSystem = new GateSystem(lotSize);
        gateSystem.run();

        int choice = 0;
        while (choice != 7) {
            System.out.print("\n1) Park\n2) Unpark\n3) Status\n4) Ticket information for a specific license plate\n5) Dump active tickets\n6) Show daily report\n7) End\nChoice: ");
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter License Plate: ");
                    String plate = ParkingUtils.normalizePlate(scanner.nextLine().trim());
                    if (!ParkingUtils.isValidPlateFormat(plate)) {
                        System.out.println("Invalid plate format. Expected format: AA11AA1111");
                        continue;
                    }

                    int typeInput;
                    while (true) {
                        System.out.print("Enter Vehicle Type(1 for Two Wheeler, 2 for Four Wheeler): ");
                        try {
                            typeInput = Integer.parseInt(scanner.nextLine().trim());
                            if (typeInput == 1 || typeInput == 2) break;
                        } catch (NumberFormatException ignored) {
                        }
                        System.out.println("Invalid. Enter 1 (Two Wheeler) or 2 (Four Wheeler).");
                    }

                    VehicleType type = typeInput == 1 ? VehicleType.TWO_WHEELER : VehicleType.FOUR_WHEELER;
                    String ticketId = gateSystem.handleParking(plate, type);
                    if (ticketId.isEmpty()) {
                        System.out.println("Parking full or duplicate active vehicle.");
                    } else {
                        System.out.println("Parked.\nTicket ID: " + ticketId);
                    }
                }
                case 2 -> {
                    System.out.print("Enter Ticket ID: ");
                    String ticketId = scanner.nextLine().trim();
                    System.out.println(gateSystem.handleUnparking(ticketId) ? "Unparked." : "Invalid ticket.");
                }
                case 3 -> gateSystem.showStatus();
                case 4 -> {
                    System.out.print("Enter License Plate: ");
                    String plate = ParkingUtils.normalizePlate(scanner.nextLine().trim());
                    if (!gateSystem.showTicketByPlate(plate)) {
                        System.out.println("Plate not found.");
                    }
                }
                case 5 -> gateSystem.dumpActiveTickets();
                case 6 -> gateSystem.showDailySummary();
                case 7 -> { }
                default -> System.out.println("Invalid choice.");
            }
        }

        scanner.close();
    }
}

