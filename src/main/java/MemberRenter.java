// Member renter type: standard deposit is waived.
public class MemberRenter implements RenterType {

    @Override
    public String getKind() {
        return "MEMBER";
    }

    @Override
    public boolean isDepositWaived() {
        return true;
    }
}
