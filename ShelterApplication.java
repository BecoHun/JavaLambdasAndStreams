import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ShelterApplication {

    // ==========================================
    // 1. MODEL CLASSES
    // ==========================================

    public static class Cat {
        private String name;
        private Integer age;
        private Breed breed;
        private Staff attendant;
        private LocalDate lastCheckUpDate;

        public enum Breed {
            BRITISH, MAINE_COON, MUNCHKIN, PERSIAN, SIBERIAN
        }

        public enum Staff {
            NANCY, CATHERINE, BOB, JACKSON, HELEN, JOHN, EDWARD
        }

        public Cat() {}

        public Cat(String name, Integer age, Breed breed, Staff attendant, LocalDate lastCheckUpDate) {
            this.name = name;
            this.age = age;
            this.breed = breed;
            this.attendant = attendant;
            this.lastCheckUpDate = lastCheckUpDate;
        }

        public static CatBuilder builder() {
            return new CatBuilder();
        }

        public static class CatBuilder {
            private String name;
            private Integer age;
            private Breed breed;
            private Staff attendant;
            private LocalDate lastCheckUpDate;

            public CatBuilder name(String name) { this.name = name; return this; }
            public CatBuilder age(Integer age) { this.age = age; return this; }
            public CatBuilder breed(Breed breed) { this.breed = breed; return this; }
            public CatBuilder attendant(Staff attendant) { this.attendant = attendant; return this; }
            public CatBuilder lastCheckUpDate(LocalDate lastCheckUpDate) { this.lastCheckUpDate = lastCheckUpDate; return this; }

            public Cat build() {
                return new Cat(name, age, breed, attendant, lastCheckUpDate);
            }
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public Breed getBreed() { return breed; }
        public void setBreed(Breed breed) { this.breed = breed; }
        public Staff getAttendant() { return attendant; }
        public void setAttendant(Staff attendant) { this.attendant = attendant; }
        public LocalDate getLastCheckUpDate() { return lastCheckUpDate; }
        public void setLastCheckUpDate(LocalDate lastCheckUpDate) { this.lastCheckUpDate = lastCheckUpDate; }
    }

    public static class ShelterRoom {
        private final List<Cat> cats;

        public ShelterRoom(List<Cat> cats) {
            this.cats = cats;
        }

        public List<Cat> getCats() {
            return cats;
        }
    }

    // ==========================================
    // 2. SERVICE CLASS
    // ==========================================

    public static class ShelterService {

        public void assignAttendants(List<ShelterRoom> rooms) {
            if (rooms == null) {
                return;
            }
            Cat.Staff[] staff = Cat.Staff.values();
            int index = 0;
            for (ShelterRoom room : rooms) {
                if (room == null || room.getCats() == null) {
                    continue;
                }
                for (Cat cat : room.getCats()) {
                    if (cat.getAttendant() == null) {
                        cat.setAttendant(staff[index % staff.length]);
                        index++;
                    }
                }
            }
        }

        public List<Cat> getCheckUpList(List<ShelterRoom> rooms, LocalDate date) {
            List<Cat> result = new ArrayList<>();
            if (rooms == null || date == null) {
                return result;
            }
            for (ShelterRoom room : rooms) {
                if (room == null || room.getCats() == null) {
                    continue;
                }
                for (Cat cat : room.getCats()) {
                    LocalDate lastCheckUpDate = cat.getLastCheckUpDate();
                    if (lastCheckUpDate != null && lastCheckUpDate.isBefore(date)) {
                        result.add(cat);
                    }
                }
            }
            return result;
        }

        public List<Cat> getCatsByBreed(List<ShelterRoom> rooms, Cat.Breed breed) {
            List<Cat> result = new ArrayList<>();
            if (rooms == null || breed == null) {
                return result;
            }
            for (ShelterRoom room : rooms) {
                if (room == null || room.getCats() == null) {
                    continue;
                }
                for (Cat cat : room.getCats()) {
                    if (breed.equals(cat.getBreed())) {
                        result.add(cat);
                    }
                }
            }
            return result;
        }
    }

    // ==========================================
    // 3. TEST UTILITY CLASS
    // ==========================================

    public static class CatTestUtils {
        public static final LocalDate TEST_DATE = LocalDate.now();
        public static final String TEST_NAME_WITH_ATTENDANT = "Alex0";
        private static final String TEST_NAME_BASE = "Alex";
        private static final Integer TEST_ROOM_COUNT = 3;
        private static final Integer TEST_CAT_COUNT = 3;
        private static final Integer THRESHOLD = 8;
        private static final Integer MAX_AGE = 15;
        private static final Integer START_DATE = 2020;
        private static final Integer END_DATE = 2023;
        private static final Random RANDOM = new Random();

        private CatTestUtils() {}

        private static String randomAlphabetic(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
            }
            return sb.toString();
        }

        public static List<Cat> generateCatsWithoutAttendant(int count) {
            List<Cat> cats = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cats.add(Cat.builder()
                        .name(randomAlphabetic(5))
                        .age(RANDOM.nextInt(MAX_AGE))
                        .breed(Cat.Breed.values()[RANDOM.nextInt(Cat.Breed.values().length)])
                        .lastCheckUpDate(createRandomDate(START_DATE, END_DATE))
                        .build());
            }
            return cats;
        }

        public static List<ShelterRoom> generateRooms(int count, int catsPerRoom, boolean nullableValues) {
            List<ShelterRoom> rooms = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                rooms.add(new ShelterRoom(
                        nullableValues ? generateCatsWithNullableValues(catsPerRoom) :
                                generateCatsWithoutAttendant(catsPerRoom)));
            }
            return rooms;
        }

        public static List<Cat> generateCatsWithNullableValues(int count) {
            List<Cat> cats = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cats.add(Cat.builder()
                        .name(getNullableName())
                        .age(getNullableAge())
                        .breed(getNullableBreed())
                        .lastCheckUpDate(getNullableDate())
                        .build());
            }
            return cats;
        }

        public static List<ShelterRoom> generateRoomsWithDefinedCats(Cat.Staff attendant) {
            List<ShelterRoom> rooms = new ArrayList<>();
            for (int i = 0; i < TEST_ROOM_COUNT; i++) {
                ShelterRoom room = new ShelterRoom(new ArrayList<>());
                for (int j = 0; j < TEST_CAT_COUNT; j++) {
                    Cat cat = Cat.builder()
                            .name(TEST_NAME_BASE + j)
                            .age(2)
                            .breed(Cat.Breed.values()[i])
                            .lastCheckUpDate(TEST_DATE.plusMonths(j))
                            .build();

                    if (TEST_NAME_WITH_ATTENDANT.equals(cat.getName())) {
                        cat.setAttendant(attendant);
                    }
                    room.getCats().add(cat);
                }
                rooms.add(room);
            }
            return rooms;
        }

        private static String getNullableName() {
            return RANDOM.nextInt(10) < THRESHOLD ? randomAlphabetic(5) : null;
        }

        private static Integer getNullableAge() {
            return RANDOM.nextInt(10) < THRESHOLD ? RANDOM.nextInt(MAX_AGE) : null;
        }

        private static Cat.Breed getNullableBreed() {
            return RANDOM.nextInt(10) < THRESHOLD ? Cat.Breed.values()[RANDOM.nextInt(Cat.Breed.values().length)] : null;
        }

        private static LocalDate getNullableDate() {
            return RANDOM.nextInt(10) < THRESHOLD ? createRandomDate(START_DATE, END_DATE) : null;
        }

        private static LocalDate createRandomDate(int startYear, int endYear) {
            int day = RANDOM.nextInt(1, 29);
            int month = RANDOM.nextInt(1, 13);
            int year = RANDOM.nextInt(startYear, endYear);
            return LocalDate.of(year, month, day);
        }
    }

    // ==========================================
    // 4. JUNIT-LIKE ASSERTION METHODS
    // ==========================================

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Condition was false, but expected true.");
        }
    }

    private static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("Condition was true, but expected false.");
        }
    }

    private static void assertNull(Object object) {
        if (object != null) {
            throw new AssertionError("Expected null value, but got: " + object);
        }
    }

    private static void assertNotNull(Object object) {
        if (object == null) {
            throw new AssertionError("Expected non-null value, but got null.");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected: <" + expected + ">, but actual was: <" + actual + ">");
        }
    }

    // ==========================================
    // 5. MAIN - RUNNING TESTS
    // ==========================================

    public static void main(String[] args) {
        System.out.println("--- Starting Tests ---");

        ShelterService shelterService = new ShelterService();
        int roomCount = 10;
        int catCount = 100;

        // Randomized Test 1
        System.out.print("Running: shouldAssignNonNullAttendants (Randomized)... ");
        List<ShelterRoom> rooms1 = CatTestUtils.generateRooms(roomCount, catCount, false);
        for (ShelterRoom room : rooms1) {
            for (Cat cat : room.getCats()) {
                assertNull(cat.getAttendant());
            }
        }
        shelterService.assignAttendants(rooms1);
        for (ShelterRoom room : rooms1) {
            for (Cat cat : room.getCats()) {
                assertNotNull(cat.getAttendant());
            }
        }
        System.out.println("OK");

        // Randomized Test 2
        System.out.print("Running: shouldGetCatListForCheckUpWithNullableDates... ");
        List<ShelterRoom> rooms2 = CatTestUtils.generateRooms(roomCount, catCount, true);
        List<Cat> checkUpList1 = shelterService.getCheckUpList(rooms2, CatTestUtils.TEST_DATE);
        assertNotNull(checkUpList1);
        assertFalse(checkUpList1.isEmpty());
        System.out.println("OK");

        // Randomized Test 3
        System.out.print("Running: shouldGetBritishCatsWhenNullableBreed... ");
        List<ShelterRoom> rooms3 = CatTestUtils.generateRooms(roomCount, catCount, true);
        List<Cat> britishCats1 = shelterService.getCatsByBreed(rooms3, Cat.Breed.MUNCHKIN);
        assertNotNull(britishCats1);
        assertFalse(britishCats1.isEmpty());
        System.out.println("OK");

        // Fixed Test 1
        System.out.print("Running: shouldAssignNonNullAttendants (Fixed)... ");
        List<ShelterRoom> rooms4 = CatTestUtils.generateRoomsWithDefinedCats(null);
        shelterService.assignAttendants(rooms4);
        for (ShelterRoom room : rooms4) {
            for (Cat cat : room.getCats()) {
                assertNotNull(cat.getAttendant());
            }
        }
        System.out.println("OK");

        // Fixed Test 2
        System.out.print("Running: shouldNotUpdateAttendants... ");
        for (Cat.Staff attendant : Cat.Staff.values()) {
            List<ShelterRoom> rooms = CatTestUtils.generateRoomsWithDefinedCats(attendant);
            shelterService.assignAttendants(rooms);
            for (ShelterRoom room : rooms) {
                for (Cat cat : room.getCats()) {
                    if (CatTestUtils.TEST_NAME_WITH_ATTENDANT.equals(cat.getName())) {
                        assertEquals(attendant, cat.getAttendant());
                    } else {
                        assertNotNull(cat.getAttendant());
                    }
                }
            }
        }
        System.out.println("OK");

        // Fixed Test 3
        System.out.print("Running: shouldGetCatListForCheckUp... ");
        List<ShelterRoom> rooms5 = CatTestUtils.generateRoomsWithDefinedCats(null);
        List<Cat> checkUpList2 = shelterService.getCheckUpList(rooms5, CatTestUtils.TEST_DATE.plusMonths(1).plusDays(1));
        assertNotNull(checkUpList2);
        assertEquals(6, checkUpList2.size());
        System.out.println("OK");

        // Fixed Test 4
        System.out.print("Running: shouldGetCatListForCheckUpDateExclusively... ");
        List<ShelterRoom> rooms6 = CatTestUtils.generateRoomsWithDefinedCats(null);
        List<Cat> checkUpList3 = shelterService.getCheckUpList(rooms6, CatTestUtils.TEST_DATE.plusMonths(1));
        assertEquals(3, checkUpList3.size());
        System.out.println("OK");

        // Fixed Test 5
        System.out.print("Running: shouldGetBritishCats... ");
        List<ShelterRoom> rooms7 = CatTestUtils.generateRoomsWithDefinedCats(null);
        List<Cat> british = shelterService.getCatsByBreed(rooms7, Cat.Breed.BRITISH);
        assertNotNull(british);
        assertEquals(3, british.size());
        System.out.println("OK");

        // Fixed Test 6
        System.out.print("Running: shouldGetEmptyList... ");
        List<ShelterRoom> rooms8 = CatTestUtils.generateRoomsWithDefinedCats(null);
        List<Cat> siberians = shelterService.getCatsByBreed(rooms8, Cat.Breed.SIBERIAN);
        assertNotNull(siberians);
        assertTrue(siberians.isEmpty());
        System.out.println("OK");

        // Fixed Test 7 & 8
        System.out.print("Running: shouldReturnEmptyListWhenGivenEmptyList... ");
        List<ShelterRoom> emptyRooms = new ArrayList<>();
        List<Cat> emptyCheckUp = shelterService.getCheckUpList(emptyRooms, LocalDate.now());
        List<Cat> emptyBreed = shelterService.getCatsByBreed(emptyRooms, null);
        assertTrue(emptyCheckUp.isEmpty());
        assertTrue(emptyBreed.isEmpty());
        shelterService.assignAttendants(emptyRooms);
        assertTrue(emptyRooms.isEmpty());
        System.out.println("OK");

        System.out.println("\n--- All tests completed successfully! ---");
    }
}
/*
This Java code implements an animal shelter cat management system along with its own custom test framework inside a single class (ShelterApplication).
Key Components and Functionality:
Data Model (Cat, ShelterRoom):
Cat: 
Represents a cat, storing its name, age, breed (Breed enum), attendant (Staff enum), and the date of its last medical check-up. 
It supports the Builder design pattern for easier instantiation.
ShelterRoom: 
Represents a room in the shelter, containing a list of cats assigned to it.
Business Logic (ShelterService):
assignAttendants: 
Cyclically assigns staff members to cats that do not yet have an assigned attendant.
getCheckUpList: 
Retrieves cats whose last check-up date was before a specified date (due for a check-up).
getCatsByBreed: 
Filters cats based on the specified breed.
Test Environment (CatTestUtils, Assertions, main):
CatTestUtils: 
A utility class for generating random or fixed test data (rooms, cats, dates), including the simulation of null values.
Custom Assertions: 
In the absence of an external testing framework (e.g., JUnit), it uses custom assertTrue, assertEquals, assertNotNull, etc., methods that throw an AssertionError upon failure.
main method: 
Executes all test cases (using both randomized and fixed data) to verify the functionality of ShelterService and prints the results to the console.
*/

