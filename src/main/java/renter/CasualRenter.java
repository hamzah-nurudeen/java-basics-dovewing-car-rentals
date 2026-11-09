package renter;

// Casual renter type: standard deposit is required.
public class CasualRenter implements RenterType {

    @Override
    public String getKind() {
        return "CASUAL";
    }

    @Override
    public boolean isDepositWaived() {
        return false;
    }
}
