import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class InsuranceCalc {

    // =========================================================================
    // 1. DOMAIN MODEL (Records, Enums, Interfaces)
    // =========================================================================

    public interface Subject {}

    public enum Currency { USD, EUR, GBP, AUD }

    public record RepeatablePayment(BigInteger amount, Currency currency, Period unit) {}

    public record Injury(String name, BigInteger curingCost, String reason, LocalDate date, Optional<Person> culprit)
            implements Comparable<Injury> {
        @Override
        public int compareTo(Injury o) {
            return this.date.compareTo(o.date);
        }
    }

    public record Employment(String company, String title, LocalDate startDate, Optional<LocalDate> endDate, Optional<RepeatablePayment> salary)
            implements Comparable<Employment> {
        @Override
        public int compareTo(Employment o) {
            return this.startDate.compareTo(o.startDate);
        }
    }

    public record Family(Set<Person> partners, Set<Person> children, Map<Currency, BigInteger> account) {}

    public record Person(
            String name,
            String surname,
            LocalDate birthDate,
            Optional<Family> family,
            Map<Currency, BigInteger> account,
            SortedSet<Employment> employmentHistory,
            Set<Car> carOwningHistory,
            SortedSet<Injury> injuries,
            SortedSet<Accommodation> accommodations
    ) implements Subject {}

    public record Car(
            String model,
            BigInteger price,
            LocalDate manufactureDate,
            LocalDate purchaseDate,
            Optional<LocalDate> soldDate
    ) implements Subject {
        public record Incident(LocalDate date, BigInteger ownDamageCost, BigInteger othersDamageCosts, Optional<Person> culprit) {}
    }

    public record Accommodation(
            String name,
            BigInteger price,
            BigInteger area,
            Integer rooms,
            LocalDate constructionDate,
            Optional<RepeatablePayment> rent,
            Optional<EmergencyStatus> emergencyStatus
    ) implements Subject, Comparable<Accommodation> {
        
        public enum Type { HOUSE, APARTMENT, ROOM }
        public enum EmergencyStatus { NONE, LOW, MEDIUM, HIGH, CRITICAL }

        @Override
        public int compareTo(Accommodation o) {
            return this.area.compareTo(o.area);
        }
    }

    // =========================================================================
    // 2. CALCULATOR BASE (InsuranceCoefficient & InsuranceCalculator)
    // =========================================================================

    public record InsuranceCoefficient(int coefficient) {
        public static final InsuranceCoefficient MAX = new InsuranceCoefficient(100);
        public static final InsuranceCoefficient MED = new InsuranceCoefficient(50);
        public static final InsuranceCoefficient MIN = new InsuranceCoefficient(0);

        public InsuranceCoefficient {
            if (coefficient < 0 || coefficient > 100) {
                throw new IllegalArgumentException("Coefficient must be in range [0; 100], but was " + coefficient);
            }
        }

        public static InsuranceCoefficient of(int coefficient) {
            return new InsuranceCoefficient(coefficient);
        }

        public static InsuranceCoefficient of(BigInteger coefficient) {
            return new InsuranceCoefficient(coefficient.intValueExact());
        }
    }

    @FunctionalInterface
    public interface InsuranceCalculator<S extends Subject> {
        Optional<InsuranceCoefficient> calculate(S entity);
    }

    // =========================================================================
    // 3. CALCULATION POLICIES (LAMBDA EXPRESSIONS)
    // =========================================================================

    public static final class PersonInsurancePolicies {
        private PersonInsurancePolicies() {}

        public static InsuranceCalculator<Person> childrenDependent(int childrenCountThreshold) {
            return person -> {
                if (person == null
                        || person.family().isEmpty()
                        || person.family().get().children() == null
                        || person.family().get().children().isEmpty()
                        || childrenCountThreshold <= 0) {
                    return Optional.of(InsuranceCoefficient.MIN);
                }
                int childrenCount = person.family().get().children().size();
                int coefficient = childrenCount * 100 / childrenCountThreshold;
                return Optional.of(InsuranceCoefficient.of(Math.min(coefficient, 100)));
            };
        }

        public static InsuranceCalculator<Person> employmentDependentInsurance(BigInteger salaryThreshold, Set<Currency> currencies) {
            return person -> {
                if (person == null || salaryThreshold == null || currencies == null
                        || person.employmentHistory() == null || person.employmentHistory().size() < 4
                        || person.account() == null || person.account().size() < 2
                        || (person.injuries() != null && !person.injuries().isEmpty())
                        || person.accommodations() == null || person.accommodations().isEmpty()) {
                    return Optional.empty();
                }

                Employment currentEmployment = person.employmentHistory().stream()
                        .filter(e -> e != null && e.endDate().isEmpty())
                        .findFirst()
                        .orElse(null);

                if (currentEmployment == null || currentEmployment.salary().isEmpty()) {
                    return Optional.empty();
                }

                RepeatablePayment salary = currentEmployment.salary().get();
                if (!currencies.contains(salary.currency()) || salary.amount().compareTo(salaryThreshold) < 0) {
                    return Optional.empty();
                }

                return Optional.of(InsuranceCoefficient.MED);
            };
        }

        public static InsuranceCalculator<Person> accommodationEmergencyInsurance(Set<Accommodation.EmergencyStatus> statuses) {
            return person -> {
                if (person == null || statuses == null || person.accommodations() == null || person.accommodations().isEmpty()) {
                    return Optional.empty();
                }
                Accommodation accommodation = person.accommodations().stream()
                        .min(Comparator.comparing(Accommodation::area))
                        .orElse(null);

                if (accommodation == null || accommodation.emergencyStatus().isEmpty()) {
                    return Optional.empty();
                }
                Accommodation.EmergencyStatus status = accommodation.emergencyStatus().get();
                if (!statuses.contains(status)) {
                    return Optional.empty();
                }
                int totalStatuses = Accommodation.EmergencyStatus.values().length;
                int coefficient = 100 * (totalStatuses - status.ordinal()) / totalStatuses;
                return Optional.of(InsuranceCoefficient.of(coefficient));
            };
        }
    }

    public static final class CarInsurancePolicies {
        private CarInsurancePolicies() {}

        public static InsuranceCalculator<Car> ageDependentInsurance(LocalDate baseDate) {
            return car -> {
                if (car == null || baseDate == null || car.manufactureDate() == null) {
                    return Optional.empty();
                }
                LocalDate manufactureDate = car.manufactureDate();

                if (!manufactureDate.isBefore(baseDate.minusYears(1))) {
                    return Optional.of(InsuranceCoefficient.MAX);
                }
                if (!manufactureDate.isBefore(baseDate.minusYears(5))) {
                    return Optional.of(InsuranceCoefficient.of(70));
                }
                if (!manufactureDate.isBefore(baseDate.minusYears(10))) {
                    return Optional.of(InsuranceCoefficient.of(30));
                }
                return Optional.of(InsuranceCoefficient.MIN);
            };
        }

        public static InsuranceCalculator<Car> priceAndOwnershipOfFreshCarInsurance(LocalDate baseDate, BigInteger priceThreshold, Period owningThreshold) {
            return car -> {
                if (car == null || baseDate == null || priceThreshold == null || owningThreshold == null
                        || car.soldDate().isPresent() || car.price() == null || car.purchaseDate() == null) {
                    return Optional.empty();
                }
                if (car.price().compareTo(priceThreshold) < 0) {
                    return Optional.empty();
                }
                if (car.purchaseDate().plus(owningThreshold).isBefore(baseDate)) {
                    return Optional.empty();
                }
                BigInteger twiceThreshold = priceThreshold.multiply(BigInteger.valueOf(2));
                BigInteger threeTimesThreshold = priceThreshold.multiply(BigInteger.valueOf(3));

                if (car.price().compareTo(threeTimesThreshold) >= 0) {
                    return Optional.of(InsuranceCoefficient.MAX);
                }
                if (car.price().compareTo(twiceThreshold) >= 0) {
                    return Optional.of(InsuranceCoefficient.MED);
                }
                return Optional.of(InsuranceCoefficient.MIN);
            };
        }
    }

    public static final class AccommodationInsurancePolicies {
        private AccommodationInsurancePolicies() {}

        public static InsuranceCalculator<Accommodation> rentDependentInsurance(BigInteger divider) {
            return accommodation -> {
                if (accommodation == null || divider == null || divider.signum() <= 0 || accommodation.rent().isEmpty()) {
                    return Optional.empty();
                }
                var rent = accommodation.rent().get();
                if (!Period.ofMonths(1).equals(rent.unit())
                        || rent.currency() != Currency.USD
                        || rent.amount() == null
                        || rent.amount().signum() <= 0) {
                    return Optional.empty();
                }
                int coefficient = rent.amount().multiply(BigInteger.valueOf(100)).divide(divider).intValue();
                return Optional.of(InsuranceCoefficient.of(Math.min(coefficient, 100)));
            };
        }
    }

    // =========================================================================
    // 4. MAIN METHOD (TEST AND EXECUTION)
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== Running Insurance Calculator Test ===");

        // --- 1. Testing Car ---
        Car car = new Car(
                "Tesla Model 3",
                BigInteger.valueOf(45000),
                LocalDate.of(2023, 5, 10),
                LocalDate.of(2023, 6, 1),
                Optional.empty()
        );

        LocalDate today = LocalDate.of(2024, 1, 1);
        InsuranceCalculator<Car> carAgePolicy = CarInsurancePolicies.ageDependentInsurance(today);
        Optional<InsuranceCoefficient> carCoeff = carAgePolicy.calculate(car);

        System.out.println("Tesla car insurance coefficient (based on age): "
                + carCoeff.map(c -> c.coefficient() + "%").orElse("Not calculable"));

        // --- 2. Testing Person / Children count ---
        Person child1 = new Person("Johnny", "Smith", LocalDate.of(2015, 1, 1),
                Optional.empty(), Map.of(), new TreeSet<>(), Set.of(), new TreeSet<>(), new TreeSet<>());
        Person child2 = new Person("Pete", "Smith", LocalDate.of(2018, 5, 5),
                Optional.empty(), Map.of(), new TreeSet<>(), Set.of(), new TreeSet<>(), new TreeSet<>());

        Family family = new Family(Set.of(), Set.of(child1, child2), Map.of());
        
        Person parent = new Person(
                "John", "Smith", LocalDate.of(1985, 3, 15),
                Optional.of(family), Map.of(), new TreeSet<>(), Set.of(), new TreeSet<>(), new TreeSet<>()
        );

        InsuranceCalculator<Person> childrenPolicy = PersonInsurancePolicies.childrenDependent(4);
        Optional<InsuranceCoefficient> personCoeff = childrenPolicy.calculate(parent);

        System.out.println("Coefficient calculated based on children (2 children / threshold of 4): "
                + personCoeff.map(c -> c.coefficient() + "%").orElse("Not calculable"));

        // --- 3. Testing Accommodation rent ---
        Accommodation apartment = new Accommodation(
                "Downtown Apartment",
                BigInteger.valueOf(150000),
                BigInteger.valueOf(65),
                2,
                LocalDate.of(2010, 1, 1),
                Optional.of(new RepeatablePayment(BigInteger.valueOf(800), Currency.USD, Period.ofMonths(1))),
                Optional.of(Accommodation.EmergencyStatus.NONE)
        );

        InsuranceCalculator<Accommodation> rentPolicy = AccommodationInsurancePolicies.rentDependentInsurance(BigInteger.valueOf(1000));
        Optional<InsuranceCoefficient> accomCoeff = rentPolicy.calculate(apartment);

        System.out.println("Accommodation insurance coefficient based on rent: "
                + accomCoeff.map(c -> c.coefficient() + "%").orElse("Not calculable"));
    }
}
/*
Purpose of the Code
This Java program implements a system for calculating insurance risk/discount coefficients (InsuranceCoefficient). 
The code determines a value between 0% and 100% based on data from various subjects (persons, cars, accommodations).
Key Structures and Functionality
Domain Model (Records and Enums):
Subject: 
A common interface for all insurable entities (Person, Car, Accommodation).
Data Models: 
Immutable Java records store relevant data (e.g., Employment, Injury, Family, RepeatablePayment).
Calculator Core:
InsuranceCoefficient: 
A percentage indicator restricted to a range of 0–100 (with predefined MIN, MED, and MAX constants).
InsuranceCalculator<S>: 
A generic functional interface (@FunctionalInterface) that calculates the coefficient for any Subject (Optional<InsuranceCoefficient>).
Calculation Rules (Policies classes):
PersonInsurancePolicies: 
Rules for individuals (e.g., based on the number of dependent children, employment status/salary, or the emergency status of their property).
CarInsurancePolicies: 
Rules for vehicles (e.g., based on vehicle age, purchase price, and ownership duration).
AccommodationInsurancePolicies: 
Rules for properties (e.g., proportional to monthly rent).
main Method:
Demonstrates the usage of the system using sample data (a Tesla car, a parent with 2 children, and a downtown apartment).
*/

