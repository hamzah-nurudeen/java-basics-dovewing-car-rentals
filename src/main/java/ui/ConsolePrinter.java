package ui;

import fleet.Rental;
import fleet.Vehicle;
import java.util.List;
import renter.Renter;

// Centralizes all console messages so formatting is separate from fleet logic.
public class ConsolePrinter {

    public void printRented(Vehicle vehicle, Renter renter, int days, double cost, double deposit) {
        System.out.println("Rented " + vehicle.getModel() + " (" + vehicle.getReg() + ") to "
                + renter.getName() + " for " + days + " days, cost GHS " + cost
                + ", deposit GHS " + deposit);
    }

    public void printReturned(Vehicle vehicle) {
        System.out.println("Returned " + vehicle.getModel() + " (" + vehicle.getReg() + ")");
    }

    public void printReport(String depotName, List<Vehicle> vehicles, List<Rental> rentals) {
        System.out.println();
        System.out.println("=== " + depotName + " - end of day ===");
        System.out.println("Fleet status:");
        for (Vehicle v : vehicles) {
            System.out.println("  " + v.getModel() + " (" + v.getReg() + ", "
                    + v.getCategory() + "): " + v.getStatus());
        }
        System.out.println("Active rentals:");
        for (Rental rt : rentals) {
            if (rt.isActive()) {
                System.out.println("  Rental #" + rt.getId() + ": " + rt.getVehicleReg()
                        + " -> " + rt.getRenterName() + " (" + rt.getDays() + " days)");
            }
        }
    }

    public void printError(String message) {
        System.out.println("Error: " + message);
    }
}
