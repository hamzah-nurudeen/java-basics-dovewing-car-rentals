import exceptions.VehicleNotAvailableException;
import exceptions.VehicleNotRentedException;
import fleet.Depot;
import fleet.EconomyVehicle;
import fleet.PremiumVehicle;
import fleet.Rental;
import fleet.Vehicle;
import fleet.VehicleStatus;
import renter.CasualRenter;
import renter.MemberRenter;
import renter.Renter;

// Verifies pricing, deposit waiver, single-source-of-truth status transitions,
// custom exceptions, and input validation using core Java only.
public class DepotTest {

    public static void main(String[] args) {
        testEconomyCasualAndReturn();
        testPremiumMemberDepositWaived();
        testDoubleBookingThrowsVehicleNotAvailable();
        testReturnAvailableVehicleThrowsVehicleNotRented();
        testInputValidationRejectsInvalidValues();
        System.out.println("All DepotTest checks passed.");
    }

    private static void testEconomyCasualAndReturn() {
        Depot depot = new Depot("Dovewing Car Rentals");
        Vehicle vitz = new EconomyVehicle("Toyota Vitz", "GT-1234-20", 150.00, 500.00);
        Renter kofi = new Renter("Kofi Mensah", new CasualRenter());
        depot.addVehicle(vitz);
        depot.addRenter(kofi);

        check(vitz.isAvailable() && !vitz.isRented(), "Vehicle starts available");
        depot.rent(kofi, vitz, 3);
        check(vitz.isRented() && !vitz.isAvailable(), "Vehicle is rented after rent()");
        check(vitz.getStatus() == VehicleStatus.RENTED, "Status is RENTED");

        Rental rental = vitz.getCurrentRental();
        check(rental != null && rental.isActive(), "Active rental linked to vehicle");
        check(rental.getCost() == 450.0, "Economy 3-day cost is 450.0");
        check(rental.getDeposit() == 500.0, "Casual renter pays full 500.0 deposit");

        depot.returnVehicle(vitz);
        check(vitz.isAvailable() && !vitz.isRented(), "Vehicle is available after return");
        check(!rental.isActive(), "Rental is no longer active after return");
    }

    private static void testPremiumMemberDepositWaived() {
        Depot depot = new Depot("Dovewing Car Rentals");
        Vehicle prado = new PremiumVehicle("Toyota Prado", "GR-5678-22", 600.00, 2000.00);
        Renter ama = new Renter("Ama Owusu", new MemberRenter());
        depot.addVehicle(prado);
        depot.addRenter(ama);

        depot.rent(ama, prado, 2);
        Rental rental = prado.getCurrentRental();
        check(rental.getCost() == 1200.0, "Premium 2-day cost is 1200.0");
        check(rental.getDeposit() == 0.0, "Member deposit is waived to 0.0");
    }

    private static void testDoubleBookingThrowsVehicleNotAvailable() {
        Depot depot = new Depot("Dovewing Car Rentals");
        Vehicle vitz = new EconomyVehicle("Toyota Vitz", "GT-1234-20", 150.00, 500.00);
        Renter kofi = new Renter("Kofi Mensah", new CasualRenter());
        Renter ama = new Renter("Ama Owusu", new MemberRenter());
        depot.addVehicle(vitz);
        depot.addRenter(kofi);
        depot.addRenter(ama);

        depot.rent(kofi, vitz, 3);
        boolean threw = false;
        try {
            depot.rent(ama, vitz, 2);
        } catch (VehicleNotAvailableException e) {
            threw = true;
        }
        check(threw, "Renting an already-rented vehicle throws VehicleNotAvailableException");
        check(depot.getRentals().size() == 1, "Failed second rental is not recorded");
    }

    private static void testReturnAvailableVehicleThrowsVehicleNotRented() {
        Depot depot = new Depot("Dovewing Car Rentals");
        Vehicle vitz = new EconomyVehicle("Toyota Vitz", "GT-1234-20", 150.00, 500.00);
        depot.addVehicle(vitz);

        boolean threw = false;
        try {
            depot.returnVehicle(vitz);
        } catch (VehicleNotRentedException e) {
            threw = true;
        }
        check(threw, "Returning an available vehicle throws VehicleNotRentedException");
    }

    private static void testInputValidationRejectsInvalidValues() {
        Depot depot = new Depot("Dovewing Car Rentals");
        Vehicle vitz = new EconomyVehicle("Toyota Vitz", "GT-1234-20", 150.00, 500.00);
        Renter kofi = new Renter("Kofi Mensah", new CasualRenter());
        depot.addVehicle(vitz);
        depot.addRenter(kofi);

        boolean threwZeroDays = false;
        try {
            depot.rent(kofi, vitz, 0);
        } catch (IllegalArgumentException e) {
            threwZeroDays = true;
        }
        check(threwZeroDays, "Zero rental days throws IllegalArgumentException");
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new IllegalStateException("FAIL: " + description);
        }
    }
}
