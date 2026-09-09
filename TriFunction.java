@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);

    public static void main(String[] args) {
        // Example 1: Concatenating strings (String, String, String -> String)
        TriFunction<String, String, String, String> formatter = 
            (firstName, middleName, lastName) -> firstName + " " + middleName + " " + lastName;

        String fullName = formatter.apply("John", "Fitzgerald", "Kennedy");
        System.out.println("Full name: " + fullName);

        // Example 2: Mathematical operation (Integer, Double, Integer -> Double)
        TriFunction<Integer, Double, Integer, Double> volumeCalculator = 
            (length, width, height) -> length * width * height;

        Double volume = volumeCalculator.apply(5, 2.5, 4);
        System.out.println("Volume: " + volume);

        // Example 3: Business logic (String, Integer, Boolean -> String)
        TriFunction<String, Integer, Boolean, String> userGreeting = 
            (name, age, isVip) -> "Welcome " + name + " (" + age + " years old)" + (isVip ? " [VIP Member]" : "");

        System.out.println(userGreeting.apply("Peter Smith", 30, true));

        // Example 4: Using method reference
        TriFunction<String, Integer, Integer, String> substringExtractor = String::substring;
        String result = substringExtractor.apply("Activation", 0, 4);
        System.out.println("Substring: " + result);
    }
}
/*
This code defines and demonstrates a three-parameter generic functional interface (TriFunction).
Interface Structure: 
Annotated with @FunctionalInterface, it accepts 4 type parameters (T, U, V for inputs, R for the return value). 
Its single abstract method is apply(T t, U u, V v).
Execution in main: 
It assigns various lambda expressions and a method reference to the interface:
String Formatting: 
Concatenates three String values into a full name.
Volume Calculation: 
Computes a Double result using Integer and Double inputs.
User Greeting: 
Formats a custom message from String, Integer, and Boolean parameters.
Method Reference: 
Binds Java's built-in String::substring method directly to TriFunction.
*/

