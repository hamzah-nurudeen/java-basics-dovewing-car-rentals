// Abstract base class for all vehicles in the Dovewing fleet.
// Holds ONE private reference (currentRental) as the single source of truth
// for status: isAvailable() and isRented() are derived from it, so a vehicle
// can never be both available and rented at the same time.
public abstract class Vehicle {
    private static int counter = 1;

    private final int id;
    private final String model;
    private final String reg;
    private final double dailyRate;
    private final double baseDeposit;
    private Rental currentRental;

    protected Vehicle(String model, String reg, double dailyRate, double baseDeposit) {
        this.id = counter;
        counter++;
        this.model = model;
        this.reg = reg;
        this.dailyRate = dailyRate;
        this.baseDeposit = baseDeposit;
        this.currentRental = null;
    }

    public int getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getReg() {
        return reg;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public double getBaseDeposit() {
        return baseDeposit;
    }

    public abstract String getCategory();

    public abstract double calculateCost(int days);

    public abstract double calculateDeposit(Renter renter);

    public boolean isAvailable() {
        return currentRental == null;
    }

    public boolean isRented() {
        return currentRental != null;
    }

    public Rental getCurrentRental() {
        return currentRental;
    }

    public String getStatus() {
        if (isRented()) {
            return "RENTED";
        }
        return "AVAILABLE";
    }

    // Package-private so only Depot can attach or clear a rental on a vehicle.
    void assignRental(Rental rental) {
        this.currentRental = rental;
    }

    void clearRental() {
        this.currentRental = null;
    }
}
