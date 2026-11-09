// Thrown when attempting to rent a vehicle that is already rented out.
public class VehicleNotAvailableException extends RuntimeException {

    public VehicleNotAvailableException(String message) {
        super(message);
    }
}
