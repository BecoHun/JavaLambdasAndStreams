import java.util.function.DoubleFunction;

public class PercentageFormatterApp {

    // The PercentageFormatter interface and its lambda implementation
    public interface PercentageFormatter {
        DoubleFunction<String> INSTANCE = percent -> {
            double value = Math.round(percent * 1000.0) / 10.0;
            return (value == (long) value 
                    ? String.valueOf((long) value) 
                    : String.valueOf(value)) + " %";
        };
    }

    public static void main(String[] args) {
        System.out.println("Starting tests...");

        // Test 1: Verify lambda implementation (isSynthetic)
        Class<? extends DoubleFunction> iClass = PercentageFormatter.INSTANCE.getClass();
        if (!iClass.isSynthetic()) {
            throw new AssertionError("PercentageFormatter must be implemented as a lambda!");
        }

        // Test 2: Verify formatting accuracy
        assertResult(0.5, "50 %");
        assertResult(-0.43, "-43 %");
        assertResult(0.6875, "68.8 %");
        assertResult(1.2, "120 %");
        assertResult(0.54333, "54.3 %");

        System.out.println("All tests passed successfully!");
    }

    private static void assertResult(double percent, String expected) {
        String actual = PercentageFormatter.INSTANCE.apply(percent);
        if (!expected.equals(actual)) {
            throw new AssertionError(String.format("Failed for %f: expected \"%s\", but got \"%s\"", 
                    percent, expected, actual));
        }
        System.out.println(String.format("Passed: %f -> \"%s\"", percent, actual));
    }
}
/*
PercentageFormatterApp is a standalone, zero-dependency Java application that tests the lambda expression defined in the PercentageFormatter interface.
Key Features
Percentage Formatting (PercentageFormatter.INSTANCE):
Multiplies the given fraction by 100 and rounds it to at most one decimal place.
If the result is a whole number, it omits the decimal point (e.g., 0.5 -> "50 %").
If there is a decimal fraction, it retains it (e.g., 0.6875 -> "68.8 %").
Automated Verification (main method):
Lambda Check: Uses the isSynthetic() method to verify that INSTANCE is indeed implemented as a lambda expression.
Value Tests: Verifies the accuracy of the formatting logic across five different test cases (0.5, -0.43, 0.6875, 1.2, 0.54333) and throws an AssertionError if a test fails.
*/

