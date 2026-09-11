import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class KaminoApp {

  // =========================================================================
  // 1. MODEL CLASSES
  // =========================================================================

  public static class Equipment {
    private String armor;
    private String weapon;

    public String getArmor() {
      return armor;
    }

    public void setArmor(String armor) {
      this.armor = armor;
    }

    public String getWeapon() {
      return weapon;
    }

    public void setWeapon(String weapon) {
      this.weapon = weapon;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Equipment equipment = (Equipment) o;
      return Objects.equals(getArmor(), equipment.getArmor()) &&
             Objects.equals(getWeapon(), equipment.getWeapon());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getArmor(), getWeapon());
    }

    @Override
    public String toString() {
      return "Equipment{" +
          "armor='" + armor + '\'' +
          ", weapon='" + weapon + '\'' +
          '}';
    }
  }

  public static final class EquipmentFactory {
    private EquipmentFactory() {}

    public static Equipment orderTheSame(Equipment equipment) {
      if (equipment == null) return null;
      Equipment ordered = new Equipment();
      ordered.setWeapon(equipment.getWeapon());
      ordered.setArmor(equipment.getArmor());
      return ordered;
    }
  }

  public static class CloneTrooper {
    private final String code;
    private String nickname;
    private Equipment equipment;

    CloneTrooper(String code) {
      this.code = Objects.requireNonNull(code);
    }

    public String getCode() {
      return code;
    }

    public String getNickname() {
      return nickname;
    }

    public void setNickname(String nickname) {
      this.nickname = nickname;
    }

    public Equipment getEquipment() {
      return equipment;
    }

    public void setEquipment(Equipment equipment) {
      this.equipment = equipment;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CloneTrooper that = (CloneTrooper) o;
      return Objects.equals(getCode(), that.getCode()) &&
             Objects.equals(getNickname(), that.getNickname()) &&
             Objects.equals(getEquipment(), that.getEquipment());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getCode(), getNickname(), getEquipment());
    }

    @Override
    public String toString() {
      return "CloneTrooper{" +
          "code='" + code + '\'' +
          ", nickname='" + nickname + '\'' +
          ", equipment=" + equipment +
          '}';
    }
  }

  // =========================================================================
  // 2. FUNCTIONAL LOGIC
  // =========================================================================

  public interface BatchPolicy {
    CloneTrooper[] formBatchOf(CloneTrooper base, int count);
  }

  public static class BatchPolicies {

    public static BatchPolicy getCodeAwarePolicy(String codePrefix, int codeSeed) {
      return (base, count) -> {
        CloneTrooper[] batch = new CloneTrooper[count];
        for (int i = 0; i < count; i++) {
          String code = String.format("%s-%04d", codePrefix, codeSeed + i);
          batch[i] = KaminoFactory.growClone(code);
        }
        return batch;
      };
    }

    public static BatchPolicy addNicknameAwareness(Iterator<String> nicknamesIterator, BatchPolicy policy) {
      return (base, count) -> {
        CloneTrooper[] batch = policy.formBatchOf(base, count);
        for (CloneTrooper clone : batch) {
          if (nicknamesIterator.hasNext()) {
            clone.setNickname(nicknamesIterator.next());
          }
        }
        return batch;
      };
    }

    public static BatchPolicy addEquipmentOrdering(Equipment equipmentExample, BatchPolicy policy) {
      return (base, count) -> {
        CloneTrooper[] batch = policy.formBatchOf(base, count);
        for (CloneTrooper clone : batch) {
          clone.setEquipment(EquipmentFactory.orderTheSame(equipmentExample));
        }
        return batch;
      };
    }
  }

  public static final class KaminoFactory {
    private KaminoFactory() {}

    public static CloneTrooper[] formBatch(BatchPolicy policy, CloneTrooper base, int count) {
      return policy.formBatchOf(base, count);
    }

    public static CloneTrooper growClone(String code) {
      return new CloneTrooper(code);
    }
  }

  // =========================================================================
  // 3. MAIN METHOD (RUNNING TESTS WITHOUT EXTERNAL DEPENDENCIES)
  // =========================================================================

  private static final CloneTrooper JANGO_FETT = new CloneTrooper("");

  public static void main(String[] args) {
    System.out.println("--- Starting Kamino Clone Factory tests ---\n");

    shouldCreateClonesWithCertainCodes();
    shouldCreateClonesWithProperlyFormattedCodes();
    shouldCreateClonesWithNicknames();
    shouldNotSetNicknamesIfThereIsNotEnoughNicknames();
    shouldSkipNicknamesWhenBatchIsLess();
    shouldProvideBatchWithEquipment();
    shouldHaveCopiesOfEquipment();

    System.out.println("\n--- All tests passed successfully! ---");
  }

  private static void shouldCreateClonesWithCertainCodes() {
    CloneTrooper[] batch = KaminoFactory.formBatch(
        BatchPolicies.getCodeAwarePolicy("ARC", 5555),
        JANGO_FETT, 10
    );
    CloneTrooper[] expectedBatch = IntStream.range(5555, 5565)
        .mapToObj(i -> clone("ARC-" + String.format("%04d", i), null, null))
        .toArray(CloneTrooper[]::new);

    assertArrayEquals(expectedBatch, batch, "Code generation test failed!");
    System.out.println("[OK] shouldCreateClonesWithCertainCodes");
  }

  private static void shouldCreateClonesWithProperlyFormattedCodes() {
    CloneTrooper[] batch = KaminoFactory.formBatch(
        BatchPolicies.getCodeAwarePolicy("CT", 1),
        JANGO_FETT, 1
    );
    CloneTrooper[] expectedBatch = {
        clone("CT-0001", null, null)
    };

    assertArrayEquals(expectedBatch, batch, "Code formatting test failed!");
    System.out.println("[OK] shouldCreateClonesWithProperlyFormattedCodes");
  }

  private static void shouldCreateClonesWithNicknames() {
    List<String> nicknames = List.of(
        "Boba Fett", "Rex", "Cody", "Hunter", "Omega",
        "Appo", "Gree", "Davijaan", "Bacara", "Wrecker"
    );
    CloneTrooper[] batch = KaminoFactory.formBatch(
        BatchPolicies.addNicknameAwareness(nicknames.iterator(), BatchPolicies.getCodeAwarePolicy("CC", 1010)),
        JANGO_FETT, nicknames.size()
    );
    CloneTrooper[] expectedBatch = IntStream.range(1010, 1010 + nicknames.size())
        .mapToObj(i -> clone(String.format("CC-%04d", i), nicknames.get(i - 1010), null))
        .toArray(CloneTrooper[]::new);

    assertArrayEquals(expectedBatch, batch, "Nickname assignment test failed!");
    System.out.println("[OK] shouldCreateClonesWithNicknames");
  }

  private static void shouldNotSetNicknamesIfThereIsNotEnoughNicknames() {
    List<String> nicknames = List.of("X1", "X2");
    CloneTrooper[] batch = KaminoFactory.formBatch(
        BatchPolicies.addNicknameAwareness(nicknames.iterator(), BatchPolicies.getCodeAwarePolicy("Beta", 1409)),
        JANGO_FETT, 3
    );
    CloneTrooper[] expectedBatch = {
        clone("Beta-1409", "X1", null),
        clone("Beta-1410", "X2", null),
        clone("Beta-1411", null, null)
    };

    assertArrayEquals(expectedBatch, batch, "Not enough nicknames test failed!");
    System.out.println("[OK] shouldNotSetNicknamesIfThereIsNotEnoughNicknames");
  }

  private static void shouldSkipNicknamesWhenBatchIsLess() {
    List<String> mockFileNicknames = List.of("A'den", "Able", "Ace", "Aeon", "Amp");
    
    CloneTrooper[] batch = KaminoFactory.formBatch(
        BatchPolicies.addNicknameAwareness(mockFileNicknames.iterator(), BatchPolicies.getCodeAwarePolicy("RC", 1207)),
        JANGO_FETT, 5
    );
    CloneTrooper[] expectedBatch = {
        clone("RC-1207", "A'den", null),
        clone("RC-1208", "Able", null),
        clone("RC-1209", "Ace", null),
        clone("RC-1210", "Aeon", null),
        clone("RC-1211", "Amp", null)
    };

    assertArrayEquals(expectedBatch, batch, "Excess nicknames test failed!");
    System.out.println("[OK] shouldSkipNicknamesWhenBatchIsLess");
  }

  private static void shouldProvideBatchWithEquipment() {
    Equipment equipment = new Equipment();
    equipment.setArmor("Katarn-class Commando");
    equipment.setWeapon("DC-17m IWS");

    CloneTrooper[] batch = KaminoFactory.formBatch(
        BatchPolicies.addEquipmentOrdering(equipment, BatchPolicies.getCodeAwarePolicy("TK", 9091)),
        JANGO_FETT, 25
    );
    CloneTrooper[] expectedBatch = IntStream.range(9091, 9116)
        .mapToObj(i -> clone(String.format("TK-%04d", i), null, equipment))
        .toArray(CloneTrooper[]::new);

    assertArrayEquals(expectedBatch, batch, "Equipment ordering test failed!");
    System.out.println("[OK] shouldProvideBatchWithEquipment");
  }

  private static void shouldHaveCopiesOfEquipment() {
    Equipment equipment = new Equipment();
    equipment.setArmor("Clone Training");
    equipment.setWeapon("DC-15A");

    CloneTrooper[] batch = KaminoFactory.formBatch(
        BatchPolicies.addEquipmentOrdering(equipment, BatchPolicies.getCodeAwarePolicy("Alpha", 1)),
        JANGO_FETT, 1
    );
    Equipment copyEquipment = new Equipment();
    copyEquipment.setArmor(equipment.getArmor());
    copyEquipment.setWeapon(equipment.getWeapon());

    CloneTrooper[] expectedBatch = {
        clone("Alpha-0001", null, copyEquipment)
    };

    assertArrayEquals(expectedBatch, batch, "Equipment copy test failed!");

    // Modify original equipment to ensure independence
    equipment.setWeapon("DC-15X");
    equipment.setArmor("Night Ops");

    assertArrayEquals(expectedBatch, batch, "Equipment must not be modified externally!");
    System.out.println("[OK] shouldHaveCopiesOfEquipment");
  }

  // =========================================================================
  // HELPER METHODS
  // =========================================================================

  private static CloneTrooper clone(String code, String nickname, Equipment equipment) {
    CloneTrooper trooper = new CloneTrooper(code);
    trooper.setNickname(nickname);
    trooper.setEquipment(equipment);
    return trooper;
  }

  private static void assertArrayEquals(Object[] expected, Object[] actual, String message) {
    if (!Arrays.equals(expected, actual)) {
      throw new AssertionError(message + "\nExpected: " + Arrays.toString(expected) + "\nActual:   " + Arrays.toString(actual));
    }
  }
}
/*
This program implements a clone trooper manufacturing system based on the Decorator design pattern, using an example from the Star Wars universe.
Model & Core Structure (CloneTrooper, Equipment)
Clones possess a unique code (code), an optional nickname (nickname), and gear (equipment).
The EquipmentFactory handles copying the gear (deep copy), ensuring each trooper receives an independent instance.
Decorator-Based Policy System (BatchPolicies)
Core Principle: The BatchPolicy interface is responsible for generating clone batches (CloneTrooper[]). These policies can be nested (chained).
getCodeAwarePolicy (Base Policy): Creates the specified number of clones with auto-incremented and formatted codes (e.g., ARC-5555, ARC-5556).
addNicknameAwareness (Decorator): Assigns nicknames from the provided list sequentially to the clones produced by the base policy for as long as names are available.
addEquipmentOrdering (Decorator): Assigns an independent copy of the specified equipment set to every clone.
Manufacturing & Testing (KaminoFactory & main)
KaminoFactory.formBatch() executes the combined policy chain.
The main method runs tests directly without JUnit or external files (verifying code generation, nickname assignment, and equipment copy isolation) to validate system functionality.
*/

