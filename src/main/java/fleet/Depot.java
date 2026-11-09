package fleet;

import exceptions.VehicleNotAvailableException;
import exceptions.VehicleNotRentedException;
import java.util.ArrayList;
import java.util.List;
import renter.Renter;
import ui.ConsolePrinter;

// Coordinates the depot's fleet, registered renters, and rental operations.
// Enforces that a vehicle can only be rented when available and only returned when rented.
public class Depot {
    private final String name;
    private final List<Vehicle> vehicles = new ArrayList<Vehicle>();
    private final List<Renter> renters = new ArrayList<Renter>();
    private final List<Rental> rentals = new ArrayList<Rental>();
    private final ConsolePrinter printer = new ConsolePrinter();

    public Depot(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Depot name must not be blank.");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addVehicle(Vehicle v) {
        if (v == null) {
            throw new IllegalArgumentException("Vehicle must not be null.");
        }
        vehicles.add(v);
    }

    public void addRenter(Renter r) {
        if (r == null) {
            throw new IllegalArgumentException("Renter must not be null.");
        }
        renters.add(r);
    }

    public void rent(Renter r, Vehicle v, int days) {
        if (r == null) {
            throw new IllegalArgumentException("Renter must not be null.");
        }
        if (v == null) {
            throw new IllegalArgumentException("Vehicle must not be null.");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("Rental days must be at least 1.");
        }
        if (v.isRented()) {
            throw new VehicleNotAvailableException(
                    v.getModel() + " (" + v.getReg() + ") is already rented.");
        }
        double cost = v.calculateCost(days);
        double deposit = v.calculateDeposit(r);
        Rental rental = new Rental(r, v, days, cost, deposit);
        v.assignRental(rental);
        r.addHistoryEntry("Rented " + v.getModel());
        rentals.add(rental);
        printer.printRented(v, r, days, cost, deposit);
    }

    public void returnVehicle(Vehicle v) {
        if (v == null) {
            throw new IllegalArgumentException("Vehicle must not be null.");
        }
        if (v.isAvailable()) {
            throw new VehicleNotRentedException(
                    v.getModel() + " (" + v.getReg() + ") is not currently rented.");
        }
        v.clearRental();
        printer.printReturned(v);
    }

    public int getFleetSize() {
        return vehicles.size();
    }

    public int getRentedCount() {
        int count = 0;
        for (Vehicle v : vehicles) {
            if (v.isRented()) {
                count++;
            }
        }
        return count;
    }

    public int getAvailableCount() {
        int count = 0;
        for (Vehicle v : vehicles) {
            if (v.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    // Defensive copies prevent outside code from mutating the depot's internal lists.
    public List<Vehicle> getVehicles() {
        return new ArrayList<Vehicle>(vehicles);
    }

    public List<Renter> getRenters() {
        return new ArrayList<Renter>(renters);
    }

    public List<Rental> getRentals() {
        return new ArrayList<Rental>(rentals);
    }

    public void printReport() {
        printer.printReport(name, vehicles, rentals);
    }
}
