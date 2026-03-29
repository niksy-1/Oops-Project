public class Slot {
    private final int id;
    private boolean occupied;
    private Vehicle assigned;

    public Slot(int id) {
        this.id = id;
    }

    public boolean assign(Vehicle vehicle) {
        if (occupied || vehicle == null) {
            return false;
        }
        assigned = vehicle;
        occupied = true;
        return true;
    }

    public void release() {
        assigned = null;
        occupied = false;
    }

    public boolean isAvailable() {
        return !occupied;
    }

    public int getId() {
        return id;
    }

    public Vehicle getAssigned() {
        return assigned;
    }
}
