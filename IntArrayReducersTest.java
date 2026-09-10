import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IntArrayReducersTest {

  @FunctionalInterface
  public interface IntArrayReducer {
    int reduce(int[] array);
  }

  public interface IntArrayReducers {

    IntArrayReducer SUMMARIZER = array -> {
      int sum = 0;
      for (int value : array) {
        sum += value;
      }
      return sum;
    };

    IntArrayReducer MULTIPLIER = array -> {
      int result = 1;
      for (int value : array) {
        result *= value;
      }
      return result;
    };

    IntArrayReducer MIN_FINDER = array -> {
      int min = array[0];
      for (int i = 1; i < array.length; i++) {
        if (array[i] < min) {
          min = array[i];
        }
      }
      return min;
    };

    IntArrayReducer MAX_FINDER = array -> {
      int max = array[0];
      for (int i = 1; i < array.length; i++) {
        if (array[i] > max) {
          max = array[i];
        }
      }
      return max;
    };

    IntArrayReducer AVERAGE_CALCULATOR = array -> {
      int sum = 0;
      for (int value : array) {
        sum += value;
      }
      return Math.round((float) sum / array.length);
    };

    IntArrayReducer UNIQUE_COUNTER = array -> {
      Set<Integer> unique = new HashSet<>();
      for (int value : array) {
        unique.add(value);
      }
      return unique.size();
    };

    IntArrayReducer SORT_DIRECTION_DEFINER = array -> {
      int direction = 0;
      for (int i = 1; i < array.length; i++) {
        if (array[i] > array[i - 1]) {
          direction++;
        } else if (array[i] < array[i - 1]) {
          direction--;
        }
      }
      if (direction > 0) {
        return 1;
      }
      if (direction < 0) {
        return -1;
      }
      return 0;
    };
  }

  public static void main(String[] args) {
    System.out.println("Starting tests...");

    // SUMMARIZER
    test(IntArrayReducers.SUMMARIZER, ints(0), 0);
    test(IntArrayReducers.SUMMARIZER, ints(-1, 1), 0);
    test(IntArrayReducers.SUMMARIZER, ints(1, 2, 3, 4, 5), 15);

    // MULTIPLIER
    test(IntArrayReducers.MULTIPLIER, ints(0), 0);
    test(IntArrayReducers.MULTIPLIER, ints(1, 2, 1234412, 0), 0);
    test(IntArrayReducers.MULTIPLIER, ints(-1, -1), 1);
    test(IntArrayReducers.MULTIPLIER, ints(-1, 1), -1);
    test(IntArrayReducers.MULTIPLIER, ints(1, 2, 3, 4, 5), 120);

    // MIN_FINDER
    test(IntArrayReducers.MIN_FINDER, ints(0), 0);
    test(IntArrayReducers.MIN_FINDER, ints(1, 1, 1), 1);
    test(IntArrayReducers.MIN_FINDER, ints(-1, 0, 1), -1);

    // MAX_FINDER
    test(IntArrayReducers.MAX_FINDER, ints(0), 0);
    test(IntArrayReducers.MAX_FINDER, ints(1, 1, 1), 1);
    test(IntArrayReducers.MAX_FINDER, ints(-1, 0, 1), 1);

    // AVERAGE_CALCULATOR
    test(IntArrayReducers.AVERAGE_CALCULATOR, ints(0), 0);
    test(IntArrayReducers.AVERAGE_CALCULATOR, ints(1, 1, 1, 1, 1), 1);
    test(IntArrayReducers.AVERAGE_CALCULATOR, ints(1, 2, 3, 4, 5), 3);
    test(IntArrayReducers.AVERAGE_CALCULATOR, ints(1, 2), 2);
    test(IntArrayReducers.AVERAGE_CALCULATOR, ints(1, 2, 1), 1);

    // UNIQUE_COUNTER
    test(IntArrayReducers.UNIQUE_COUNTER, ints(0), 1);
    test(IntArrayReducers.UNIQUE_COUNTER, ints(0, 0, 0), 1);
    test(IntArrayReducers.UNIQUE_COUNTER, ints(1, 2, 3), 3);
    test(IntArrayReducers.UNIQUE_COUNTER, ints(1, 2, 2, 3, 3, 3), 3);

    // SORT_DIRECTION_DEFINER
    test(IntArrayReducers.SORT_DIRECTION_DEFINER, ints(0), 0);
    test(IntArrayReducers.SORT_DIRECTION_DEFINER, ints(0, 0), 0);
    test(IntArrayReducers.SORT_DIRECTION_DEFINER, ints(0, 1), 1);
    test(IntArrayReducers.SORT_DIRECTION_DEFINER, ints(1, 0), -1);
    test(IntArrayReducers.SORT_DIRECTION_DEFINER, ints(1, 0, 1), 0);
    test(IntArrayReducers.SORT_DIRECTION_DEFINER, ints(0, 0, 1), 1);
    test(IntArrayReducers.SORT_DIRECTION_DEFINER, ints(0, 0, -1), -1);

    System.out.println("All tests passed successfully!");
  }

  private static void test(IntArrayReducer reducer, int[] array, int expected) {
    int actual = reducer.reduce(array);
    if (actual != expected) {
      throw new RuntimeException(
          String.format("Test failed! Array: %s | Expected: %d | Actual: %d",
              Arrays.toString(array), expected, actual)
      );
    }
  }

  private static int[] ints(int... array) {
    return array;
  }
}
/*
This program is a Java-based array processing and testing application that performs various aggregation/reduction operations on integer arrays (int[]).
Key Components:
IntArrayReducer: 
A functional interface that defines the array reduction operation (input: int[], output: int).
IntArrayReducers: 
A collection of reduction algorithms implemented as lambda expressions:
SUMMARIZER: 
Calculates the sum of the elements.
MULTIPLIER: 
Calculates the product of the elements.
MIN_FINDER, MAX_FINDER: 
Finds the minimum, maximum element.
AVERAGE_CALCULATOR: 
Calculates the rounded average.
UNIQUE_COUNTER: 
Counts the number of unique elements.
SORT_DIRECTION_DEFINER: 
Determines the sorting order of the array (ascending: 1, descending: -1, unsorted, equal: 0).
The main method: 
Tests all algorithms against predefined test cases using custom assertion logic, without relying on external frameworks like JUnit.
*/
