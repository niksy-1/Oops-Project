public class SlabFeePolicy implements FeePolicy {
    private final int firstHourRate;
    private final int nextHoursRate;
    private final int laterHoursRate;

    public SlabFeePolicy(int firstHourRate, int nextHoursRate, int laterHoursRate) {
        this.firstHourRate = firstHourRate;
        this.nextHoursRate = nextHoursRate;
        this.laterHoursRate = laterHoursRate;
    }

    @Override
    public double calculate(long entryMs, long exitMs) {
        if (exitMs <= entryMs) {
            return 0;
        }

        long hours = (exitMs - entryMs) / 3_600_000L;
        if (hours == 0) {
            return 0;
        }
        if (hours == 1) {
            return firstHourRate;
        }
        if (hours <= 3) {
            return firstHourRate + (hours - 1) * nextHoursRate;
        }
        return firstHourRate + 2L * nextHoursRate + (hours - 3) * laterHoursRate;
    }

    @Override
    public int getBaseRate() {
        return firstHourRate;
    }
}
