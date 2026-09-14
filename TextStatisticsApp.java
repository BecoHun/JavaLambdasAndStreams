import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class TextStatisticsApp {

  // --- Functional Interface ---

  @FunctionalInterface
  public interface TokenStatisticsCalculator<K, V> {
    void calculate(Map<K, V> statistics, Iterator<K> tokens);
  }

  // --- Token Record ---

  public record Token(String name, Type type) {

    public Token(String name) {
      this(name, Type.WORD);
    }

    public static Token token(String name, Type type) {
      return new Token(name, type);
    }

    public static Token token(String name) {
      return new Token(name);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || !obj.getClass().equals(this.getClass())) return false;
      Token that = (Token) obj;
      return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name);
    }

    public enum Type {
      WORD,
      CUSTOM,
      NUMBER,
      CODE
    }
  }

  // --- TextStatistics Logic ---

  public static final class TextStatistics {

    private TextStatistics() {
      throw new UnsupportedOperationException();
    }

    public static TokenStatisticsCalculator<Token, Long> countTokens() {
      return (statistics, tokens) -> {
        while (tokens.hasNext()) {
          statistics.merge(tokens.next(), 1L, Long::sum);
        }
      };
    }

    public static TokenStatisticsCalculator<Token, Long> countKnownTokensWithMaxLimit(int maxLimit) {
      return (statistics, tokens) -> {
        while (tokens.hasNext()) {
          statistics.computeIfPresent(tokens.next(), (key, value) ->
              value < maxLimit ? value + 1L : value);
        }
      };
    }

    public static TokenStatisticsCalculator<Token, Boolean> findUnknownTokensOfTypes(Set<Token.Type> types) {
      Objects.requireNonNull(types);
      return (statistics, tokens) -> {
        while (tokens.hasNext()) {
          Token token = tokens.next();
          if (types.contains(token.type())) {
            statistics.computeIfAbsent(token, key -> true);
          }
        }
      };
    }

    public static TokenStatisticsCalculator<Token, Integer> combinedSearch(int maxLimit, Set<Token.Type> types) {
      Objects.requireNonNull(types);
      return (statistics, tokens) -> {
        int index = 0;
        while (tokens.hasNext()) {
          Token token = tokens.next();
          if (!statistics.containsKey(token)) {
            statistics.computeIfAbsent(token, key -> -1);
          } else if (index < maxLimit) {
            int finalIndex = index;
            statistics.computeIfPresent(token, (key, value) -> finalIndex);
            index++;
          }
        }
      };
    }
  }

  // --- Executable Main Method (with tests) ---

  public static void main(String[] args) {
    System.out.println("Running tests...");

    testCountTokens();
    testCountKnownTokensWithMaxLimit();
    testFindUnknownTokensOfTypes();
    testCombinedSearch();

    System.out.println("All tests passed successfully!");
  }

  private static void testCountTokens() {
    Map<Token, Long> inputMap = new HashMap<>();
    List<Token> tokens = Stream.of(
        "Heap", "Lend", "Moon", "Fun", "Language",
        "Fun", "Obey", "Heap", "Obey", "Moon",
        "Obey", "Obey", "Language", "Fun", "Heap"
    ).map(Token::new).toList();

    Map<Token, Long> expectedMap = Map.of(
        Token.token("Heap"), 3L,
        Token.token("Lend"), 1L,
        Token.token("Moon"), 2L,
        Token.token("Language"), 2L,
        Token.token("Obey"), 4L,
        Token.token("Fun"), 3L
    );

    test(TextStatistics.countTokens(), inputMap, tokens.iterator(), expectedMap);
    System.out.println("-> testCountTokens PASSED");
  }

  private static void testCountKnownTokensWithMaxLimit() {
    Map<Token, Long> inputMap = new HashMap<>(Map.of(
        Token.token("name"), 8L,
        Token.token("surname"), 11L,
        Token.token("age"), 10L,
        Token.token("gender"), 6L,
        Token.token("education"), 3L,
        Token.token("specialization"), 7L
    ));

    List<Token> tokens = List.of(
        Token.token("name"),
        Token.token("name"),
        Token.token("name"),
        Token.token("surname"),
        Token.token("surname"),
        Token.token("specialization"),
        Token.token("specialization")
    );

    Map<Token, Long> expectedMap = Map.of(
        Token.token("name"), 10L,
        Token.token("surname"), 11L,
        Token.token("age"), 10L,
        Token.token("gender"), 6L,
        Token.token("education"), 3L,
        Token.token("specialization"), 9L
    );

    test(TextStatistics.countKnownTokensWithMaxLimit(10), inputMap, tokens.iterator(), expectedMap);
    System.out.println("-> testCountKnownTokensWithMaxLimit PASSED");
  }

  private static void testFindUnknownTokensOfTypes() {
    Map<Token, Boolean> inputMap = new HashMap<>(Map.of(
        Token.token("name", Token.Type.WORD), false,
        Token.token("age", Token.Type.NUMBER), false,
        Token.token("education", Token.Type.CUSTOM), false,
        Token.token("specialization", Token.Type.CUSTOM), false,
        Token.token("id", Token.Type.CODE), false
    ));

    List<Token> tokens = List.of(
        Token.token("id", Token.Type.CODE),
        Token.token("insurance id", Token.Type.CODE),
        Token.token("education", Token.Type.CUSTOM),
        Token.token("driver licence", Token.Type.CODE),
        Token.token("courses", Token.Type.CUSTOM),
        Token.token("salary", Token.Type.NUMBER),
        Token.token("company", Token.Type.WORD)
    );

    Map<Token, Boolean> expectedMap = Map.of(
        Token.token("name", Token.Type.WORD), false,
        Token.token("age", Token.Type.NUMBER), false,
        Token.token("education", Token.Type.CUSTOM), false,
        Token.token("specialization", Token.Type.CUSTOM), false,
        Token.token("id", Token.Type.CODE), false,
        Token.token("insurance id", Token.Type.CODE), true,
        Token.token("driver licence", Token.Type.CODE), true,
        Token.token("courses", Token.Type.CUSTOM), true
    );

    test(TextStatistics.findUnknownTokensOfTypes(Set.of(Token.Type.CODE, Token.Type.CUSTOM)), inputMap, tokens.iterator(), expectedMap);
    System.out.println("-> testFindUnknownTokensOfTypes PASSED");
  }

  private static void testCombinedSearch() {
    Map<Token, Integer> inputMap = new HashMap<>(Map.of(
        Token.token("normal", Token.Type.WORD), 5,
        Token.token("exceeded", Token.Type.NUMBER), 1732,
        Token.token("specific", Token.Type.CUSTOM), 4,
        Token.token("broken", Token.Type.CODE), 28147912
    ));

    List<Token> tokens = List.of(
        Token.token("non-existed"),
        Token.token("normal", Token.Type.WORD),
        Token.token("exceeded", Token.Type.NUMBER),
        Token.token("specific", Token.Type.CUSTOM),
        Token.token("broken", Token.Type.CODE)
    );

    Map<Token, Integer> expectedMap = Map.of(
        Token.token("non-existed"), -1,
        Token.token("normal", Token.Type.WORD), 0,
        Token.token("exceeded", Token.Type.NUMBER), 1,
        Token.token("specific", Token.Type.CUSTOM), 2,
        Token.token("broken", Token.Type.CODE), 3
    );

    test(TextStatistics.combinedSearch(10, Set.of(Token.Type.WORD, Token.Type.NUMBER)), inputMap, tokens.iterator(), expectedMap);
    System.out.println("-> testCombinedSearch PASSED");
  }

  // --- Test Procedure and Restricted Map Class ---

  private static <K, V> void test(TokenStatisticsCalculator<K, V> calculator, Map<K, V> inputMap, Iterator<K> tokens, Map<K, V> expectedMap) {
    Map<K, V> copyMap = new RestrictedMap<>(inputMap);
    calculator.calculate(copyMap, tokens);

    if (copyMap.size() != expectedMap.size() || !copyMap.keySet().containsAll(expectedMap.keySet())) {
      throw new AssertionError("Map size or key set mismatch!");
    }

    for (Map.Entry<K, V> entry : copyMap.entrySet()) {
      V expectedValue = expectedMap.get(entry.getKey());
      if (!Objects.equals(entry.getValue(), expectedValue)) {
        throw new AssertionError("Value mismatch for key: " + entry.getKey() + ". Expected: " + expectedValue + ", Actual: " + entry.getValue());
      }
    }
  }

  private record RestrictedMap<K, V>(Map<K, V> map) implements Map<K, V> {
    @Override public int size() { return map.size(); }
    @Override public boolean isEmpty() { return map.isEmpty(); }
    @Override public boolean containsKey(Object key) { return map.containsKey(key); }
    @Override public boolean containsValue(Object value) { return map.containsValue(value); }
    @Override public V get(Object key) { return map.get(key); }
    @Override public V put(K key, V value) { throw unsupported(); }
    @Override public V remove(Object key) { throw unsupported(); }
    @Override public void putAll(Map<? extends K, ? extends V> m) { throw unsupported(); }
    @Override public void clear() { throw unsupported(); }
    @Override public Set<K> keySet() { return Set.copyOf(map.keySet()); }
    @Override public Collection<V> values() { return List.copyOf(map.values()); }
    @Override public Set<Entry<K, V>> entrySet() { return map.entrySet(); }
    @Override public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) { throw unsupported(); }
    @Override public V putIfAbsent(K key, V value) { throw unsupported(); }
    @Override public boolean remove(Object key, Object value) { throw unsupported(); }
    @Override public boolean replace(K key, V oldValue, V newValue) { throw unsupported(); }
    @Override public V replace(K key, V value) { throw unsupported(); }
    @Override public void forEach(BiConsumer<? super K, ? super V> action) { map.forEach(action); }
    @Override public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) { return map.computeIfAbsent(key, mappingFunction); }
    @Override public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) { return map.computeIfPresent(key, remappingFunction); }
    @Override public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) { return map.compute(key, remappingFunction); }
    @Override public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) { return map.merge(key, value, remappingFunction); }
    @Override public V getOrDefault(Object key, V defaultValue) { return map.getOrDefault(key, defaultValue); }

    private UnsupportedOperationException unsupported() {
      return new UnsupportedOperationException("Map modification operation is restricted!");
    }
  }
}
/*
This provided Java code is a text-analysis and statistics-generating application that processes text elements (Tokens) using lambdas and methods of the Java Map interface.
Key Features:
Token record: 
Represents an element with a name and a type (WORD, NUMBER, CODE, CUSTOM). Equality checks (equals/hashCode) are based strictly on the name.
TextStatistics class (Lambda calculators):
countTokens(): 
Counts the occurrences of each token in an existing map (Map.merge).
countKnownTokensWithMaxLimit(): 
Increments the count only for already existing (known) tokens, up to a specified upper limit (maxLimit).
findUnknownTokensOfTypes(): 
Identifies unknown tokens matching specific types and registers them in the map with a value of true.
combinedSearch(): 
Assigns a value of -1 to unknown tokens and updates the value of known tokens to an incremental index up to the defined limit.
Built-in Test Suite (main & RestrictedMap):
The code tests its functionality using its own executable main method, without relying on external test frameworks (such as JUnit).
RestrictedMap is a safety wrapper class that ensures the calculators exclusively use allowed atomic Map methods (computeIfPresent, merge, etc.) 
instead of direct modifications (put, remove).
*/

