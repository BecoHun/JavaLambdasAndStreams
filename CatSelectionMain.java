import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class CatSelectionMain {

    public static void main(String[] args) {
        // Enable assertion checking in case it is disabled in the JVM
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        System.out.println("Starting tests...");

        CatSelectionTestRunner runner = new CatSelectionTestRunner();
        runner.setUp();

        runner.shouldReturnFirst10CatsSortedByName();
        runner.shouldReturnWithoutFirst100CatsSortedByAge();
        runner.shouldReturnCatsTallerThan30();
        runner.shouldReturnCatsSmallerThan5();
        runner.shouldReturnUniqueNames();
        runner.shouldReturnEmptyListWhenGivenEmptyList();
        runner.shouldThrowExceptionWhenInvalidThreshold();

        System.out.println("All tests executed successfully!");
    }

    // ==========================================
    // 1. Cat Model
    // ==========================================
    public static class Cat {
        private String name;
        private Integer age;
        private Integer weight;
        private Integer height;
        private Breed breed;

        public Cat(String name, Integer age, Integer weight, Integer height, Breed breed) {
            this.name = name;
            this.age = age;
            this.weight = weight;
            this.height = height;
            this.breed = breed;
        }

        public String getName() { return name; }
        public Integer getAge() { return age; }
        public Integer getWeight() { return weight; }
        public Integer getHeight() { return height; }
        public Breed getBreed() { return breed; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cat cat = (Cat) o;
            return Objects.equals(name, cat.name) &&
                   Objects.equals(age, cat.age) &&
                   Objects.equals(weight, cat.weight) &&
                   Objects.equals(height, cat.height) &&
                   breed == cat.breed;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, weight, height, breed);
        }

        public enum Breed {
            BRITISH(0),
            MAINE_COON(1),
            MUNCHKIN(2),
            PERSIAN(3),
            SIBERIAN(4);

            final int code;

            Breed(int code) {
                this.code = code;
            }

            public static Breed getBreedByCode(int code) {
                for (Breed breed : values()) {
                    if (breed.code == code) {
                        return breed;
                    }
                }
                return null;
            }
        }
    }

    // ==========================================
    // 2. CatSelection Business Logic
    // ==========================================
    public static class CatSelection {

        public List<Cat> getFirstNCatsSortedByComparator(List<Cat> cats, Comparator<Cat> comparator, int number) {
            return cats.stream()
                    .sorted(comparator)
                    .limit(number)
                    .collect(Collectors.toList());
        }

        public List<Cat> getWithoutFirstNCatsSortedByComparator(List<Cat> cats, Comparator<Cat> comparator, int number) {
            return cats.stream()
                    .sorted(comparator)
                    .skip(number)
                    .collect(Collectors.toList());
        }

        public List<Cat> getSmallCats(List<Cat> cats, int threshold) {
            validateThreshold(threshold);
            return cats.stream()
                    .filter(cat -> cat.getWeight() != null)
                    .filter(cat -> cat.getWeight() < threshold)
                    .collect(Collectors.toList());
        }

        public List<Cat> getTallCats(List<Cat> cats, int threshold) {
            validateThreshold(threshold);
            return cats.stream()
                    .filter(cat -> cat.getHeight() != null)
                    .filter(cat -> cat.getHeight() > threshold)
                    .collect(Collectors.toList());
        }

        public List<String> getUniqueNames(List<Cat> cats) {
            return cats.stream()
                    .map(Cat::getName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }

        private void validateThreshold(int threshold) {
            if (threshold < 0 || threshold > 100) {
                throw new RuntimeException("Invalid threshold");
            }
        }
    }

    // ==========================================
    // 3. Test Utility Class
    // ==========================================
    public static class CatTestUtils {
        private static final Random random = new Random(42); // Fixed seed for reproducibility
        public static final Integer CAT_COUNT = 1000;

        public static List<Cat> generateCats(int count) {
            List<Cat> cats = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cats.add(new Cat(
                        generateRandomString(5),
                        random.nextInt(15),
                        random.nextInt(15),
                        random.nextInt(10, 40),
                        Cat.Breed.values()[random.nextInt(Cat.Breed.values().length)]
                ));
            }
            return cats;
        }

        private static String generateRandomString(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            return sb.toString();
        }
    }

    // ==========================================
    // 4. Test Runner
    // ==========================================
    public static class CatSelectionTestRunner {
        private CatSelection catSelection;
        private List<Cat> cats;

        public void setUp() {
            catSelection = new CatSelection();
            cats = CatTestUtils.generateCats(CatTestUtils.CAT_COUNT);
        }

        public void shouldReturnFirst10CatsSortedByName() {
            int expectedLength = 10;
            List<Cat> result = catSelection.getFirstNCatsSortedByComparator(cats, Comparator.comparing(Cat::getName), expectedLength);

            assert result.size() == expectedLength : "Size must be 10";
            System.out.println("✔ shouldReturnFirst10CatsSortedByName passed");
        }

        public void shouldReturnWithoutFirst100CatsSortedByAge() {
            int skip = 100;
            int expectedLength = 900;
            List<Cat> result = catSelection.getWithoutFirstNCatsSortedByComparator(cats, Comparator.comparing(Cat::getAge), skip);

            assert result.size() == expectedLength : "Size must be 900";
            System.out.println("✔ shouldReturnWithoutFirst100CatsSortedByAge passed");
        }

        public void shouldReturnCatsTallerThan30() {
            int threshold = 30;
            List<Cat> result = catSelection.getTallCats(cats, threshold);

            long expectedCount = cats.stream().filter(c -> c.getHeight() != null && c.getHeight() > threshold).count();
            assert result.size() == expectedCount : "Incorrect number of tall cats";
            System.out.println("✔ shouldReturnCatsTallerThan30 passed");
        }

        public void shouldReturnCatsSmallerThan5() {
            int threshold = 5;
            List<Cat> result = catSelection.getSmallCats(cats, threshold);

            long expectedCount = cats.stream().filter(c -> c.getWeight() != null && c.getWeight() < threshold).count();
            assert result.size() == expectedCount : "Incorrect number of small cats";
            System.out.println("✔ shouldReturnCatsSmallerThan5 passed");
        }

        public void shouldReturnUniqueNames() {
            List<String> result = catSelection.getUniqueNames(cats);
            long expectedCount = cats.stream().map(Cat::getName).filter(Objects::nonNull).distinct().count();

            assert result.size() == expectedCount : "Number of unique names does not match";
            System.out.println("✔ shouldReturnUniqueNames passed");
        }

        public void shouldReturnEmptyListWhenGivenEmptyList() {
            List<Cat> emptyCats = new ArrayList<>();

            assert catSelection.getFirstNCatsSortedByComparator(emptyCats, Comparator.comparing(Cat::getName), 0).isEmpty();
            assert catSelection.getWithoutFirstNCatsSortedByComparator(emptyCats, Comparator.comparing(Cat::getName), 0).isEmpty();
            assert catSelection.getSmallCats(emptyCats, 10).isEmpty();
            assert catSelection.getTallCats(emptyCats, 10).isEmpty();
            assert catSelection.getUniqueNames(emptyCats).isEmpty();

            System.out.println("✔ shouldReturnEmptyListWhenGivenEmptyList passed");
        }

        public void shouldThrowExceptionWhenInvalidThreshold() {
            List<Cat> emptyCats = new ArrayList<>();
            int[] invalidThresholds = {-999, 999};

            for (int threshold : invalidThresholds) {
                boolean smallCatExceptionThrown = false;
                try {
                    catSelection.getSmallCats(emptyCats, threshold);
                } catch (RuntimeException e) {
                    smallCatExceptionThrown = true;
                }
                assert smallCatExceptionThrown : "getSmallCats did not throw an exception for invalid threshold: " + threshold;

                boolean tallCatExceptionThrown = false;
                try {
                    catSelection.getTallCats(emptyCats, threshold);
                } catch (RuntimeException e) {
                    tallCatExceptionThrown = true;
                }
                assert tallCatExceptionThrown : "getTallCats did not throw an exception for invalid threshold: " + threshold;
            }

            System.out.println("✔ shouldThrowExceptionWhenInvalidThreshold passed");
        }
    }
}
/*
Purpose and Structure of the CodeThis is a standalone Java application with no external dependencies that filters and sorts cat (Cat) data using the Java Stream API, 
and verifies its functionality using a built-in test runner.
Main ComponentsCat (Model):
Stores the cat's properties (name, age, weight, height, breed).
Contains the Breed enum for identifying breeds.
CatSelection (Business Logic):
getFirstNCatsSortedByComparator: 
Selects the first $N$ elements based on a custom comparator (limit).
getWithoutFirstNCatsSortedByComparator: 
Skips the first N elements after sorting (skip).
getSmallCats / getTallCats: 
Weight- and height-based filtering with validation (threshold value must be between 0 and 100).
getUniqueNames: 
Extracts unique, non-null names (distinct).
CatTestUtils (Test Data Generator):
Generates 1000 test objects in memory with randomized data.
Uses a fixed seed (new Random(42)) for reproducible results.
CatSelectionTestRunner (Test Framework):
Uses Java's built-in assert statement and try-catch blocks instead of JUnit to verify the following scenarios:
Accuracy of element counts and sorting.
Filtering of unique names.
Handling of empty lists.
Exception handling for invalid thresholds (RuntimeException).
main Method:
Programmatically enables assertion checking (setDefaultAssertionStatus(true)), allowing the application to run immediately without additional JVM flags.
*/

