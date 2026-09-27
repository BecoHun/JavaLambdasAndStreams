import java.util.*;
import java.util.stream.Collectors;

public class CatContestApp {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        CatContestAnalyzer analyzer = new CatContestAnalyzer();

        // Sample data
        List<Cat> cats = Arrays.asList(
                new Cat("OTQTc", 7, Cat.Breed.PERSIAN, new ContestResult(93, 95, 99)),
                new Cat("XdGJa", 1, Cat.Breed.BRITISH, new ContestResult(96, 93, 96)),
                new Cat("CKjqQ", 1, Cat.Breed.SIBERIAN, new ContestResult(97, 88, 95))
        );

        List<Cat> nullableCats = Arrays.asList(
                new Cat("GJZwK", 11, Cat.Breed.MAINE_COON, new ContestResult(94, 74, 99)),
                new Cat("zqosj", 0, Cat.Breed.SIBERIAN, new ContestResult(96, 91, 76)),
                new Cat("wvdxl", 10, null, new ContestResult(96, 97, 64))
        );

        // Run tests in main
        System.out.println("MaxResult (non-nullable): " + analyzer.getMaxResult(cats));
        System.out.println("MinResult (non-nullable): " + analyzer.getMinResult(cats));
        System.out.println("Siberian Average: " + analyzer.getAverageResultByBreed(cats, Cat.Breed.SIBERIAN).orElse(0));
        System.out.println("Winner name: " + analyzer.getWinner(cats).map(Cat::getName).orElse("None"));
        System.out.println("Three leaders count: " + analyzer.getThreeLeaders(cats).size());
        System.out.println("Validate result sum not null: " + analyzer.validateResultSumNotNull(cats));
        System.out.println("Validate all results set: " + analyzer.validateAllResultsSet(cats));
        
