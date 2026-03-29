import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingManager {
    private final List<Slot> slots;
    private final Map<String, Ticket> activeTickets;
    private int nextTicketNo;

    public ParkingManager(int capacity) {
        this.slots = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            slots.add(new Slot(i));
        }
        this.activeTickets = new HashMap<>();
        this.nextTicketNo = 1;
    }

    public int findNearestSlot() {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).isAvailable()) {
                return i;
            }
        }
        return -1;
    }

    public Ticket issueTicket(Vehicle vehicle) {
        int slotIdx = findNearestSlot();
        if (slotIdx < 0 || vehicle == null || !slots.get(slotIdx).assign(vehicle)) {
            return null;
        }

        Ticket ticket = new Ticket("T" + nextTicketNo++, ParkingUtils.nowMs(), slotIdx, vehicle);
        activeTickets.put(ticket.getTicketID(), ticket);
        return ticket;
    }

    public Ticket releaseSlot(String ticketId) {
        Ticket ticket = activeTickets.remove(ticketId);
        if (ticket == null) {
            return null;
        }

        int slotId = ticket.getAssignedSlot();
        if (slotId >= 0 && slotId < slots.size()) {
            slots.get(slotId).release();
        }
        return ticket;
    }

    public Ticket findTicket(String licensePlate) {
        for (Ticket ticket : activeTickets.values()) {
            if (ticket.getVehicle().getLicensePlate().equals(licensePlate)) {
                return ticket;
            }
        }
        return null;
    }

    public boolean hasActivePlate(String plate) {
        return findTicket(plate) != null;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public Map<String, Ticket> getActiveTickets() {
        return activeTickets;
    }
}
