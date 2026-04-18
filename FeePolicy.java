public interface FeePolicy {
    double calculate(long entryMs, long exitMs);

    int getBaseRate();
}
