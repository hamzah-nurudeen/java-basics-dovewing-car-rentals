package fleet;

import renter.Renter;

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
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Vehicle model must not be blank.");
        }
        if (reg == null || reg.isBlank()) {
            throw new IllegalArgumentException("Vehicle registration must not be blank.");
        }
        if (dailyRate < 0) {
            throw new IllegalArgumentException("Daily rate must not be negative.");
        }
        if (baseDeposit < 0) {
            throw new IllegalArgumentException("Base deposit must not be negative.");
        }
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

    public abstract VehicleCategory getCategory();

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

    public VehicleStatus getStatus() {
        if (isRented()) {
            return VehicleStatus.RENTED;
        }
        return VehicleStatus.AVAILABLE;
    }

    // Package-private inside fleet so only Depot can attach or clear a rental.
    void assignRental(Rental rental) {
        this.currentRental = rental;
    }

    void clearRental() {
        this.currentRental = null;
    }
}
