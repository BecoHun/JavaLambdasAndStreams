import java.util.Comparator;
import java.util.Objects;

public class RadianComparatorRunner {
    private static final double TWO_PI = 2 * Math.PI;
    private static final double EPSILON = 0.001;

    public static final class RadianComparator {
        public static Comparator<Double> get() {
            return (angle1, angle2) -> {
                if (angle1 == null && angle2 == null) {
                    return 0;
                }
                if (angle1 == null) {
                    return -1;
                }
                if (angle2 == null) {
                    return 1;
                }

                double a = normalize(angle1);
                double b = normalize(angle2);

                double difference = Math.abs(a - b);
                if (difference < EPSILON) {
                    return 0;
                }

                double circularDifference = TWO_PI - difference;
                if (circularDifference < EPSILON) {
                    return 0;
                }

                return Double.compare(a, b);
            };
        }

        private static double normalize(double angle) {
            double result = angle % TWO_PI;
            if (result < 0) {
                result += TWO_PI;
            }
            return result;
        }
    }

    public static void main(String[] args) {
        Comparator<Double> cmp = RadianComparator.get();
        int passed = 0;
        int failed = 0;

        // Reconstructing test data
        TestCase[] testCases = new TestCase[]{
            new TestCase(0.0, 0.0, 0),
            new TestCase(0.0, 2 * Math.PI, 0),
            new TestCase(0.0, Math.PI, -1),
            new TestCase(Math.PI, 0.0, 1),
            new TestCase(2.3, Math.fma(16, Math.PI, 2.3), 0),
            new TestCase(null, 0.0, -1),
            new TestCase(0.0, null, 1),
            new TestCase(null, null, 0),
            new TestCase(0.0, 0.0005, 0),
            new TestCase(0.001, 0.0, 1),
            new TestCase(0.0, 0.001, -1)
        };

        System.out.println("--- RUNNING RADIAN_COMPARATOR TESTS ---\n");

        for (int i = 0; i < testCases.length; i++) {
            TestCase tc = testCases[i];
            int rawResult = cmp.compare(tc.angle1, tc.angle2);
            int actualSign = (int) Math.signum(rawResult);
            int expectedSign = (int) Math.signum(tc.expectedResultSign);

            boolean isSuccess = actualSign == expectedSign;

            if (isSuccess) {
                passed++;
                System.out.printf("[SUCCESS] Test #%2d: cmp(%s, %s) -> expected sign: %2d, actual sign: %2d%n",
                        i + 1, format(tc.angle1), format(tc.angle2), expectedSign, actualSign);
            } else {
                failed++;
                System.err.printf("[FAILURE] Test #%2d: cmp(%s, %s) -> expected sign: %2d, but got actual sign: %2d%n",
                        i + 1, format(tc.angle1), format(tc.angle2), expectedSign, actualSign);
            }
        }

        System.out.println("\n-------------------------------------------");
        System.out.printf("RESULTS: %d passed, %d failed tests.%n", passed, failed);

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static String format(Double val) {
        return Objects.toString(val, "null");
    }

    private static class TestCase {
        final Double angle1;
        final Double angle2;
        final int expectedResultSign;

        TestCase(Double angle1, Double angle2, int expectedResultSign) {
            this.angle1 = angle1;
            this.angle2 = angle2;
            this.expectedResultSign = expectedResultSign;
        }
    }
}
/*
This code is a standalone Java application designed to compare angles provided in radians, and it includes a built-in test runner.
Angle normalization: 
Converts input values to the [0, 2PI) range, ignoring full periods.
Tolerance and periodicity: 
Operates with a precision of 0.001 (EPSILON). 
If the difference between two angles is smaller than this threshold—or if they are brought within this threshold due to wrapping around 2PI (e.g., 0 == 2PI) — 
they are treated as equal.
Null safety: 
Safely handles null values (null is considered smaller than non-null values).
Built-in testing: 
The main method verifies the comparator's behavior against predefined test cases without external libraries (like JUnit) and prints the results to the console.
*/

