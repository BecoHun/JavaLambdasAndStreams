import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ThrowingFunctionTest {

    // ==========================================
    // 1. Interface definition
    // ==========================================
    @FunctionalInterface
    public interface ThrowingFunction<A, R, E extends Throwable> {

        R apply(A argument) throws E;

        static <A, R, E extends Throwable> Function<A, R> quiet(
                ThrowingFunction<A, R, E> function) {

            if (function == null) {
                return null;
            }

            return argument -> {
                try {
                    return function.apply(argument);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    // ==========================================
    // 2. Constants (Direct class reference)
    // ==========================================
    private static final String FULL_CLASS_NAME = "ThrowingFunctionTest$ThrowingFunction";
    private static final String FI_CLASS_NAME = "ThrowingFunction";
    private static final String FI_APPLY_METHOD_NAME = FI_CLASS_NAME + ".apply";
    private static final String FI_APPLY_RETURN_NAME = FI_CLASS_NAME + ".apply (return type)";
    private static final String FI_APPLY_PARAMETER_NAME = FI_CLASS_NAME + ".apply (parameter)";
    private static final String FI_APPLY_EXCEPTION_NAME = FI_CLASS_NAME + ".apply (exception)";
    private static final String FI_QUIET_METHOD_NAME = FI_CLASS_NAME + ".quiet";
    private static final String FI_QUIET_RETURN_NAME = FI_CLASS_NAME + ".quiet (return type)";
    private static final String FI_QUIET_PARAMETER_NAME = FI_CLASS_NAME + ".quiet (parameter)";

    public static void main(String[] args) {
        System.out.println("Running tests...\n");

        runTest("shouldExist", ThrowingFunctionTest::shouldExist);
        runTest("shouldBeAnInterface", ThrowingFunctionTest::shouldBeAnInterface);
        runTest("shouldBeGeneralized", ThrowingFunctionTest::shouldBeGeneralized);
        runTest("shouldBeAnnotatedWithFunctionalInterface", ThrowingFunctionTest::shouldBeAnnotatedWithFunctionalInterface);
        runTest("shouldHaveMethodApply", ThrowingFunctionTest::shouldHaveMethodApply);
        runTest("shouldApplyBeAbstract", ThrowingFunctionTest::shouldApplyBeAbstract);
        runTest("shouldThrowAnException", ThrowingFunctionTest::shouldThrowAnException);
        runTest("shouldBeProperlyGeneralizedParametersMethod", ThrowingFunctionTest::shouldBeProperlyGeneralizedParametersMethod);
        runTest("shouldBeProperlyGeneralizedReturnType", ThrowingFunctionTest::shouldBeProperlyGeneralizedReturnType);
        runTest("shouldHaveDifferentGenericTypes", ThrowingFunctionTest::shouldHaveDifferentGenericTypes);
        runTest("shouldApplyMethodNotBeSelfGeneralized", ThrowingFunctionTest::shouldApplyMethodNotBeSelfGeneralized);
        runTest("shouldHaveQuietMethod", ThrowingFunctionTest::shouldHaveQuietMethod);
        runTest("shouldQuietMethodBeStatic", ThrowingFunctionTest::shouldQuietMethodBeStatic);
        runTest("shouldQuietMethodHaveThrowingFunctionAsParameter", ThrowingFunctionTest::shouldQuietMethodHaveThrowingFunctionAsParameter);
        runTest("shouldQuietMethodHaveFunctionAsReturnType", ThrowingFunctionTest::shouldQuietMethodHaveFunctionAsReturnType);
        runTest("shouldQuietMethodNotThrowAnything", ThrowingFunctionTest::shouldQuietMethodNotThrowAnything);
        runTest("shouldQuietMethodHaveItsOwnTypeParameters", ThrowingFunctionTest::shouldQuietMethodHaveItsOwnTypeParameters);
        runTest("shouldQuietMethodBeProperlyGeneralized", ThrowingFunctionTest::shouldQuietMethodBeProperlyGeneralized);
        runTest("shouldReturnNullWhenNullIsPassed", ThrowingFunctionTest::shouldReturnNullWhenNullIsPassed);
        runTest("shouldWorkCorrectlyWhenNothingWasThrown", ThrowingFunctionTest::shouldWorkCorrectlyWhenNothingWasThrown);
        runTest("shouldThrowUncheckedExceptionWhenErrorOccurred", ThrowingFunctionTest::shouldThrowUncheckedExceptionWhenErrorOccurred);

        System.out.println("\nAll tests passed successfully!");
    }

    private static void runTest(String testName, Runnable test) {
        try {
            test.run();
            System.out.println("[PASS] " + testName);
        } catch (AssertionError e) {
            System.err.println("[FAIL] " + testName + " -> " + e.getMessage());
            System.exit(1);
        } catch (Throwable t) {
            System.err.println("[ERROR] " + testName + " -> An unexpected error occurred:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    //region ThrowingFunction type tests
    static void shouldExist() {
        findClass();
    }

    static void shouldBeAnInterface() {
        Class<?> fiClass = findClass();
        assertTrue(fiClass.isInterface(), "ThrowingFunction must be an interface");
    }

    static void shouldBeGeneralized() {
        Class<?> fiClass = findClass();
        assertThatTypeParametersValidity(FI_CLASS_NAME, fiClass.getTypeParameters());
    }

    static void shouldBeAnnotatedWithFunctionalInterface() {
        Class<?> fiClass = findClass();
        FunctionalInterface annotation = fiClass.getDeclaredAnnotation(FunctionalInterface.class);
        if (annotation == null) {
            fail(FI_CLASS_NAME + " must be annotated with @FunctionalInterface");
        }
    }
    //endregion

    //region ThrowingFunction.apply tests
    static void shouldHaveMethodApply() {
        findApplyMethod();
    }

    static void shouldApplyBeAbstract() {
        Method method = findApplyMethod();
        assertTrue(Modifier.isAbstract(method.getModifiers()), FI_APPLY_METHOD_NAME + " must be abstract");
        assertTrue(Modifier.isPublic(method.getModifiers()), FI_APPLY_METHOD_NAME + " must be public");
    }

    static void shouldThrowAnException() {
        Method method = findApplyMethod();
        Type[] exceptions = method.getGenericExceptionTypes();
        assertEquals(1, exceptions.length, FI_APPLY_METHOD_NAME + " must have declared throwing exceptions");
        Type exception = exceptions[0];
        assertEquals(Throwable.class.getCanonicalName(),
                method.getExceptionTypes()[0].getCanonicalName(),
                FI_APPLY_EXCEPTION_NAME + " must have Throwable as upper boundary");
        assertNotEquals(Throwable.class.getCanonicalName(), exception.getTypeName(),
                FI_APPLY_EXCEPTION_NAME + " must be generalized");
    }

    static void shouldBeProperlyGeneralizedParametersMethod() {
        Method method = findApplyMethod();
        assertEquals(1, method.getParameterCount(), FI_APPLY_METHOD_NAME + " must have exactly one argument");
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        assertEquals(Object.class, parameterTypes[0], FI_APPLY_PARAMETER_NAME + " must not have upper boundary");
        assertNotEquals(parameterTypes[0].getCanonicalName(), genericParameterTypes[0].getTypeName(),
                FI_APPLY_PARAMETER_NAME + " must be generalized");
    }

    static void shouldBeProperlyGeneralizedReturnType() {
        Method method = findApplyMethod();
        Class<?> returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        assertEquals(Object.class, returnType, FI_APPLY_RETURN_NAME + " must not have upper boundary");
        assertNotEquals(returnType.getCanonicalName(), genericReturnType.getTypeName(),
                FI_APPLY_RETURN_NAME + " must be generalized");
    }

    static void shouldHaveDifferentGenericTypes() {
        Method method = findApplyMethod();
        ArrayList<Type> types = new ArrayList<>();
        types.add(method.getGenericReturnType());
        types.addAll(List.of(method.getGenericParameterTypes()));
        types.addAll(List.of(method.getGenericExceptionTypes()));
        assertThatAllGenericTypesAreDifferent(FI_CLASS_NAME, types);
    }

    static void shouldApplyMethodNotBeSelfGeneralized() {
        Method method = findApplyMethod();
        if (method.getTypeParameters().length > 0) {
            fail(FI_APPLY_METHOD_NAME + " must not have any method type variable");
        }
    }
    //endregion

    //region ThrowingFunction.quiet tests
    static void shouldHaveQuietMethod() {
        findQuietMethod();
    }

    static void shouldQuietMethodBeStatic() {
        Method method = findQuietMethod();
        assertTrue(Modifier.isStatic(method.getModifiers()), FI_QUIET_METHOD_NAME + " must be static");
        assertTrue(Modifier.isPublic(method.getModifiers()), FI_QUIET_METHOD_NAME + " must be public");
    }

    static void shouldQuietMethodHaveThrowingFunctionAsParameter() {
        Class<?> fiClass = findClass();
        Method method = findQuietMethod();
        assertEquals(1, method.getParameterCount(), FI_QUIET_METHOD_NAME + " must have exactly one argument");
        ParameterizedType type = toParameterizedType(method.getGenericParameterTypes()[0]);
        assertEquals(fiClass, type.getRawType(), FI_QUIET_PARAMETER_NAME + " must be a " + FI_CLASS_NAME);
    }

    static void shouldQuietMethodHaveFunctionAsReturnType() {
        Method method = findQuietMethod();
        ParameterizedType type = toParameterizedType(method.getGenericReturnType());
        assertEquals(Function.class, type.getRawType(), FI_QUIET_RETURN_NAME + " must be a Function");
    }

    static void shouldQuietMethodNotThrowAnything() {
        Method method = findQuietMethod();
        Type[] exceptionTypes = method.getGenericExceptionTypes();
        if (exceptionTypes.length > 0) {
            fail(FI_QUIET_METHOD_NAME + " must not throw anything");
        }
    }

    static void shouldQuietMethodHaveItsOwnTypeParameters() {
        Method method = findQuietMethod();
        assertThatTypeParametersValidity(FI_QUIET_METHOD_NAME, method.getTypeParameters());
    }

    static void shouldQuietMethodBeProperlyGeneralized() {
        Class<?> fiClass = findClass();
        Method method = findQuietMethod();
        assertEquals(1, method.getParameterCount(), FI_QUIET_METHOD_NAME + " must have exactly one argument");
        TypeVariable<Method>[] typeParameters = method.getTypeParameters();
        assertThatTypeParametersValidity(FI_QUIET_METHOD_NAME, typeParameters);
        assertThatAllGenericTypesAreDifferent(FI_QUIET_METHOD_NAME, List.of(typeParameters));
        ParameterizedType returnType = toParameterizedType(method.getGenericReturnType());
        assertEquals(Function.class, returnType.getRawType(), FI_QUIET_RETURN_NAME + " must be a Function");
        Type[] returnTypeArguments = returnType.getActualTypeArguments();
        assertThatAllGenericTypesAreDifferent(FI_QUIET_RETURN_NAME, List.of(returnTypeArguments));
        ParameterizedType parameterType = toParameterizedType(method.getGenericParameterTypes()[0]);
        assertEquals(fiClass, parameterType.getRawType(), FI_QUIET_PARAMETER_NAME + " must be a " + FI_CLASS_NAME);
        Type[] parameterTypeArguments = parameterType.getActualTypeArguments();
        assertThatAllGenericTypesAreDifferent(FI_QUIET_PARAMETER_NAME, List.of(parameterTypeArguments));
        assertEquals(returnTypeArguments[0], parameterTypeArguments[0],
                FI_QUIET_METHOD_NAME + " input type of the parameter and return type are not the same");
        assertEquals(returnTypeArguments[1], parameterTypeArguments[1],
                FI_QUIET_METHOD_NAME + " return type of the parameter and return type are not the same");
    }
    //endregion

    //region ThrowingFunction.quiet functional tests
    static void shouldReturnNullWhenNullIsPassed() {
        Object quietFunction = invokeQuiet(null);
        assertNull(quietFunction, FI_QUIET_METHOD_NAME + " must return null when null is passed");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void shouldWorkCorrectlyWhenNothingWasThrown() {
        var throwingFunction = newProxy(null);
        Object quietFunction = invokeQuiet(throwingFunction);
        if (!(quietFunction instanceof Function)) {
            fail(FI_QUIET_METHOD_NAME + " must return a Function");
        }

        Object result = ((Function) quietFunction).apply("argument");
        if (!(result instanceof String)) {
            fail(FI_QUIET_METHOD_NAME + " must return Function with the same return type as of the argument");
        }
        assertEquals("argument", result, "Return value does not match expected");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void shouldThrowUncheckedExceptionWhenErrorOccurred() {
        final var exceptionToThrow = new Exception("exception message");
        var throwingFunction = newProxy(exceptionToThrow);
        Object quietFunction = invokeQuiet(throwingFunction);
        if (!(quietFunction instanceof Function)) {
            fail(FI_QUIET_METHOD_NAME + " must return a Function");
        }

        try {
            ((Function) quietFunction).apply("argument");
        } catch (RuntimeException e) {
            assertEquals(RuntimeException.class, e.getClass(), FI_QUIET_METHOD_NAME + " must wrap exactly in RuntimeException");
            assertEquals(exceptionToThrow, e.getCause(), FI_QUIET_METHOD_NAME + " hides the original exception");
            return;
        } catch (Throwable e) {
            fail(FI_QUIET_METHOD_NAME + " failed to silence an exception: " + e.getMessage());
        }
        fail(FI_QUIET_METHOD_NAME + " completely hides the exception instead of throwing an unchecked one");
    }
    //endregion

    //region Custom Assertions & Reflection utilities
    private static void assertTrue(boolean condition, String message) {
        if (!condition) fail(message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        fail(message + " [Expected: " + expected + ", Actual: " + actual + "]");
    }

    private static void assertNotEquals(Object unexpected, Object actual, String message) {
        if (unexpected == null && actual == null) fail(message);
        if (unexpected != null && unexpected.equals(actual)) fail(message);
    }

    private static void assertNull(Object actual, String message) {
        if (actual != null) fail(message + " [Expected null, but was: " + actual + "]");
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    private static Class<?> findClass() {
        try {
            return Class.forName(FULL_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            fail(FI_CLASS_NAME + " does not exist");
            return null;
        }
    }

    private static Method findApplyMethod() {
        Class<?> fiClass = findClass();
        try {
            Method method = fiClass.getDeclaredMethod("apply", Object.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            fail(FI_CLASS_NAME + " must have a method named apply (T -> R)");
            return null;
        }
    }

    private static Method findQuietMethod() {
        Class<?> fiClass = findClass();
        try {
            Method method = fiClass.getDeclaredMethod("quiet", fiClass);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            fail(FI_CLASS_NAME + " must have a method named quiet (ThrowingFunction -> Function)");
            return null;
        }
    }

    private static ParameterizedType toParameterizedType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            fail(type.getTypeName() + " of " + FI_QUIET_METHOD_NAME + " is not parameterized");
        }
        return (ParameterizedType) type;
    }

    private static void assertThatTypeParametersValidity(String name, TypeVariable<?>[] typeParams) {
        assertEquals(3, typeParams.length, name + " must be properly generalized");
        boolean exceptionBounded = false;
        for (TypeVariable<?> typeParam : typeParams) {
            Type[] bounds = typeParam.getBounds();
            assertEquals(1, bounds.length, name + " type parameters might have only upper bound");
            String typeName = bounds[0].getTypeName();
            boolean isException = typeName.equals(Throwable.class.getCanonicalName());

            Class<?> clazz;
            try {
                clazz = Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                fail("Upper boundary is not reachable - " + typeName);
                return;
            }

            boolean isExceptionSubclass = Throwable.class.isAssignableFrom(clazz);
            if (exceptionBounded && isExceptionSubclass) {
                fail(name + " must have exactly one exception related generic type");
            }
            if (!exceptionBounded && isExceptionSubclass) {
                exceptionBounded = true;
            }
            if (isExceptionSubclass && !isException) {
                assertEquals(Throwable.class.getCanonicalName(), typeName, name + " exception related boundary must be Throwable");
            }
            boolean isObject = typeName.equals(Object.class.getCanonicalName());
            if (!isObject && !isException) {
                assertEquals(Object.class.getCanonicalName(), typeName, "Non exceptional generic type should not be bounded");
            }
        }
        if (!exceptionBounded) {
            fail(name + " must have exactly one exception related generic type, but was none");
        }
    }

    private static void assertThatAllGenericTypesAreDifferent(String name, Collection<Type> types) {
        if (types.size() != Set.copyOf(types).size()) {
            fail(name + " has reused type parameters");
        }
    }

    @SuppressWarnings("all")
    private static Object newProxy(Throwable exception) {
        Class<?> fiClass = findClass();
        return Proxy.newProxyInstance(fiClass.getClassLoader(), new Class[]{fiClass},
                (proxy, method, args) -> {
                    if (exception != null) {
                        throw exception;
                    }
                    return args[0];
                });
    }

    @SuppressWarnings("all")
    private static Object invokeQuiet(Object argument) {
        try {
            return findQuietMethod().invoke(null, argument);
        } catch (InvocationTargetException e) {
            fail(FI_QUIET_METHOD_NAME + " has thrown unexpected exception: " + e.getMessage());
        } catch (IllegalAccessException e) {
            fail("Failed to invoke " + FI_QUIET_METHOD_NAME);
        }
        throw new Error("Should never be thrown");
    }
    //endregion
}
/*
The program is a test framework that verifies the structure and functionality of a custom functional interface—ThrowingFunction—using Java Reflection.
Key Components of the Program
ThrowingFunction<A, E R,> Interface:
A functional interface that, unlike standard Java Function, allows checked exceptions to be thrown from its apply method.
Features a static quiet method that converts a exception-throwing function into a standard Java Function. 
If the function throws an exception at runtime, quiet automatically wraps it in an unchecked RuntimeException.
Reflection Tests (main and test methods):
The test environment dynamically inspects the ThrowingFunction class at runtime:
Structural Checks: Verifies whether the interface exists, carries the @FunctionalInterface annotation, and has an abstract apply method.
Generic Type Inspection: Ensures the correct number, bounds, and differentiation of type parameters (e.g., upper bound for the exception type is Throwable).
Method Signatures: Confirms that the quiet method is static, accepts the correct parameters, and returns a Function type.
Functional Tests: Validates that it correctly handles null values, performs normal transformations, and properly throws a wrapped RuntimeException when an error occurs.
*/

