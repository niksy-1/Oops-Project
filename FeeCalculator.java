public class FeeCalculator {
    public double calculate(long entry, long exit, VehicleType type) {
        if (exit <= entry) {
            return 0;
        }

        long hours = (exit - entry) / 3_600_000L;

        if (type == VehicleType.FOUR_WHEELER) {
            if (hours == 0) return 0;
            if (hours == 1) return 100;
            if (hours <= 3) return 100 + (hours - 1) * 300;
            return 100 + 2 * 300 + (hours - 3) * 500;
        }

        if (hours == 0) return 0;
        if (hours == 1) return 50;
        if (hours <= 3) return 50 + (hours - 1) * 100;
        return 50 + 2 * 100 + (hours - 3) * 250;
    }

    public int getRate(VehicleType type) {
        return type == VehicleType.FOUR_WHEELER ? 100 : 50;
    }
}
