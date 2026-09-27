import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CatGeneratorMain {

    public static void main(String[] args) {
        System.out.println("--- Starting tests ---");

        testShouldGenerateNonNullableCats();
        testShouldReturnEmptyListWhenZeroCount();
        testShouldReturnFoodAmount();
        testShouldThrowArithmeticException();
        testShouldThrowIllegalArgumentException();

        System.out.println("\n All tests passed successfully!");
    }

    // ==========================================
    // TEST METHODS
    // ==========================================

    private static void testShouldGenerateNonNullableCats() {
        int catCount = 10;
        List<Cat> cats = CatGenerator.generateCats(catCount);

        assertTrue(cats != null, "The cats list must not be null");
        assertEquals(catCount, cats.size(), "The list size is incorrect");

        for (Cat cat : cats) {
            assertTrue(cat.getName() != null, "Cat name must not be null");
            assertTrue(cat.getAge() != null, "Cat age must not be null");
            assertTrue(cat.getBreed() != null, "Cat breed must not be null");
        }
        System.out.println("✓ testShouldGenerateNonNullableCats passed");
    }

    private static void testShouldReturnEmptyListWhenZeroCount() {
        List<Cat> cats = CatGenerator.generateCats(0);

        assertTrue(cats != null, "The cats list must not be null");
        assertTrue(cats.isEmpty(), "The list should be empty when count is 0");
        System.out.println("✓ testShouldReturnEmptyListWhenZeroCount passed");
    }

    private static void testShouldReturnFoodAmount() {
        // [familySize, skip, expected]
        long[][] testData = {
                {0, 0, 0},
                {4, 0, 60},
                {8, 0, 1020},
                {20, 0, 4194300},
                {39, 0, 2199023255548L},
                {43, 13, 35184372056064L},
                {6, 5, 128},
                {9, 10, 0}
        };

        for (long[] data : testData) {
            int familySize = (int) data[0];
            int skip = (int) data[1];
            long expected = data[2];

            long foodAmount = CatGenerator.generateFood(familySize, skip);
            assertEquals(expected, foodAmount, "Incorrect food amount for parameters: familySize=" + familySize + ", skip=" + skip);
        }
        System.out.println("✓ testShouldReturnFoodAmount passed");
    }

    private static void testShouldThrowArithmeticException() {
        int[][] testData = {
                {19563, 0},
                {89, 40},
                {19563, 19562}
        };

        for (int[] data : testData) {
            int familySize = data[0];
            int skip = data[1];

            boolean exceptionThrown = false;
            try {
                CatGenerator.generateFood(familySize, skip);
            } catch (ArithmeticException e) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown, "Expected ArithmeticException was not thrown for parameters: familySize=" + familySize + ", skip=" + skip);
        }
        System.out.println("✓ testShouldThrowArithmeticException passed");
    }

    private static void testShouldThrowIllegalArgumentException() {
        int[][] testData = {
                {-100, 0},
                {0, -100},
                {-100, -100}
        };

        for (int[] data : testData) {
            int familySize = data[0];
            int skip = data[1];

            try {
                CatGenerator.generateFood(familySize, skip);
                throw new AssertionError("Expected IllegalArgumentException was not thrown.");
            } catch (IllegalArgumentException e) {
                assertEquals("Input arguments cannot be negative", e.getMessage(), "Different exception message");
            }
        }
        System.out.println("✓ testShouldThrowIllegalArgumentException passed");
    }

    // ==========================================
    // HELPER ASSERTION METHODS
    // ==========================================

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Test failure: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Test failure: " + message + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    // ==========================================
    // UTILITY CLASSES
    // ==========================================

    public static class CatGenerator {

        public static List<Cat> generateCats(int count) {
            List<Cat> cats = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cats.add(
                        Cat.builder()
                                .name("Cat" + i)
                                .age(i)
                                .breed(Cat.Breed.values()[i % Cat.Breed.values().length])
                                .build()
                );
            }
            return cats;
        }

        public static long generateFood(int familySize, int skip) {
            if (familySize < 0 || skip < 0) {
                throw new IllegalArgumentException(
                        "Input arguments cannot be negative"
                );
            }

            if (familySize + 2 >= 63) {
                throw new ArithmeticException("long overflow");
            }

            if (skip >= familySize) {
                return 0;
            }

            long totalFood = 1L << (familySize + 2);
            long skippedFood = 1L << (skip + 2);

            return Math.subtractExact(totalFood, skippedFood);
        }
    }

    public static class Cat {

        private String name;
        private Integer age;
        private Breed breed;

        public Cat() {
        }

        public Cat(String name, Integer age, Breed breed) {
            this.name = name;
            this.age = age;
            this.breed = breed;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Breed getBreed() {
            return breed;
        }

        public void setBreed(Breed breed) {
            this.breed = breed;
        }

        public static CatBuilder builder() {
            return new CatBuilder();
        }

        public enum Breed {
            BRITISH, MAINE_COON, MUNCHKIN, PERSIAN, SIBERIAN
        }

        @Override
        public String toString() {
            return "Cat{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", breed=" + breed +
                    '}';
        }

        public static class CatBuilder {
            private String name;
            private Integer age;
            private Breed breed;

            public CatBuilder name(String name) {
                this.name = name;
                return this;
            }

            public CatBuilder age(Integer age) {
                this.age = age;
                return this;
            }

            public CatBuilder breed(Breed breed) {
                this.breed = breed;
                return this;
            }

            public Cat build() {
                return new Cat(name, age, breed);
            }
        }
    }
}
/*
The provided Java code is a standalone, dependency-free testable application that implements cat object generation and food amount calculation.
Main Components and Functionality
Cat Data Model:
Represents a cat with name, age, and breed (Breed enum) fields.
Uses a custom Builder pattern (CatBuilder) for convenient and clear object creation.
CatGenerator Business Logic:
generateCats(int count): 
Generates a list containing the specified number of fully populated Cat objects.
generateFood(int familySize, int skip): 
Calculates the required food amount using a bit-shift formula (1L << n). 
It throws an IllegalArgumentException for invalid arguments and an ArithmeticException in case of an overflow.
Built-in Framework-Free Test Runner:
The main method runs the test suite instead of relying on JUnit.
Uses custom assertion helper methods (assertTrue, assertEquals).
Tested Scenarios: 
Successful list and value generation, empty list handling, accurate calculations, and proper exception throwing upon invalid inputs or integer overflow.
*/

