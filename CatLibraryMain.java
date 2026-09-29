import java.util.*;
import java.util.stream.Collectors;

public class CatLibraryMain {
    public static void main(String[] args) {
        System.out.println("=== Starting CatLibrary Tests ===");
        
        CatLibrary library = new CatLibrary();
        List<Cat> cats = CatTestUtils.generateCats(CatTestUtils.CAT_COUNT);
        List<Cat> nullableCats = CatTestUtils.generateCatsWithNullableValues(CatTestUtils.CAT_COUNT);

        // 1. Test: Map cats by name
        testMapCatsByName(library, cats, nullableCats);

        // 2. Test: Map cats by breed
        testMapCatsByBreed(library, cats, nullableCats);

        // 3. Test: Join cat names by breed
        testMapCatNamesByBreed(library, cats, nullableCats);

        // 4. Test: Average results by breed
        testMapAverageResultByBreed(library, cats, nullableCats);

        // 5. Test: Ordered set by contest results
        testGetOrderedCatsByContestResults(library, cats);

        System.out.println("\nAll tests passed successfully!");
    }

    private static void testMapCatsByName(CatLibrary library, List<Cat> cats, List<Cat> nullableCats) {
        Map<String, Cat> catMap = library.mapCatsByName(cats);
        assertEq(1000, catMap.size(), "Cats map size should be 1000");
        assertNull(catMap.get(null), "Null key should not exist");

        Map<String, Cat> nullableMap = library.mapCatsByName(nullableCats);
        assertNull(nullableMap.get(null), "Null key should not exist in nullable map");
        System.out.println("[OK] testMapCatsByName passed");
    }

    private static void testMapCatsByBreed(CatLibrary library, List<Cat> cats, List<Cat> nullableCats) {
        Map<Cat.Breed, Set<Cat>> catMap = library.mapCatsByBreed(cats);
        assertEq(6, catMap.keySet().size(), "Breed keySet size should be 6");
        int totalSum = catMap.values().stream().mapToInt(Collection::size).sum();
        assertEq(CatTestUtils.CAT_COUNT, totalSum, "Total cats count mismatch");

        Map<Cat.Breed, Set<Cat>> nullableMap = library.mapCatsByBreed(nullableCats);
        assertEq(6, nullableMap.keySet().size(), "Nullable breed keySet size should be 6");
        System.out.println("[OK] testMapCatsByBreed passed");
    }

    private static void testMapCatNamesByBreed(CatLibrary library, List<Cat> cats, List<Cat> nullableCats) {
        Map<Cat.Breed, String> catMap = library.mapCatNamesByBreed(cats.subList(0, 25));
        assertEq(6, catMap.keySet().size(), "Sublist breed keySet size should be 6");
        assertTrue(catMap.get(Cat.Breed.BRITISH).startsWith("Cat names:"), "Should start with 'Cat names:'");

        Map<Cat.Breed, String> nullableMap = library.mapCatNamesByBreed(nullableCats.subList(0, 25));
        assertEq(5, nullableMap.keySet().size(), "Nullable sublist breed keySet size should be 5");
        System.out.println("[OK] testMapCatNamesByBreed passed");
    }

    private static void testMapAverageResultByBreed(CatLibrary library, List<Cat> cats, List<Cat> nullableCats) {
        Map<Cat.Breed, Double> averageMap = library.mapAverageResultByBreed(cats);
        assertTrue(!averageMap.isEmpty(), "Average map should not be empty");

        Map<Cat.Breed, Double> nullableAverageMap = library.mapAverageResultByBreed(nullableCats);
        assertTrue(!nullableAverageMap.isEmpty(), "Nullable average map should not be empty");
        System.out.println("[OK] testMapAverageResultByBreed passed");
    }

    private static void testGetOrderedCatsByContestResults(CatLibrary library, List<Cat> cats) {
        SortedSet<Cat> catSet = library.getOrderedCatsByContestResults(cats);
        assertEq(1000, catSet.size(), "SortedSet size should be 1000");

        Iterator<Cat> iterator = catSet.iterator();
        Cat previous = iterator.hasNext() ? iterator.next() : null;
        while (iterator.hasNext()) {
            Cat current = iterator.next();
            if (previous != null) {
                boolean condition = previous.getContestResult().getSum() >= current.getContestResult().getSum();
                assertTrue(condition, "Cats should be ordered by sum descending");
            }
            previous = current;
        }
        System.out.println("[OK] testGetOrderedCatsByContestResults passed");
    }

