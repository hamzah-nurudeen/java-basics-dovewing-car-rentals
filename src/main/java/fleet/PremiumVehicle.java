package fleet;

import renter.Renter;

// Premium vehicle class isolated from economy pricing and deposit rules.
public class PremiumVehicle extends Vehicle {

    public PremiumVehicle(String model, String reg, double dailyRate, double baseDeposit) {
        super(model, reg, dailyRate, baseDeposit);
    }

    @Override
    public String getCategory() {
        return "PREMIUM";
    }

    @Override
    public double calculateCost(int days) {
        return getDailyRate() * days;
    }

    @Override
    public double calculateDeposit(Renter renter) {
        if (renter.isDepositWaived()) {
            return 0.0;
        }
        return getBaseDeposit();
    }
}
