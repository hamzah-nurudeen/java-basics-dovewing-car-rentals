// Dovewing Car Rentals - fleet and rental tracker entry point.
public class RentalTracker {

    public static void main(String[] args) {
        Depot d = new Depot("Dovewing Car Rentals");
        ConsolePrinter printer = new ConsolePrinter();

        Vehicle vitz = new EconomyVehicle("Toyota Vitz", "GT-1234-20", 150.00, 500.00);
        Vehicle prado = new PremiumVehicle("Toyota Prado", "GR-5678-22", 600.00, 2000.00);
        d.addVehicle(vitz);
        d.addVehicle(prado);

        Renter kofi = new Renter("Kofi Mensah", new CasualRenter());
        Renter ama = new Renter("Ama Owusu", new MemberRenter());
        d.addRenter(kofi);
        d.addRenter(ama);

        // a normal day
        d.rent(kofi, vitz, 3);
        d.rent(ama, prado, 2);
        d.returnVehicle(prado);

        // ... and the sort of thing that used to make them keep a paper backup:
        try {
            d.rent(ama, vitz, 2);
        } catch (VehicleNotAvailableException e) {
            printer.printError(e.getMessage());
        }

        d.printReport();
    }
}