    // Helper methods for assertions
    private static void assertEq(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " -> Expected: " + expected + ", but got: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Condition not met: " + message);
        }
    }

    private static void assertNull(Object object, String message) {
        if (object != null) {
            throw new AssertionError(message + " -> Non-null value received: " + object);
        }
    }
}

// --- Model Classes and Helpers ---

class ContestResult {
    private final Integer running;
    private final Integer jumping;
    private final Integer purring;
    private final Integer sum;

    public ContestResult(Integer running, Integer jumping, Integer purring) {
        this.running = running;
        this.jumping = jumping;
        this.purring = purring;
        this.sum = countResults(running, jumping, purring);
    }

    public Integer countResults(Integer running, Integer jumping, Integer purring) {
        int r = running != null ? running : 0;
        int j = jumping != null ? jumping : 0;
        int p = purring != null ? purring : 0;
        return r + j + p;
    }

    public Integer getRunning() { return running; }
    public Integer getJumping() { return jumping; }
    public Integer getPurring() { return purring; }
    public Integer getSum() { return sum; }
}

class Cat {
    private String name;
    private Integer age;
    private Breed breed;
    private Integer weight;
    private Integer awards;
    private ContestResult contestResult;

    public Cat(String name, Integer age, Breed breed, Integer weight, Integer awards, ContestResult contestResult) {
        this.name = name;
        this.age = age;
        this.breed = breed;
        this.weight = weight;
        this.awards = awards;
        this.contestResult = contestResult;
    }

    public String getName() { return name; }
    public Integer getAge() { return age; }
    public Breed getBreed() { return breed; }
    public Integer getWeight() { return weight; }
    public Integer getAwards() { return awards; }
    public ContestResult getContestResult() { return contestResult; }

    public static CatBuilder builder() {
        return new CatBuilder();
    }

    public static class CatBuilder {
        private String name;
        private Integer age;
        private Breed breed;
        private Integer weight;
        private Integer awards;
        private ContestResult contestResult;

        public CatBuilder name(String name) { this.name = name; return this; }
        public CatBuilder age(Integer age) { this.age = age; return this; }
        public CatBuilder breed(Breed breed) { this.breed = breed; return this; }
        public CatBuilder weight(Integer weight) { this.weight = weight; return this; }
        public CatBuilder awards(Integer awards) { this.awards = awards; return this; }
        public CatBuilder contestResult(ContestResult contestResult) { this.contestResult = contestResult; return this; }
        
        public Cat build() {
            return new Cat(name, age, breed, weight, awards, contestResult);
        }
    }

    enum Breed {
        BRITISH, MAINE_COON, MUNCHKIN, PERSIAN, SIBERIAN, SPHYNX
    }
}

class CatLibrary {
    static {
        Locale.setDefault(Locale.Category.FORMAT, Locale.US);
    }

    public Map<String, Cat> mapCatsByName(List<Cat> cats) {
        return cats.stream()
                .filter(cat -> cat.getName() != null)
                .filter(cat -> !cat.getName().isEmpty())
                .collect(Collectors.toMap(Cat::getName, cat -> cat, (c1, c2) -> c1));
    }

    public Map<Cat.Breed, Set<Cat>> mapCatsByBreed(List<Cat> cats) {
        return cats.stream()
                .filter(cat -> cat.getBreed() != null)
                .collect(Collectors.groupingBy(Cat::getBreed, Collectors.toSet()));
    }

    public Map<Cat.Breed, String> mapCatNamesByBreed(List<Cat> cats) {
        return cats.stream()
                .filter(cat -> cat.getBreed() != null)
                .filter(cat -> cat.getName() != null)
                .filter(cat -> !cat.getName().isEmpty())
                .collect(Collectors.groupingBy(
                        Cat::getBreed, Collectors.mapping(Cat::getName,
                                Collectors.joining(", ", "Cat names: ", "."))));
    }

    public Map<Cat.Breed, Double> mapAverageResultByBreed(List<Cat> cats) {
        return cats.stream()
                .filter(cat -> cat.getBreed() != null)
                .filter(cat -> cat.getContestResult() != null)
                .collect(Collectors.groupingBy(Cat::getBreed, Collectors.averagingInt(
                        cat -> cat.getContestResult().getSum())));
    }

