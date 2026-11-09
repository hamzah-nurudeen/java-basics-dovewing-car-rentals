package fleet;

import renter.Renter;

// Commercial van vehicle class with its own deposit rule:
// Because commercial vans carry heavy cargo risk, members pay half (50%) of the
// base deposit instead of having the entire deposit waived, while casual renters
// pay the full base deposit.
// Adding this class requires zero changes to Depot, Vehicle, EconomyVehicle,
// PremiumVehicle, Renter, Rental, or ConsolePrinter.
public class CommercialVanVehicle extends Vehicle {

    public CommercialVanVehicle(String model, String reg, double dailyRate, double baseDeposit) {
        super(model, reg, dailyRate, baseDeposit);
    }

    @Override
    public String getCategory() {
        return "COMMERCIAL_VAN";
    }

    @Override
    public double calculateCost(int days) {
        return getDailyRate() * days;
    }

    @Override
    public double calculateDeposit(Renter renter) {
        if (renter.isDepositWaived()) {
            return getBaseDeposit() * 0.5;
        }
        return getBaseDeposit();
    }
}
