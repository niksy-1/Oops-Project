public class Ticket {
    private final String ticketID;
    private final long entryTime;
    private final int assignedSlot;
    private final Vehicle vehicle;

    public Ticket(String ticketID, long entryTime, int assignedSlot, Vehicle vehicle) {
        this.ticketID = ticketID;
        this.entryTime = entryTime;
        this.assignedSlot = assignedSlot;
        this.vehicle = vehicle;
    }

    public String getTicketID() {
        return ticketID;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public int getAssignedSlot() {
        return assignedSlot;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getEntryTimeFormatted() {
        return ParkingUtils.formatTime(entryTime);
    }
}
