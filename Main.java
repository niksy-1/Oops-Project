import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args) 
	{
        if (GraphicsEnvironment.isHeadless()) 
		{
            runConsoleMode();
            return;
        }

        boolean testMode = askTestMode();
        int lotSize = askLotSize();
        ParkingLotGUI.launch(lotSize, testMode);
    }

    private static boolean askTestMode() 
	{
        int choice = JOptionPane.showConfirmDialog(
                null,
                "Enable test mode? (1 second = 5 minutes)",
                "Parking Lot Setup",
                JOptionPane.YES_NO_OPTION
        );
        return choice == JOptionPane.YES_OPTION;
    }

    private static int askLotSize()
	{
        while (true)
{
            String input = JOptionPane.showInputDialog(null, "Enter number of parking slots:", "Parking Lot Setup", JOptionPane.QUESTION_MESSAGE);
            if (input == null)
			{
                System.exit(0);
            }

            try
			{
                int lotSize = Integer.parseInt(input.trim());
                if (lotSize > 0)	
				{
                    return lotSize;
                }
            } catch (NumberFormatException ignored)
			{
            }

            JOptionPane.showMessageDialog(null, "Invalid value. Please enter a positive number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void runConsoleMode()
{
        Scanner scanner = new Scanner(System.in);
        System.out.println("Running in console mode (headless environment detected).");

        char testChoice;
        while (true)
{
            System.out.print("Test mode? (y/n): ");
            String value = scanner.nextLine().trim().toLowerCase();
            if (value.equals("y") || value.equals("n"))
{
                testChoice = value.charAt(0);
                break;
            }
            System.out.println("Invalid. Enter y or n.");
        }

        ParkingUtils.setTimeScale(testChoice == 'y' ? 300 : 1);

        int lotSize;
        while (true)
{
            System.out.print("Enter number of parking slots: ");
            try
{
                lotSize = Integer.parseInt(scanner.nextLine().trim());
                if (lotSize > 0) break;
            } catch (NumberFormatException ignored)
			{
            }
            System.out.println("Invalid. Enter a positive number of slots.");
        }

        GateSystem gateSystem = new GateSystem(lotSize);
        int choice = 0;
        while (choice != 4)
		{
            System.out.print("\n1) Park\n2) Unpark\n3) Status\n4) Exit\nChoice: ");
            try
			{
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e)
			{
                System.out.println("Invalid choice.");
                continue;
            }

            switch (choice)
			{
                case 1 ->
				{
                    System.out.print("Enter License Plate: ");
                    String plate = ParkingUtils.normalizePlate(scanner.nextLine().trim());
                    if (!ParkingUtils.isValidPlateFormat(plate))
					{
                        System.out.println("Invalid plate format. Expected format: AA11AA1111");
                        continue;
                    }
                    System.out.println("Select Vehicle Type:");
                    VehicleType[] types = VehicleType.values();
                    for (int i = 0; i < types.length; i++)
					{
                        System.out.println((i + 1) + " for " + types[i].getDisplayName());
                    }

                    VehicleType type;
                    try
					{
                        int typeInput = Integer.parseInt(scanner.nextLine().trim());
                        type = VehicleType.fromMenuOption(typeInput);
                    } catch (NumberFormatException e)
					{
                        type = null;
                    }

                    if (type == null)
					{
                        System.out.println("Invalid vehicle type selection.");
                        continue;
                    }

                    String ticketId = gateSystem.handleParking(plate, type);
					System.out.println(ticketId.isEmpty() ? "Parking full or duplicate active vehicle." : "Parked. Ticket ID: " + ticketId);
                }
                case 2 ->
				{
                    System.out.print("Enter Ticket ID: ");
                    String ticketId = scanner.nextLine().trim();
					String receipt = gateSystem.handleUnparkingWithReceipt(ticketId);
                    if (receipt == null)
					{
                        System.out.println("Invalid ticket.");
                    }
                }
				case 3 -> gateSystem.showStatus();
                case 4 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice.");
            }
        }

        scanner.close();
    }
}