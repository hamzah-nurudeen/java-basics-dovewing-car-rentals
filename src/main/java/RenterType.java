// Defines how a renter's membership status affects deposits.
// Composed inside Renter so a customer can upgrade from casual to member
// without replacing the Renter object or losing their rental history.
public interface RenterType {

    String getKind();

    boolean isDepositWaived();
}
