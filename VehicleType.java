public enum VehicleType {
    TWO_WHEELER("Two Wheeler", "2W", "TwoWheeler", 50, 100, 250),
    FOUR_WHEELER("Four Wheeler", "4W", "FourWheeler", 100, 300, 500);

    private final String displayName;
    private final String shortCode;
    private final String storageLabel;
    private final int firstHourRate;
    private final int nextHoursRate;
    private final int laterHoursRate;

    VehicleType(String displayName, String shortCode, String storageLabel, int firstHourRate, int nextHoursRate, int laterHoursRate) {
        this.displayName = displayName;
        this.shortCode = shortCode;
        this.storageLabel = storageLabel;
        this.firstHourRate = firstHourRate;
        this.nextHoursRate = nextHoursRate;
        this.laterHoursRate = laterHoursRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getStorageLabel() {
        return storageLabel;
    }

    public FeePolicy createFeePolicy() {
        return new SlabFeePolicy(firstHourRate, nextHoursRate, laterHoursRate);
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
