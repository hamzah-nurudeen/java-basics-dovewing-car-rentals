// Thrown when attempting to return a vehicle that is not currently rented.
public class VehicleNotRentedException extends RuntimeException {

    public VehicleNotRentedException(String message) {
        super(message);
    }
}
