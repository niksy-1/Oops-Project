import java.util.Map;

public class GateSystem {
    private final ParkingManager manager;
    private final FeeCalculator calculator;
    private final StorageService storage;

    public GateSystem(int capacity) {
        this.manager = new ParkingManager(capacity);
        this.calculator = new FeeCalculator();
        this.storage = new StorageService("logs");
    }

    public void run() {
        System.out.println("GateSystem initialized.");
    }

    public String handleParking(String plate, VehicleType type) {
        if (manager.hasActivePlate(plate)) {
            return "";
        }

        Vehicle vehicle = type == VehicleType.TWO_WHEELER ? new TwoWheeler(plate) : new FourWheeler(plate);
        Ticket ticket = manager.issueTicket(vehicle);
        return ticket == null ? "" : ticket.getTicketID();
    }

    public boolean handleUnparking(String ticketID) {
        Ticket ticket = manager.releaseSlot(ticketID);
        if (ticket == null) {
            return false;
        }

        long exitMs = ParkingUtils.nowMs();
        double fee = calculator.calculate(ticket.getEntryTime(), exitMs, ticket.getVehicle().getType());
        printReceipt(ticket, exitMs, fee);
        storage.saveToLog(ticket, fee, exitMs);
        return true;
    }

    public void showStatus() {
        int freeCount = 0;
        for (Slot slot : manager.getSlots()) {
            if (slot.isAvailable()) {
                freeCount++;
            }
        }

        System.out.println("Total slots: " + manager.getSlots().size() + " | Free: " + freeCount + " | Occupied: " + (manager.getSlots().size() - freeCount));
        for (Slot slot : manager.getSlots()) {
            String label = slot.isAvailable() ? "Empty" : slot.getAssigned().getLicensePlate();
            System.out.println("Slot " + (slot.getId() + 1) + ": " + label);
        }
    }

    public boolean showTicketByPlate(String plate) {
        Ticket ticket = manager.findTicket(plate);
        if (ticket == null) {
            return false;
        }

        long diffMs = ParkingUtils.nowMs() - ticket.getEntryTime();
        long totalSecs = diffMs / 1000;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;

        System.out.println("Ticket ID: " + ticket.getTicketID());
        System.out.println("Slot: " + (ticket.getAssignedSlot() + 1));
        System.out.println("Time parked: " + mins + " minute(s) " + secs + " second(s)\n");
        return true;
    }

    public void dumpActiveTickets() {
        Map<String, Ticket> activeTickets = manager.getActiveTickets();
        if (activeTickets.isEmpty()) {
            System.out.println("No active tickets.");
            return;
        }

        long snapshot = ParkingUtils.nowMs();
        for (Ticket ticket : activeTickets.values()) {
            storage.saveActiveSnapshot(ticket, snapshot);
        }
        System.out.println("Active tickets dumped to logger with exit_time as NA.");
    }

    public void showDailySummary() {
        storage.generateDailySummary();
    }

    private void printReceipt(Ticket ticket, long exitMs, double fee) {
        long diffMs = exitMs - ticket.getEntryTime();
        long totalSecs = diffMs / 1000;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;
        long billedHours = diffMs / 3_600_000L;

        System.out.println("\n===== RECEIPT =====");
        System.out.println("Ticket ID   : " + ticket.getTicketID());
        System.out.println("Plate       : " + ticket.getVehicle().getLicensePlate());
        System.out.println("Type        : " + (ticket.getVehicle().getType() == VehicleType.TWO_WHEELER ? "2W" : "4W"));
        System.out.println("Slot        : " + (ticket.getAssignedSlot() + 1));
        System.out.println("Time In     : " + ParkingUtils.formatTime(ticket.getEntryTime()));
        System.out.println("Time Out    : " + ParkingUtils.formatTime(exitMs));
        System.out.println("Duration    : " + mins + " min " + secs + " sec");
        System.out.println("Billed Hours: " + billedHours);
        System.out.println("Amount      : Rs " + Math.round(fee));
        System.out.println("===================");
    }
}
