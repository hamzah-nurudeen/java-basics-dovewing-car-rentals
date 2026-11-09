package renter;

// Member renter type: standard deposit is waived.
public class MemberRenter implements RenterType {

    @Override
    public RenterKind getKind() {
        return RenterKind.MEMBER;
    }

    @Override
    public boolean isDepositWaived() {
        return true;
    }
}
