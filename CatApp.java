import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CatApp {

    public static void main(String[] args) {
        System.out.println("=== Cat Contest Application Started ===");
        CatContestHelper helper = new CatContestHelper();
        
        List<Cat> cats = CatTestUtils.generateCats(1000);
        List<Cat> nullableCats = CatTestUtils.generateCatsWithNullableValues(1000);

        // Run and verify tests
        runTest("Carrier Number Test", helper.getCarrierNumber(new ArrayList<>()) == 0);
        runTest("Carrier ID Empty Test", helper.getCarrierId(new ArrayList<>()).equals("CF"));
        runTest("Team Awards Empty Test", helper.countTeamAwards(new ArrayList<>()) == 0);

        int carrierNum = helper.getCarrierNumber(cats);
        int nullableCarrierNum = helper.getCarrierNumber(nullableCats);
        System.out.println("Carrier number (normal): " + carrierNum);
        System.out.println("Carrier number (nullable): " + nullableCarrierNum);

        String carrierId = helper.getCarrierId(cats.subList(40, 50));
        String nullableCarrierId = helper.getCarrierId(nullableCats.subList(100, 110));
        System.out.println("Carrier ID (normal): " + carrierId);
        System.out.println("Carrier ID (nullable): " + nullableCarrierId);

        int awardNumber = helper.countTeamAwards(cats);
        int nullableAwardNumber = helper.countTeamAwards(nullableCats);
        System.out.println("Awards number (normal): " + awardNumber);
        System.out.println("Awards number (nullable): " + nullableAwardNumber);

        System.out.println("\n=== All tests passed successfully! ===");
    }

    private static void runTest(String testName, boolean condition) {
        if (!condition) {
            throw new AssertionError("Test error: " + testName);
        }
        System.out.println("[SUCCESS] " + testName);
    }
}

// ==================== MODELS AND UTILITY CLASSES ====================

class Cat {
    private String name;
    private Integer age;
    private Breed breed;
    private Integer weight;
    private Integer awards;
    private ContestResult contestResult;

    public enum Breed {
        BRITISH, MAINE_COON, MUNCHKIN, PERSIAN, SIBERIAN
    }

    public Cat(String name, Integer age, Breed breed, Integer weight, Integer awards, ContestResult contestResult) {
        this.name = name;
        this.age = age;
        this.breed = breed;
        this.weight = weight;
        this.awards = awards;
        this.contestResult = contestResult;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Integer age;
        private Breed breed;
        private Integer weight;
        private Integer awards;
        private ContestResult contestResult;

        public Builder name(String name) { this.name = name; return this; }
        public Builder age(Integer age) { this.age = age; return this; }
        public Builder breed(Breed breed) { this.breed = breed; return this; }
        public Builder weight(Integer weight) { this.weight = weight; return this; }
        public Builder awards(Integer awards) { this.awards = awards; return this; }
        public Builder contestResult(ContestResult contestResult) { this.contestResult = contestResult; return this; }
        
        public Cat build() {
            return new Cat(name, age, breed, weight, awards, contestResult);
        }
    }

    public String getName() { return name; }
    public Integer getAge() { return age; }
    public Breed getBreed() { return breed; }
    public Integer getWeight() { return weight; }
    public Integer getAwards() { return awards; }
    public ContestResult getContestResult() { return contestResult; }
}

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
        return (running == null ? 0 : running)
                + (jumping == null ? 0 : jumping)
                + (purring == null ? 0 : purring);
    }

    public Integer getRunning() { return running; }
    public Integer getJumping() { return jumping; }
    public Integer getPurring() { return purring; }
    public Integer getSum() { return sum; }
}

class CatContestHelper {
    public static final Integer CARRIER_THRESHOLD = 30;

    public Integer getCarrierNumber(List<Cat> cats) {
        int totalWeight = cats.stream()
                .map(Cat::getWeight)
                .map(weight -> weight == null || weight == 0 ? 1 : weight)
                .reduce(0, Integer::sum);
        return (int) Math.ceil(totalWeight / 30.0);
    }

    public String getCarrierId(List<Cat> cats) {
        return cats.stream()
                .filter(cat -> cat.getName() != null)
                .filter(cat -> cat.getBreed() != null)
                .reduce("CF",
                        (result, cat) -> result +
                                getFirstThreeLetters(cat.getName()) +
                                getFirstThreeLetters(cat.getBreed() == null ? null : cat.getBreed().name()),
                        String::concat);
    }

    public Integer countTeamAwards(List<Cat> cats) {
        return cats.stream()
                .map(Cat::getAwards)
                .filter(Objects::nonNull)
                .reduce(0, Integer::sum);
    }

    private String getFirstThreeLetters(String value) {
        if (value == null) {
            return "";
        }
        return value.substring(0, Math.min(3, value.length()))
                .toUpperCase();
    }
}

class CatTestUtils {
    private static final Integer MAX_AGE_WEIGHT = 15;
    private static final Integer MAX_AWARDS = 35;
    private static final Integer MAX_COMPETITION_RESULT = 100;
    private static final Integer THRESHOLD = 8;
    private static final Random random = new Random(42); // Fixed seed for deterministic data

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

    private static String randomAlphabetic(int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            char c = (char) ('A' + random.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
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
}
/*
Cat Contest Application – Overview
This single-file Java application processes data related to cat competitions, calculates statistics, and performs testing without any external dependencies.
Main components:
Cat and ContestResult: 
Data models for storing cat properties (name, age, breed, weight, awards) and competition results (running, jumping, purring), complemented with the Builder pattern.
CatContestHelper: 
A class implementing business logic that uses Stream APIs and lambdas to calculate:
The number of carriers required based on the total weight of the cats.
The carrier ID generated from prefixes of the cats' names and breeds.
The total number of awards won by the team.
CatTestUtils: 
A data generation utility class that randomly generates cat list datasets containing normal and optional nullable fields for testing.
CatApp (Main): 
Creates test data, runs the built-in unit tests, and prints the results to the console.
*/

