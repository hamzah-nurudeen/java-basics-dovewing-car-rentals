# Dovewing Car Rentals — Fleet and Rental Tracker

This repository contains the diagnosis, rebuilt codebase, verification checks, and design notes for the **Dovewing Car Rentals** fleet and rental tracker.

See [DIAGNOSIS.md](DIAGNOSIS.md) for the full defect analysis, business risk ranking, and class diagram.

---

## 1. Repository Structure & Running the Program (JDK 21, Core Java Only)

```text
.
├── DIAGNOSIS.md                            # Pre-refactor defect analysis & class diagram
├── README.md                               # Design decisions & extensibility notes
├── pom.xml                                 # Maven build configuration (Java 21)
└── src/
    ├── main/java/
    │   ├── RentalTracker.java              # Main entry point
    │   ├── exceptions/
    │   │   ├── VehicleNotAvailableException.java
    │   │   └── VehicleNotRentedException.java
    │   ├── fleet/
    │   │   ├── Vehicle.java
    │   │   ├── VehicleCategory.java
    │   │   ├── VehicleStatus.java
    │   │   ├── EconomyVehicle.java
    │   │   ├── PremiumVehicle.java
    │   │   ├── Rental.java
    │   │   ├── RentalStatus.java
    │   │   └── Depot.java
    │   ├── renter/
    │   │   ├── RenterType.java
    │   │   ├── RenterKind.java
    │   │   ├── CasualRenter.java
    │   │   ├── MemberRenter.java
    │   │   └── Renter.java
    │   └── ui/
    │       └── ConsolePrinter.java
    └── test/java/
        └── DepotTest.java                  # Executable verification suite (core Java)
```

### Compile and Run the Main Program (Plain `javac` / `java`)
```bash
javac --release 21 -d target/classes src/main/java/RentalTracker.java src/main/java/exceptions/*.java src/main/java/fleet/*.java src/main/java/renter/*.java src/main/java/ui/*.java
java -cp target/classes RentalTracker
```

### Compile and Run the Verification Checks (`DepotTest`)
```bash
javac --release 21 -d target/test-classes -cp target/classes src/test/java/DepotTest.java
java -cp "target/classes;target/test-classes" DepotTest
```
*(On macOS/Linux, replace `;` with `:` in `-cp`.)*

### Compile and Run with Maven (`pom.xml` included)
```bash
mvn compile
mvn exec:java -Dexec.mainClass=RentalTracker
```

### Console Output (Before vs. After)

**Before (Original Starter Code):**
Allowed `vitz` (`GT-1234-20`) to be rented to Ama while already out with Kofi, leaving two active rentals for the same car:
```text
Rented Toyota Vitz (GT-1234-20) to Kofi Mensah for 3 days, cost GHS 450.0, deposit GHS 500.0
Rented Toyota Prado (GR-5678-22) to Ama Owusu for 2 days, cost GHS 1200.0, deposit GHS 0.0
Returned Toyota Prado (GR-5678-22)
Rented Toyota Vitz (GT-1234-20) to Ama Owusu for 2 days, cost GHS 300.0, deposit GHS 0.0

=== Dovewing Car Rentals - end of day ===
Fleet status:
  Toyota Vitz (GT-1234-20, ECONOMY): RENTED
  Toyota Prado (GR-5678-22, PREMIUM): AVAILABLE
Active rentals:
  Rental #1: GT-1234-20 -> Kofi Mensah (3 days)
  Rental #3: GT-1234-20 -> Ama Owusu (2 days)
```

**After (Rebuilt Code):**
Blocks the double-booking of `GT-1234-20` with `VehicleNotAvailableException` and keeps only Kofi's active rental:
```text
Rented Toyota Vitz (GT-1234-20) to Kofi Mensah for 3 days, cost GHS 450.0, deposit GHS 500.0
Rented Toyota Prado (GR-5678-22) to Ama Owusu for 2 days, cost GHS 1200.0, deposit GHS 0.0
Returned Toyota Prado (GR-5678-22)
Error: Toyota Vitz (GT-1234-20) is already rented.

=== Dovewing Car Rentals - end of day ===
Fleet status:
  Toyota Vitz (GT-1234-20, ECONOMY): RENTED
  Toyota Prado (GR-5678-22, PREMIUM): AVAILABLE
Active rentals:
  Rental #1: GT-1234-20 -> Kofi Mensah (3 days)
```

---

## 2. Key Design Decisions (and Rejected Alternatives)

### Decision 1: Inheritance for Vehicles (`abstract class Vehicle`) vs. Interface + Composition for Renters (`RenterType` inside `Renter`)
- **What we chose**:
  - `Vehicle` is an `abstract` class with `private` fields and abstract methods `getCategory()`, `calculateCost(int days)`, and `calculateDeposit(Renter renter)`. `EconomyVehicle` and `PremiumVehicle` extend `Vehicle`.
  - `RenterType` is an interface (`getKind()`, `isDepositWaived()`) implemented by `CasualRenter` and `MemberRenter` and **composed** inside `Renter` (`private RenterType type`).
  - Category, status, and renter-kind identifiers are centralized in enums (`VehicleCategory`, `VehicleStatus`, `RentalStatus`, `RenterKind`) rather than scattered magic strings.
- **Alternative rejected**:
  - Using `if/else` chains on raw strings (`"ECONOMY"`, `"PREMIUM"`, `"MEMBER"`) inside `Depot.rent()`, or using inheritance for `Renter` (`CasualRenter extends Renter`).
