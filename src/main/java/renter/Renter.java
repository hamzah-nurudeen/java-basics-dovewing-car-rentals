package renter;

import java.util.ArrayList;
import java.util.List;

// Represents a customer who can rent vehicles from the depot.
// Uses composition with RenterType rather than inheritance so a casual
// renter can later become a member without losing their ID or rental history.
public class Renter {
    private static int counter = 1;

    private final int id;
    private final String name;
    private RenterType type;
    private final List<String> history = new ArrayList<String>();

    public Renter(String name, RenterType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Renter name must not be blank.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Renter type must not be null.");
        }
        this.id = counter;
        counter++;
        this.name = name;
        this.type = type;
    }

    public static Renter casual(String name) {
        return new Renter(name, new CasualRenter());
    }

    public static Renter member(String name) {
        return new Renter(name, new MemberRenter());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RenterType getType() {
        return type;
    }

    public void setType(RenterType type) {
        if (type == null) {
            throw new IllegalArgumentException("Renter type must not be null.");
        }
        this.type = type;
    }

    public RenterKind getKind() {
        return type.getKind();
    }

    public boolean isDepositWaived() {
        return type.isDepositWaived();
    }

    public void addHistoryEntry(String entry) {
        history.add(entry);
    }

    // Returns a copy so outside callers cannot modify the renter's history directly.
    public List<String> getHistory() {
        return new ArrayList<String>(history);
    }
}
