package renter;

// Casual renter type: standard deposit is required.
public class CasualRenter implements RenterType {

    @Override
    public RenterKind getKind() {
        return RenterKind.CASUAL;
    }

    @Override
    public boolean isDepositWaived() {
        return false;
    }
}