- **Why**:
  - A physical vehicle's class never changes over its lifetime (a Toyota Vitz is always an economy car), so inheritance (*is-a*) fits `Vehicle` and keeps economy pricing isolated from premium pricing.
  - A customer (`Kofi Mensah`), however, can start as a casual renter and later become a member. Composing `RenterType` inside `Renter` lets Kofi upgrade via `kofi.setType(new MemberRenter())` without creating a new `Renter` object or losing his `id` and rental `history`.

### Decision 2: Single Source of Truth for Status (`Vehicle.currentRental` + Derived `Rental.isActive()`)
- **What we chose**:
  - Removed `Vehicle.status` (`"AVAILABLE"` / `"RENTED"`) and `Rental.status` (`"ACTIVE"` / `"RETURNED"`).
  - `Vehicle` holds **one** private reference: `private Rental currentRental` (`null` when available).
  - `Vehicle.isAvailable()` returns `currentRental == null`; `Vehicle.isRented()` returns `currentRental != null`.
  - `Rental.isActive()` returns `vehicle.getCurrentRental() == this`.
- **Alternative rejected**:
  - Keeping two status fields (`Vehicle.status` and `Rental.status`) and trying to keep them synchronized inside `Depot`.
- **Why**:
  - Whenever two fields store the same fact, a bug can update one and leave the other stale — which is how the original code allowed a car to show as `AVAILABLE` and have an `ACTIVE` rental at the same time.
  - By deriving `isAvailable()`, `isRented()`, and `Rental.isActive()` from the single `Vehicle.currentRental` field, a contradictory state is impossible.

### Decision 3: Package-Private Encapsulation (`fleet` Package) and Derived Counts
- **What we chose**:
  - All fields on `Vehicle`, `Renter`, `Rental`, and `Depot` are `private`.
  - `Vehicle`, `Rental`, and `Depot` live together in the `fleet` package, where `Vehicle.assignRental(Rental)` and `Vehicle.clearRental()` are **package-private** with no public setters — so outside classes (`RentalTracker`, `ui`, `renter`) cannot flip vehicle status.
  - `Depot.getFleetSize()`, `Depot.getRentedCount()`, and `Depot.getAvailableCount()` are computed from `vehicles`, and `getVehicles()`, `getRenters()`, and `getRentals()` return defensive `ArrayList` copies.
- **Alternative rejected**:
  - Public setters or manual counter fields.
- **Why**:
  - Prevents outside code from flipping vehicle status, altering pricing, or mutating the depot's internal lists.

### Decision 4: Custom Exceptions (`VehicleNotAvailableException`, `VehicleNotRentedException`) and Separated Console Output (`ConsolePrinter`)
- **What we chose**:
  - `Depot.rent()` throws `VehicleNotAvailableException` if `v.isRented()` is `true`.
  - `Depot.returnVehicle()` throws `VehicleNotRentedException` if `v.isAvailable()` is `true`.
  - `ConsolePrinter` handles console output, and `RentalTracker.main()` catches and reports exceptions at the UI boundary.
- **Alternative rejected**:
  - Printing warning messages inside `Depot` and continuing execution.
- **Why**:
  - Throwing an exception stops the invalid operation immediately before a bogus `Rental` record can be created.

---

## 3. How the Commercial-Van Class Will Be Added Next Quarter

When Dovewing introduces its **Commercial Van** class next quarter:

1. **Add `COMMERCIAL_VAN` to `VehicleCategory`** and **create one new subclass `CommercialVanVehicle` extending `Vehicle`** in `src/main/java/fleet/`:
   - Override `getCategory()` to return `VehicleCategory.COMMERCIAL_VAN`.
   - Override `calculateCost(int days)` and `calculateDeposit(Renter renter)` with the commercial van's specific deposit rule.
2. **Existing files needing zero changes**: `Vehicle.java`, `EconomyVehicle.java`, `PremiumVehicle.java`, `Renter.java`, `RenterType.java`, `Rental.java`, `Depot.java`, and `ConsolePrinter.java` require **zero edits** because `Depot.rent()` delegates cost and deposit calculation polymorphically to `Vehicle`.

### What the ORIGINAL Starter Code Would Have Forced Us to Change
In the untouched starter code (`git show e09ead6:src/main/java/RentalTracker.java`), `Depot.rent()` branched on raw category strings and fell through silently when a category was not `"ECONOMY"` or `"PREMIUM"`:
```java
if (v.category.equals("ECONOMY")) {
    cost = v.dailyRate * days;
    deposit = v.deposit;
    if (r.kind.equals("MEMBER")) {
        deposit = 0;
    }
    v.status = "RENTED";
    System.out.println(...);
} else if (v.category.equals("PREMIUM")) {
    // duplicated block ...
}
r.history.add("Rented " + v.model);
rentals.add(new Rental(r.name, v.reg, days, cost, deposit));
```
Adding a commercial van in the original code required modifying `Depot.rent()` directly to add another `else if` branch. If a developer added a van without updating `Depot.rent()`, the original code left `v.status` as `"AVAILABLE"` while still executing `rentals.add(...)` with `cost = 0` and `deposit = 0`.

---

## 4. One Improvement Beyond the Brief: Input Validation at Domain Boundaries

- **What we added**: Fail-fast input validation with descriptive `IllegalArgumentException` messages in `Vehicle` (rejecting blank model/registration or negative daily rate/base deposit), `Renter` (rejecting blank name or null `RenterType`), and `Depot` (rejecting null vehicles/renters or `days <= 0` in `rent()`).
- **Why it helps Dovewing**: In the original code, passing `0` or `-2` days to `Depot.rent()` silently created an `ACTIVE` rental with zero or negative cost (`GHS -1200.0`), corrupting daily revenue totals. Validating inputs at construction and rental time prevents bad data from entering the depot.
