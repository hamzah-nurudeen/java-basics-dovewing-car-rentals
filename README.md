# Dovewing Car Rentals — Fleet and Rental Tracker

This repository contains the diagnosis, rebuilt codebase, and design notes for the **Dovewing Car Rentals** fleet and rental tracker.

See [DIAGNOSIS.md](DIAGNOSIS.md) for the defect analysis, risk ranking, and class diagram.

---

## 1. Running the Program (JDK 21, Core Java Only)

**Directly with `javac` / `java`:**
```bash
javac --release 21 -d target/classes src/main/java/*.java
java -cp target/classes RentalTracker
```

**With Maven:**
```bash
mvn compile
mvn exec:java -Dexec.mainClass=RentalTracker
```

### Console Output (Before vs. After)

**Before (Original Code):**
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
  - `Vehicle` is an `abstract` class with `private` fields and abstract methods `getCategory()`, `calculateCost(int days)`, and `calculateDeposit(Renter renter)`. `EconomyVehicle`, `PremiumVehicle`, and `CommercialVanVehicle` extend `Vehicle`.
  - `RenterType` is an interface (`getKind()`, `isDepositWaived()`) implemented by `CasualRenter` and `MemberRenter` and **composed** inside `Renter` (`private RenterType type`).
- **Alternative rejected**:
  - Using `if/else` chains on strings (`"ECONOMY"`, `"PREMIUM"`, `"MEMBER"`) inside `Depot.rent()`, or using inheritance for `Renter` (`CasualRenter extends Renter`).
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

### Decision 3: Encapsulated State Transitions and Derived Counts
- **What we chose**:
  - All fields on `Vehicle`, `Renter`, `Rental`, and `Depot` are `private`.
  - `Vehicle.assignRental(Rental)` and `Vehicle.clearRental()` are package-private with no public setters.
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

## 3. How to Add a New Vehicle Class (Commercial Van Guide & Improvement)

Next quarter Dovewing is adding a **Commercial Van** class with its own deposit rules.

### With the Rebuilt Design
1. **Create one new file**: `src/main/java/CommercialVanVehicle.java` extending `Vehicle`:
   ```java
   public class CommercialVanVehicle extends Vehicle {

       public CommercialVanVehicle(String model, String reg, double dailyRate, double baseDeposit) {
           super(model, reg, dailyRate, baseDeposit);
       }

       @Override
       public String getCategory() {
           return "COMMERCIAL_VAN";
       }

       @Override
       public double calculateCost(int days) {
           return getDailyRate() * days;
       }

       @Override
       public double calculateDeposit(Renter renter) {
           if (renter.isDepositWaived()) {
               return getBaseDeposit() * 0.5; // Members pay 50% cargo deposit on vans
           }
           return getBaseDeposit();
       }
   }
   ```
2. **Existing files modified**: **Zero** (`Vehicle.java`, `EconomyVehicle.java`, `PremiumVehicle.java`, `Renter.java`, `RenterType.java`, `Rental.java`, `Depot.java`, and `ConsolePrinter.java` require 0 changes).

### What the ORIGINAL Code Would Have Forced Us to Change
- In the original `RentalTracker.java`, adding `"COMMERCIAL_VAN"` required editing `Depot.rent()` (lines 113–136) to add another `else if (v.category.equals("COMMERCIAL_VAN"))` branch and repeating the cost calculation, deposit waiver check, status assignment, and print statement.
- If someone created a `"COMMERCIAL_VAN"` vehicle without editing `Depot.rent()`, the original code silently skipped the `if / else if` branches, left the van `AVAILABLE`, and created an `ACTIVE` rental with **cost GHS 0.0 and deposit GHS 0.0**.
