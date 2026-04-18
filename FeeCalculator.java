import java.util.EnumMap;
import java.util.Map;

public class FeeCalculator {
    private final Map<VehicleType, FeePolicy> policies;

    public FeeCalculator() {
        this.policies = new EnumMap<>(VehicleType.class);
        registerPolicy(VehicleType.TWO_WHEELER, new SlabFeePolicy(50, 100, 250));
        registerPolicy(VehicleType.FOUR_WHEELER, new SlabFeePolicy(100, 300, 500));
    }

    public void registerPolicy(VehicleType type, FeePolicy policy) {
        if (type == null || policy == null) {
            throw new IllegalArgumentException("Vehicle type and policy are required");
        }
        policies.put(type, policy);
    }

    public double calculate(long entry, long exit, VehicleType type) {
        FeePolicy policy = policies.get(type);
        if (policy == null) {
            throw new IllegalStateException("No fee policy registered for type: " + type);
        }
        return policy.calculate(entry, exit);
    }

    public int getRate(VehicleType type) {
        FeePolicy policy = policies.get(type);
        if (policy == null) {
            throw new IllegalStateException("No fee policy registered for type: " + type);
        }
        return policy.getBaseRate();
    }
}
