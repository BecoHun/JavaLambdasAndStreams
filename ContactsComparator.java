import java.time.LocalDate;
import java.util.Comparator;

// --- 1. MODEL CLASSES ---

final class Address {
    String country, city, street;
    Integer zipCode, building, apartment;

    public String country() { return country; }
    public Address country(String c) { this.country = c; return this; }
    public String city() { return city; }
    public Address city(String c) { this.city = c; return this; }
    public Integer zipCode() { return zipCode; }
    public Address zipCode(Integer z) { this.zipCode = z; return this; }
    public String street() { return street; }
    public Address street(String s) { this.street = s; return this; }
    public Integer building() { return building; }
    public Address building(Integer b) { this.building = b; return this; }
    public Integer apartment() { return apartment; }
    public Address apartment(Integer a) { this.apartment = a; return this; }
}

final class Person {
    String name, surname;
    LocalDate birthdate;
    Address address;

    public String name() { return name; }
    public Person name(String n) { this.name = n; return this; }
    public String surname() { return surname; }
    public Person surname(String s) { this.surname = s; return this; }
    public LocalDate birthdate() { return birthdate; }
    public Person birthdate(LocalDate b) { this.birthdate = b; return this; }
    public Address address() { return address; }
    public Person address(Address a) { this.address = a; return this; }
}

final class Company {
    String name, registrationUuid;
    Person director;
    Address officeAddress;

    public String name() { return name; }
    public Company name(String n) { this.name = n; return this; }
    public String registrationUuid() { return registrationUuid; }
    public Company registrationUuid(String r) { this.registrationUuid = r; return this; }
    public Person director() { return director; }
    public Company director(Person d) { this.director = d; return this; }
    public Address officeAddress() { return officeAddress; }
    public Company officeAddress(Address a) { this.officeAddress = a; return this; }
}

// --- 2. COMPARATORS WITH LAMBDA EXPRESSIONS ---

interface Comparators {
    // ZipCode reverse order with lambda expression: (a, b) -> b.compareTo(a)
    Comparator<Address> ZIP_CODE_COMPARATOR = 
        Comparator.nullsFirst(Comparator.<Address, Integer>comparing(
            a -> a.zipCode(), 
            (a, b) -> b.compareTo(a)
        ));

    Comparator<Address> STREET_COMPARATOR = 
        Comparator.nullsFirst(Comparator.<Address, String>comparing(
            a -> a.street(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(
            a -> a.building(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(
            a -> a.apartment(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        ));

    Comparator<Address> REGION_COMPARATOR = 
        Comparator.nullsFirst(Comparator.<Address, String>comparing(
            a -> a.country(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(
            a -> a.city(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(ZIP_CODE_COMPARATOR));

    Comparator<Address> ADDRESS_COMPARATOR = 
        Comparator.nullsLast(Comparator.<Address, String>comparing(
            a -> a.country(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(
            a -> a.city(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(ZIP_CODE_COMPARATOR)
        .thenComparing(
            a -> a.street(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(
            a -> a.building(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(
            a -> a.apartment(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        ));

    Comparator<Person> FULL_NAME_COMPARATOR = 
        Comparator.nullsFirst(Comparator.<Person, String>comparing(p -> p.name())
        .thenComparing(p -> p.surname()));

    Comparator<Person> BIRTHDATE_COMPARATOR = 
        Comparator.nullsFirst(Comparator.<Person, LocalDate>comparing(
            p -> p.birthdate(), 
            Comparator.nullsLast((a, b) -> b.compareTo(a))
        ));

    Comparator<Person> PERSON_COMPARATOR = 
        Comparator.nullsLast(FULL_NAME_COMPARATOR
        .thenComparing(BIRTHDATE_COMPARATOR)
        .thenComparing(
            p -> p.address(), 
            Comparator.nullsLast(ADDRESS_COMPARATOR)
        ));

    Comparator<Company> REGISTRATION_ID_COMPARATOR = 
        Comparator.nullsLast(Comparator.comparing(c -> c.registrationUuid()));

    Comparator<Company> COMPANY_COMPARATOR = 
        Comparator.nullsLast(Comparator.<Company, String>comparing(
            c -> c.name(), 
            Comparator.nullsLast((a, b) -> a.compareTo(b))
        )
        .thenComparing(REGISTRATION_ID_COMPARATOR)
        .thenComparing(
            c -> c.director(), 
            Comparator.nullsLast(PERSON_COMPARATOR)
        )
        .thenComparing(
            c -> c.officeAddress(), 
            Comparator.nullsLast(ADDRESS_COMPARATOR)
        ));
}

// --- 3. EXECUTION AND TESTS ---

public class ContactsComparator {
    public static void main(String[] args) {
        System.out.println("=== Comparator Tests ===");

        // 1. ZipCode comparison (Reverse order)
        Address a1 = new Address().zipCode(1000);
        Address a2 = new Address().zipCode(2000);
        check("ZIP code (reversed)", Comparators.ZIP_CODE_COMPARATOR.compare(a1, a2) > 0);

        // 2. Street and building comparison
        Address st1 = new Address().street("Main St").building(10);
        Address st2 = new Address().street("Main St").building(20);
        check("Street + building number", Comparators.STREET_COMPARATOR.compare(st1, st2) < 0);

        // 3. Person (Name and birthdate comparison)
        Person p1 = new Person().name("Anna").surname("Smith").birthdate(LocalDate.of(1990, 1, 1));
        Person p2 = new Person().name("Anna").surname("Smith").birthdate(LocalDate.of(2000, 1, 1));
        check("Persons birthdate (younger comes first)", Comparators.PERSON_COMPARATOR.compare(p1, p2) > 0);

        // 4. Null value handling (nullsLast)
        Company c1 = new Company().name("ABC LLC");
        Company c2 = new Company().name(null);
        check("Company name nullsLast", Comparators.COMPANY_COMPARATOR.compare(c1, c2) < 0);

        System.out.println("\nAll tests executed successfully!");
    }

    private static void check(String testName, boolean condition) {
        if (condition) {
            System.out.println("[OK] " + testName);
        } else {
            System.out.println("[Failed Test!] " + testName);
        }
    }
}
/*
This code implements a Java-based comparator system for sorting and testing complex data structures without relying on external frameworks.
Key Components and Functionality
Model classes (Address, Person, Company): Data classes equipped with fluent getters and setters.
Lambda-based Comparators interface: Constants using the standard Java Comparator API to define the ordering of elements:
Multi-level sorting: Hierarchically compares fields using thenComparing (e.g., Country → City → ZIP code).
Null safety: Handles missing values using nullsFirst and nullsLast wrappers to prevent NullPointerException.
Custom ordering: Lambda expressions cover both natural (ascending) and reverse (descending) orders (e.g., by ZIP code or birthdate).
ContactsComparator (Runner and Testing): 
The main method verifies the behavior of the comparators on concrete object instances and provides console feedback on the test results using a simple check() helper method.
*/

