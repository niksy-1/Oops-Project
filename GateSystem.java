import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

        Vehicle vehicle = createVehicle(plate, type);
        if (vehicle == null) {
            return "";
        }

        Ticket ticket = manager.issueTicket(vehicle);
        return ticket == null ? "" : ticket.getTicketID();
    }

    public boolean handleUnparking(String ticketID) {
        return handleUnparkingWithReceipt(ticketID) != null;
    }

    public String handleUnparkingWithReceipt(String ticketID) {
        Ticket ticket = manager.releaseSlot(ticketID);
        if (ticket == null) {
            return null;
        }

        long exitMs = ParkingUtils.nowMs();
        double fee = calculator.calculate(ticket.getEntryTime(), exitMs, ticket.getVehicle().getType());
        String receipt = buildReceipt(ticket, exitMs, fee);
        System.out.println(receipt);
        storage.saveToLog(ticket, fee, exitMs);
        return receipt;
    }

    public void showStatus() {
        System.out.println(getStatusReport());
    }

    public String getStatusReport() {
        StringBuilder output = new StringBuilder();
        int freeCount = 0;
        for (Slot slot : manager.getSlots()) {
            if (slot.isAvailable()) {
                freeCount++;
            }
        }

        output
                .append("Total slots: ")
                .append(manager.getSlots().size())
                .append(" | Free: ")
                .append(freeCount)
                .append(" | Occupied: ")
                .append(manager.getSlots().size() - freeCount)
                .append("\n");

        for (Slot slot : manager.getSlots()) {
            String label = slot.isAvailable() ? "Empty" : slot.getAssigned().getLicensePlate();
            output.append("Slot ").append(slot.getId() + 1).append(": ").append(label).append("\n");
        }
        return output.toString().trim();
    }

    public boolean showTicketByPlate(String plate) {
        String details = getTicketInfoByPlate(plate);
        if (details == null) {
            return false;
        }
        System.out.println(details);
        return true;
    }

    public String getTicketInfoByPlate(String plate) {
        Ticket ticket = manager.findTicket(plate);
        if (ticket == null) {
            return null;
        }

        long diffMs = ParkingUtils.nowMs() - ticket.getEntryTime();
        long totalSecs = diffMs / 1000;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;

        return "Ticket ID: " + ticket.getTicketID() + "\n"
                + "Slot: " + (ticket.getAssignedSlot() + 1) + "\n"
                + "Time parked: " + mins + " minute(s) " + secs + " second(s)";
    }

    public void dumpActiveTickets() {
        if (!dumpActiveTicketsToLog()) {
            System.out.println("No active tickets.");
            return;
        }
        System.out.println("Active tickets dumped to logger with exit_time as NA.");
    }

    public boolean dumpActiveTicketsToLog() {
        Map<String, Ticket> activeTickets = manager.getActiveTickets();
        if (activeTickets.isEmpty()) {
            return false;
        }

        long snapshot = ParkingUtils.nowMs();
        for (Ticket ticket : activeTickets.values()) {
            storage.saveActiveSnapshot(ticket, snapshot);
        }
        return true;
    }

    public void showDailySummary() {
        storage.generateDailySummary();
    }

    public List<Ticket> getActiveTicketsSnapshot() {
        List<Ticket> tickets = new ArrayList<>(manager.getActiveTickets().values());
        tickets.sort(Comparator.comparingInt(Ticket::getAssignedSlot));
        return tickets;
    }

    private Vehicle createVehicle(String plate, VehicleType type) {
        if (type == null) {
            return null;
        }
        return new Vehicle(plate, type);
    }

    private String buildReceipt(Ticket ticket, long exitMs, double fee) {
        long diffMs = exitMs - ticket.getEntryTime();
        long totalSecs = diffMs / 1000;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;
        long billedHours = diffMs / 3_600_000L;

        return "\n===== RECEIPT =====\n"
                + "Ticket ID   : " + ticket.getTicketID() + "\n"
                + "Plate       : " + ticket.getVehicle().getLicensePlate() + "\n"
                + "Type        : " + ticket.getVehicle().getType().getShortCode() + "\n"
                + "Slot        : " + (ticket.getAssignedSlot() + 1) + "\n"
                + "Time In     : " + ParkingUtils.formatTime(ticket.getEntryTime()) + "\n"
                + "Time Out    : " + ParkingUtils.formatTime(exitMs) + "\n"
                + "Duration    : " + mins + " min " + secs + " sec\n"
                + "Billed Hours: " + billedHours + "\n"
                + "Amount      : Rs " + Math.round(fee) + "\n"
                + "===================";
    }
}
