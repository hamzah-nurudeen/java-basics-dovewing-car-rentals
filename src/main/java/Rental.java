// Records a single vehicle rental transaction.
// Whether this rental is active is derived from vehicle.getCurrentRental() == this,
// so a Rental and its Vehicle can never disagree on status.
public class Rental {
    private static int counter = 1;

    private final int id;
    private final Renter renter;
    private final Vehicle vehicle;
    private final int days;
    private final double cost;
    private final double deposit;

    public Rental(Renter renter, Vehicle vehicle, int days, double cost, double deposit) {
        this.id = counter;
        counter++;
        this.renter = renter;
        this.vehicle = vehicle;
        this.days = days;
        this.cost = cost;
        this.deposit = deposit;
    }

    public int getId() {
        return id;
    }

    public Renter getRenter() {
        return renter;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getRenterName() {
        return renter.getName();
    }

    public String getVehicleReg() {
        return vehicle.getReg();
    }

    public int getDays() {
        return days;
    }

    public double getCost() {
        return cost;
    }

    public double getDeposit() {
        return deposit;
    }

    public boolean isActive() {
        return vehicle.getCurrentRental() == this;
    }

    public String getStatus() {
        if (isActive()) {
            return "ACTIVE";
        }
        return "RETURNED";
    }
}
