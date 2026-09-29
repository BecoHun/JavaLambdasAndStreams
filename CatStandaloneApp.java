import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CatStandaloneApp {

    // =========================================================================
    // 1. MODEL CLASSES (Records)
    // =========================================================================

    public enum Breed {
        BRITISH, MAINE_COON, MUNCHKIN, PERSIAN, SIBERIAN, SPHYNX
    }

    public record ContestResult(Integer running, Integer jumping, Integer purring) {
        public Integer sum() {
            int r = running != null ? running : 0;
            int j = jumping != null ? jumping : 0;
            int p = purring != null ? purring : 0;
            return r + j + p;
        }

        @Override
        public String toString() {
            return "running: " + running + "\njumping: " + jumping + "\npurring: " + purring;
        }
    }

    public record Cat(
            String name,
            Integer age,
            Breed breed,
            Integer weight,
            Integer awards,
            ContestResult contestResult
    ) {}

    // Simple data structure for representing table entries
    public record TableEntry<R, C, V>(R row, C column, V value) {}

    // =========================================================================
    // 2. DATA PROCESSOR CLASS (Using Stream API)
    // =========================================================================

    public static class CatDataProcessor {

        // Filters and maps cats to a list of TableEntries using Stream API.
        public List<TableEntry<String, Breed, Integer>> createCatTable(List<Cat> cats) {
            if (cats == null) return Collections.emptyList();

            return cats.stream()
                    .filter(cat -> cat.name() != null && !cat.name().isBlank())
                    .filter(cat -> cat.breed() != null)
                    .filter(cat -> cat.contestResult() != null && cat.contestResult().sum() != null)
                    .map(cat -> new TableEntry<>(cat.name(), cat.breed(), cat.contestResult().sum()))
                    .collect(Collectors.toList());
        }

        // Generates JSON string representation using Stream API.
        public String createCatJson(List<Cat> cats) {
            if (cats == null || cats.isEmpty()) {
                return "[]";
            }

            return cats.stream()
                    .map(this::mapCatToJsonObject)
                    .collect(Collectors.joining(",\n  ", "[\n  ", "\n]"));
        }

        private String mapCatToJsonObject(Cat cat) {
            List<String> fields = new ArrayList<>();

            addField(fields, "name", cat.name() != null ? "\"" + escapeJson(cat.name()) + "\"" : null);
            addField(fields, "age", cat.age());
            addField(fields, "breed", cat.breed() != null ? "\"" + cat.breed().name() + "\"" : null);
            addField(fields, "weight", cat.weight());
            addField(fields, "awards", cat.awards());

            if (cat.contestResult() != null) {
                ContestResult cr = cat.contestResult();
                List<String> contestFields = new ArrayList<>();
                addField(contestFields, "running", cr.running());
                addField(contestFields, "jumping", cr.jumping());
                addField(contestFields, "purring", cr.purring());
                addField(contestFields, "sum", cr.sum());

                fields.add("\"contestResult\": {\n    " + String.join(",\n    ", contestFields) + "\n  }");
            }

            return "{\n  " + String.join(",\n  ", fields) + "\n}";
        }

        private void addField(List<String> list, String key, Object value) {
            if (value != null) {
                list.add("\"" + key + "\": " + value);
            }
        }

        private String escapeJson(String input) {
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    // =========================================================================
    // 3. UTILS CLASS (Generation and file I/O with Streams)
    // =========================================================================

    public static class CatTestUtils {
        public static final String CATS_CSV_PATH = "cats_test.csv";
        public static final String NULLABLE_CATS_CSV_PATH = "nullable_cats_test.csv";
        public static final Integer CAT_COUNT = 1000;
        private static final String HEADERS = "name,age,breed,weight,awards,running,jumping,purring";

        private static final Integer MAX_AGE_WEIGHT = 15;
        private static final Integer MAX_AWARDS = 35;
        private static final Integer MAX_COMPETITION_RESULT = 100;
        private static final Integer THRESHOLD = 8;
        private static final Random random = new Random();

        public static void createCSVFile(String path) {
            List<Cat> cats = CATS_CSV_PATH.equals(path)
                    ? generateCats(CAT_COUNT)
                    : generateCatsWithNullableValues(CAT_COUNT);

            try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
                writer.println(HEADERS);
                cats.forEach(cat -> {
                    ContestResult cr = cat.contestResult();
                    String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                            valToStr(cat.name()),
                            valToStr(cat.age()),
                            cat.breed() != null ? cat.breed().name() : "",
                            valToStr(cat.weight()),
                            valToStr(cat.awards()),
                            cr != null ? valToStr(cr.running()) : "",
                            cr != null ? valToStr(cr.jumping()) : "",
                            cr != null ? valToStr(cr.purring()) : ""
                    );
                    writer.println(line);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static List<Cat> readCSVFile(String path) {
            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                return reader.lines()
                        .skip(1) // Skip CSV header
                        .filter(line -> !line.isBlank())
                        .map(line -> {
                            String[] tokens = line.split(",", -1);
                            return new Cat(
                                    tokens[0].isEmpty() ? null : tokens[0],
                                    parseIntSafe(tokens[1]),
                                    tokens[2].isEmpty() ? null : Breed.valueOf(tokens[2]),
                                    parseIntSafe(tokens[3]),
                                    parseIntSafe(tokens[4]),
                                    new ContestResult(
                                            parseIntSafe(tokens[5]),
                                            parseIntSafe(tokens[6]),
                                            parseIntSafe(tokens[7])
                                    )
                            );
                        })
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static String valToStr(Object val) {
            return val == null ? "" : val.toString();
        }

        private static Integer parseIntSafe(String val) {
            return (val == null || val.isBlank()) ? null : Integer.parseInt(val.trim());
        }

        public static List<Cat> generateCats(int count) {
            return java.util.stream.IntStream.range(0, count)
                    .mapToObj(i -> new Cat(
                            generateRandomString(5),
                            random.nextInt(MAX_AGE_WEIGHT),
                            Breed.values()[random.nextInt(Breed.values().length)],
                            random.nextInt(MAX_AGE_WEIGHT),
                            random.nextInt(MAX_AWARDS),
                            new ContestResult(
                                    random.nextInt(MAX_COMPETITION_RESULT),
                                    random.nextInt(MAX_COMPETITION_RESULT),
                                    random.nextInt(MAX_COMPETITION_RESULT)
                            )
                    ))
                    .collect(Collectors.toList());
        }

        public static List<Cat> generateCatsWithNullableValues(int count) {
            return java.util.stream.IntStream.range(0, count)
                    .mapToObj(i -> new Cat(
                            getNullable(() -> generateRandomString(5)),
                            getNullable(() -> random.nextInt(MAX_AGE_WEIGHT)),
                            getNullable(() -> Breed.values()[random.nextInt(Breed.values().length)]),
                            getNullable(() -> random.nextInt(MAX_AGE_WEIGHT)),
                            getNullable(() -> random.nextInt(MAX_AWARDS)),
                            new ContestResult(
                                    getNullable(() -> random.nextInt(MAX_COMPETITION_RESULT)),
                                    getNullable(() -> random.nextInt(MAX_COMPETITION_RESULT)),
                                    getNullable(() -> random.nextInt(MAX_COMPETITION_RESULT))
                            )
                    ))
                    .collect(Collectors.toList());
        }

        private static <T> T getNullable(java.util.function.Supplier<T> supplier) {
            return random.nextInt(10) < THRESHOLD ? supplier.get() : null;
        }

        private static String generateRandomString(int length) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            return random.ints(length, 0, chars.length())
                    .mapToObj(chars::charAt)
                    .map(Object::toString)
                    .collect(Collectors.joining());
        }

        public static void cleanupFiles() {
            try {
                Files.deleteIfExists(Paths.get(CATS_CSV_PATH));
                Files.deleteIfExists(Paths.get(NULLABLE_CATS_CSV_PATH));
            } catch (IOException ignored) {}
        }
    }

    // =========================================================================
    // 4. MAIN & CUSTOM TEST RUNNER
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== Starting Standalone Cat Processor ===\n");

        // Prepare test CSV files
        CatTestUtils.createCSVFile(CatTestUtils.CATS_CSV_PATH);
        CatTestUtils.createCSVFile(CatTestUtils.NULLABLE_CATS_CSV_PATH);

        CatDataProcessor processor = new CatDataProcessor();
        List<Cat> cats = CatTestUtils.readCSVFile(CatTestUtils.CATS_CSV_PATH);
        List<Cat> nullableCats = CatTestUtils.readCSVFile(CatTestUtils.NULLABLE_CATS_CSV_PATH);

        int passed = 0;
        int failed = 0;

        // TEST 1: Table generation with full dataset
        try {
            List<TableEntry<String, Breed, Integer>> table = processor.createCatTable(cats);
            assertEquals(1000, table.size(), "Table size must be 1000");
            System.out.println("✓ TEST 1 PASSED: Table generation with full dataset");
            passed++;
        } catch (Throwable e) {
            System.err.println("✗ TEST 1 FAILED: " + e.getMessage());
            failed++;
        }

        // TEST 2: Table generation with nullable dataset (Verify stream filtering)
        try {
            List<TableEntry<String, Breed, Integer>> table = processor.createCatTable(nullableCats);
            boolean allValid = table.stream().allMatch(e -> e.row() != null && e.column() != null && e.value() != null);
            assertTrue(allValid, "Filtered elements must not contain nulls");
            System.out.println("✓ TEST 2 PASSED: Nullable table generation & filter verification (Valid elements: " + table.size() + ")");
            passed++;
        } catch (Throwable e) {
            System.err.println("✗ TEST 2 FAILED: " + e.getMessage());
            failed++;
        }

        // TEST 3: Handling empty input
        try {
            List<TableEntry<String, Breed, Integer>> table = processor.createCatTable(Collections.emptyList());
            assertTrue(table.isEmpty(), "Table should be empty");
            String json = processor.createCatJson(Collections.emptyList());
            assertEquals("[]", json, "JSON should be an empty array");
            System.out.println("✓ TEST 3 PASSED: Empty input handled correctly");
            passed++;
        } catch (Throwable e) {
            System.err.println("✗ TEST 3 FAILED: " + e.getMessage());
            failed++;
        }

        // TEST 4: JSON generation via Stream API
        try {
            List<Cat> subList = cats.subList(0, 2);
            String json = processor.createCatJson(subList);
            assertTrue(json.contains("\"name\":"), "JSON should contain name property");
            assertTrue(json.contains("\"contestResult\":"), "JSON should contain contestResult property");
            System.out.println("✓ TEST 4 PASSED: JSON generation via Streams");
            System.out.println("\nSample JSON Output:\n" + json);
            passed++;
        } catch (Throwable e) {
            System.err.println("✗ TEST 4 FAILED: " + e.getMessage());
            failed++;
        }

        // Cleanup temporary files
        CatTestUtils.cleanupFiles();

        System.out.println("\n=========================================");
        System.out.printf("TEST RESULTS: %d Passed | %d Failed\n", passed, failed);
        System.out.println("=========================================");
    }

    // Helper methods for assertions
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
/*
CatStandaloneApp is a standalone Java application (free of external dependencies) that processes, transforms, and tests cat data and competition results using the Java Stream API.
Key Features and Architecture
Data Model (Records):
Uses modern, immutable data structures (Cat, ContestResult, TableEntry) and a breed enum (Breed) to store data.
Data Processing (CatDataProcessor):
createCatTable: 
Filters out incomplete/null values, then maps the cats' names, breeds, and total competition scores into a tabular format (TableEntry).
createCatJson: 
A custom JSON serializer built without external libraries that converts the list of cats into a JSON array.
Test Data Handling (CatTestUtils):
Automatically generates a 1,000-item test dataset (with both complete and incomplete data fields), handles CSV reading and writing, and performs cleanup after testing.
Built-in Test Runner (main method):
Executes 4 unit tests using custom assertion methods (assertEquals, assertTrue), verifying filtering accuracy, empty input handling, and the correctness of JSON generation.
*/

