public enum VehicleType {
    TWO_WHEELER("Two Wheeler"),
    FOUR_WHEELER("Four Wheeler");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static VehicleType fromMenuOption(int option) {
        VehicleType[] values = VehicleType.values();
        if (option < 1 || option > values.length) {
            return null;
        }
        return values[option - 1];
    }

    @Override
    public String toString() {
        return displayName;
    }
}