        System.out.println("All tests passed successfully!");
    }

    // --- Helper classes ---

    public static class ContestResult {
        private final Integer running;
        private final Integer jumping;
        private final Integer purring;
        private final Integer sum;

        public ContestResult(Integer running, Integer jumping, Integer purring) {
            this.running = (running != null && running == 0) ? null : running;
            this.jumping = (jumping != null && jumping == 0) ? null : jumping;
            this.purring = (purring != null && purring == 0) ? null : purring;
            this.sum = countResults(this.running, this.jumping, this.purring);
        }

        public Integer countResults(Integer running, Integer jumping, Integer purring) {
            if (running == null && jumping == null && purring == null) {
                return null;
            }
            int r = running != null ? running : 0;
            int j = jumping != null ? jumping : 0;
            int p = purring != null ? purring : 0;
            return r + j + p;
        }

        public Integer getRunning() { return running; }
        public Integer getJumping() { return jumping; }
        public Integer getPurring() { return purring; }
        public Integer getSum() { return sum; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContestResult that = (ContestResult) o;
            return Objects.equals(running, that.running) &&
                   Objects.equals(jumping, that.jumping) &&
                   Objects.equals(purring, that.purring) &&
                   Objects.equals(sum, that.sum);
        }

        @Override
        public int hashCode() {
            return Objects.hash(running, jumping, purring, sum);
        }
    }

    public static class Cat {
        public enum Breed {
            BRITISH, MAINE_COON, MUNCHKIN, PERSIAN, SIBERIAN
        }

        private String name;
        private Integer age;
        private Breed breed;
        private ContestResult contestResult;

        public Cat(String name, Integer age, Breed breed, ContestResult contestResult) {
            this.name = name;
            this.age = age;
            this.breed = breed;
            this.contestResult = contestResult;
        }

        public String getName() { return name; }
        public Integer getAge() { return age; }
        public Breed getBreed() { return breed; }
        public ContestResult getContestResult() { return contestResult; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cat cat = (Cat) o;
            return Objects.equals(name, cat.name) &&
                   Objects.equals(age, cat.age) &&
                   breed == cat.breed &&
                   Objects.equals(contestResult, cat.contestResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, breed, contestResult);
        }
    }

    public static class CatContestAnalyzer {
        public static final Integer DEFAULT_VALUE = -1;

        public Integer getMaxResult(List<Cat> cats) {
            if (cats == null || cats.isEmpty()) {
                return DEFAULT_VALUE;
            }
            return cats.stream()
                    .filter(Objects::nonNull)
                    .map(Cat::getContestResult)
                    .filter(Objects::nonNull)
                    .map(ContestResult::getSum)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(DEFAULT_VALUE);
        }

        public Integer getMinResult(List<Cat> cats) {
            if (cats == null || cats.isEmpty()) {
                return DEFAULT_VALUE;
            }
            return cats.stream()
                    .filter(Objects::nonNull)
                    .map(Cat::getContestResult)
                    .filter(Objects::nonNull)
                    .map(ContestResult::getSum)
                    .filter(Objects::nonNull)
                    .min(Integer::compareTo)
                    .orElse(DEFAULT_VALUE);
        }

        public OptionalDouble getAverageResultByBreed(List<Cat> cats, Cat.Breed breed) {
            if (cats == null || breed == null) {
                return OptionalDouble.empty();
            }
            return cats.stream()
                    .filter(Objects::nonNull)
                    .filter(cat -> breed.equals(cat.getBreed()))
                    .map(Cat::getContestResult)
                    .filter(Objects::nonNull)
                    .map(ContestResult::getSum)
                    .mapToInt(sum -> sum != null ? sum : 0)
                    .average();
        }

        public Optional<Cat> getWinner(List<Cat> cats) {
            if (cats == null || cats.isEmpty()) {
                return Optional.empty();
            }
            return cats.stream()
                    .filter(Objects::nonNull)
                    .filter(cat -> cat.getContestResult() != null && cat.getContestResult().getSum() != null)
                    .max(Comparator.comparing(cat -> cat.getContestResult().getSum()));
        }

        public List<Cat> getThreeLeaders(List<Cat> cats) {
            if (cats == null || cats.isEmpty()) {
                return Collections.emptyList();
            }
            return cats.stream()
                    .filter(Objects::nonNull)
                    .filter(cat -> cat.getContestResult() != null && cat.getContestResult().getSum() != null)
                    .sorted(Comparator.comparing((Cat cat) -> cat.getContestResult().getSum()).reversed())
                    .limit(3)
                    .collect(Collectors.toList());
        }

        public boolean validateResultSumNotNull(List<Cat> cats) {
            if (cats == null) {
                return false;
            }
            return cats.stream()
                    .allMatch(cat -> cat != null
                            && cat.getContestResult() != null
                            && cat.getContestResult().getSum() != null);
        }

        public boolean validateAllResultsSet(List<Cat> cats) {
            if (cats == null) {
                return false;
            }
            return cats.stream()
                    .allMatch(cat -> cat != null
                            && cat.getContestResult() != null
                            && cat.getContestResult().getRunning() != null
                            && cat.getContestResult().getJumping() != null
                            && cat.getContestResult().getPurring() != null);
        }
    }
}
/*
Description of CatContestApp
This Java program processes and analyzes data from a cat beauty and performance contest using the Java Streams API.
Main Components:
Cat (Model): 
Stores the cat's name, age, breed (Breed enum: British, Maine Coon, Munchkin, Persian, Siberian), and contest results.
ContestResult (Result): 
Tracks scores for contest events (running, jumping, purring) and automatically calculates their sum.
CatContestAnalyzer (Analyzer): 
Provides various statistical and query methods:
Min / Max: Finds the lowest or highest score.
Average: Calculates the average score for a specific breed (using OptionalDouble).
Winner / Top Leaders: Selects the winning cat or the top 3 leaders.
Validations: Checks for the presence of scores (null-safety).
The program demonstrates the functionality of the analyzer methods using predefined test data within the main method.
*/

