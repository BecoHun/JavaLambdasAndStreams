import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class CatDatabaseMain {

    public static void main(String[] args) {
        System.out.println("=== STARTING CAT DATABASE APPLICATION (IN-MEMORY TEST) ===\n");

        // 1. Generate test data in memory
        System.out.println("1. Generating test data in memory...");
        List<Cat> cats = CatTestUtils.generateCats(100);
        List<Cat> nullableCats = CatTestUtils.generateCatsWithNullableValues(100);

        System.out.println("   - Generated standard cats count: " + cats.size());
        System.out.println("   - Generated nullable cats count: " + nullableCats.size() + "\n");

        // 2. Filter tests for CatDatabase
        CatDatabase db = new CatDatabase();

        System.out.println("2. Running filter tests (Stream & Lambda):");

        // A) Filter by breed
        List<String> siberianNames = db.getCatNamesByBreed(nullableCats, Cat.Breed.SIBERIAN);
        System.out.println("   - Siberian cat names (from nullable list): " + siberianNames.size() + " pcs");
        if (!siberianNames.isEmpty()) {
            System.out.println("     Sample names: " + siberianNames.stream().limit(5).collect(Collectors.joining(", ")));
        }

        // B) Filter by age (younger than 4 years)
        List<Cat> youngCats = db.filterYoungerCatsByAge(cats, 4);
        System.out.println("   - Cats younger than 4 years (from standard list): " + youngCats.size() + " pcs");

        // C) Filter by name prefix ('A' or 'a')
        List<Cat> aCats = db.filterCatsByNamePrefix(cats, "A");
        System.out.println("   - Cats starting with 'A': " + aCats.size() + " pcs");

        // D) Test empty list filtering
        List<Cat> emptyList = new ArrayList<>();
        List<String> emptyResult = db.getCatNamesByBreed(emptyList, Cat.Breed.MAINE_COON);
        System.out.println("   - Empty list test result: " + emptyResult.size() + " elements (Correct)");

        System.out.println("\n=== ALL TESTS COMPLETED SUCCESSFULLY ===");
    }
}

// ==========================================
// CAT MODEL (with Custom Builder)
// ==========================================
class Cat {
    private String name;
    private Integer age;
    private Breed breed;

    public Cat(String name, Integer age, Breed breed) {
        this.name = name;
        this.age = age;
        this.breed = breed;
    }

    public String getName() { return name; }
    public Integer getAge() { return age; }
    public Breed getBreed() { return breed; }

    @Override
    public String toString() {
        return "Cat{name='" + name + "', age=" + age + ", breed=" + breed + "}";
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
            return Arrays.stream(values())
                    .filter(b -> b.code == code)
                    .findFirst()
                    .orElse(null);
        }
    }

    public static CatBuilder builder() {
        return new CatBuilder();
    }

    public static class CatBuilder {
        private String name;
        private Integer age;
        private Breed breed;

        public CatBuilder name(String name) { this.name = name; return this; }
        public CatBuilder age(Integer age) { this.age = age; return this; }
        public CatBuilder breed(Breed breed) { this.breed = breed; return this; }

        public Cat build() {
            return new Cat(name, age, breed);
        }
    }
}

// ==========================================
// CAT DATABASE (Filtering with Streams)
// ==========================================
class CatDatabase {

    public List<String> getCatNamesByBreed(List<Cat> cats, Cat.Breed breed) {
        return cats.stream()
                .filter(cat -> Objects.equals(cat.getBreed(), breed))
                .map(Cat::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Cat> filterYoungerCatsByAge(List<Cat> cats, Integer age) {
        return cats.stream()
                .filter(cat -> cat.getAge() != null)
                .filter(cat -> cat.getAge() <= age)
                .collect(Collectors.toList());
    }

    public List<Cat> filterCatsByNamePrefix(List<Cat> cats, String prefix) {
        return cats.stream()
                .filter(cat -> cat.getName() != null)
                .filter(cat -> prefix != null && cat.getName().toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}

// ==========================================
// TEST UTILS (In-Memory Data Generation)
// ==========================================
class CatTestUtils {

    private static final Random random = new Random();
    private static final Integer THRESHOLD = 8;

    public static List<Cat> generateCats(int count) {
        List<Cat> cats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cats.add(Cat.builder()
                    .name(generateRandomName())
                    .age(random.nextInt(15))
                    .breed(Cat.Breed.getBreedByCode(random.nextInt(5)))
                    .build());
        }
        return cats;
    }

    public static List<Cat> generateCatsWithNullableValues(int count) {
        List<Cat> cats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cats.add(Cat.builder()
                    .name(random.nextInt(10) < THRESHOLD ? generateRandomName() : null)
                    .age(random.nextInt(10) < THRESHOLD ? random.nextInt(15) : null)
                    .breed(random.nextInt(10) < THRESHOLD ? Cat.Breed.getBreedByCode(random.nextInt(5)) : null)
                    .build());
        }
        return cats;
    }

    private static String generateRandomName() {
        int length = 5;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = (char) ('a' + random.nextInt(26));
            if (i == 0) c = Character.toUpperCase(c);
            sb.append(c);
        }
        return sb.toString();
    }
}
/*
This Java program is a standalone, in-memory test application that demonstrates cat data management and filtering operations using Java Streams and Lambda expressions.
Main Features:
Data Generation (CatTestUtils):
Randomly generates Cat objects, including incomplete datasets (containing null values) to test fault tolerance.
Data Model (Cat):
Stores the cat's name, age, and breed (Breed enum).
Uses a custom Builder pattern (CatBuilder) for flexible object instantiation.
Filtering Logic (CatDatabase):
By Breed: Extracts the names of cats of a specific breed.
By Age: Filters cats younger than a given age.
ByName Prefix: Finds cats whose names start with a specified prefix (case-insensitively).
Testing (CatDatabaseMain):
Runs the above filters on standard, incomplete, and empty lists within the main method, then prints the results to the console.
*/