    public SortedSet<Cat> getOrderedCatsByContestResults(List<Cat> cats) {
        Comparator<Cat> comparator = Comparator.comparingInt((Cat cat) -> cat.getContestResult().getSum())
                .reversed()
                .thenComparing(Cat::getName, Comparator.nullsLast(Comparator.naturalOrder()));
        return cats.stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(comparator)));
    }
}

class CatTestUtils {
    public static final Integer CAT_COUNT = 1000;
    private static final Integer MAX_AGE_WEIGHT = 15;
    private static final Integer MAX_AWARDS = 35;
    private static final Integer MAX_COMPETITION_RESULT = 100;
    private static final Integer THRESHOLD = 8;
    private static final Random random = new Random(42); // Fixed seed for reproducibility

    private CatTestUtils() {}

    public static List<Cat> generateCats(int count) {
        List<Cat> cats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cats.add(Cat.builder()
                    .name(randomAlphabetic(5))
                    .age(random.nextInt(MAX_AGE_WEIGHT))
                    .breed(Cat.Breed.values()[random.nextInt(Cat.Breed.values().length)])
                    .weight(random.nextInt(MAX_AGE_WEIGHT))
                    .awards(random.nextInt(MAX_AWARDS))
                    .contestResult(new ContestResult(
                            random.nextInt(MAX_COMPETITION_RESULT),
                            random.nextInt(MAX_COMPETITION_RESULT),
                            random.nextInt(MAX_COMPETITION_RESULT)))
                    .build());
        }
        return cats;
    }

    public static List<Cat> generateCatsWithNullableValues(int count) {
        List<Cat> cats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cats.add(Cat.builder()
                    .name(getNullableName())
                    .age(getNullableAgeWeight())
                    .breed(getNullableBreed())
                    .weight(getNullableAgeWeight())
                    .awards(getNullableAwards())
                    .contestResult(new ContestResult(
                            getNullableCompetitionResult(),
                            getNullableCompetitionResult(),
                            getNullableCompetitionResult()))
                    .build());
        }
        return cats;
    }

    private static String getNullableName() {
        if (random.nextInt(10) < THRESHOLD) {
            return randomAlphabetic(5);
        }
        return null;
    }

    private static Cat.Breed getNullableBreed() {
        if (random.nextInt(10) < THRESHOLD) {
            return Cat.Breed.values()[random.nextInt(Cat.Breed.values().length)];
        }
        return null;
    }

    private static Integer getNullableAgeWeight() {
        if (random.nextInt(10) < THRESHOLD) {
            return random.nextInt(MAX_AGE_WEIGHT);
        }
        return null;
    }

    private static Integer getNullableAwards() {
        if (random.nextInt(10) < THRESHOLD) {
            return random.nextInt(MAX_AWARDS);
        }
        return null;
    }

    private static Integer getNullableCompetitionResult() {
        if (random.nextInt(10) < THRESHOLD) {
            return random.nextInt(MAX_COMPETITION_RESULT);
        }
        return null;
    }

    private static String randomAlphabetic(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
/*
This Java code implements and tests a cat management module (CatLibrary) using the Java Stream API.
Key Components:
Data Model (Cat, ContestResult):
Classes representing cats (Cat) and their competition results (ContestResult). 
The Cat class uses the Builder pattern for instantiation and supports categorization by breed (Breed enum).
Business Logic (CatLibrary):
mapCatsByName: 
Groups cats by name into a Map (filtering out null names).
mapCatsByBreed: 
Groups cats by breed into sets (Set).
mapCatNamesByBreed: 
Concatenates cat names by breed into a single string (e.g., "Cat names: ... ").
mapAverageResultByBreed: 
Calculates the average competition scores by breed.
getOrderedCatsByContestResults: 
Sorts cats into a TreeSet in descending order by score, then in ascending order by name.
Testing and Helpers (CatLibraryMain, CatTestUtils):
CatTestUtils: 
Randomly generates 1,000 cats with complete as well as missing (null) data for testing purposes.
CatLibraryMain: 
Runs the 5 main test cases, verifying filtering, grouping, and sorting correctness using custom assertion methods (assertEq, assertTrue, assertNull).
*/

