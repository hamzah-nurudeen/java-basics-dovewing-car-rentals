// Economy vehicle class with economy daily rate and deposit rules.
public class EconomyVehicle extends Vehicle {

    public EconomyVehicle(String model, String reg, double dailyRate, double baseDeposit) {
        super(model, reg, dailyRate, baseDeposit);
    }

    @Override
    public String getCategory() {
        return "ECONOMY";
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
