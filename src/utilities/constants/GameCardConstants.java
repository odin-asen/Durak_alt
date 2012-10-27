package utilities.constants;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 14:27
 */
public interface GameCardConstants {
  /* Card Type */
  public static enum CardType {
    DEFAULT(0),
    ATTACK(1),
    DEFENSE(2);

    private Integer value;
    CardType(Integer value) {
      this.value = value;
    }

    public Integer getValue() {
      return value;
    }
  }

  /* Card Color */
  public static enum CardColour {
    SPADE(0, "Pik"),
    CLUBS(1, "Kreuz"),
    HEARTS(2, "Herz"),
    DIAMONDS(3, "Karo");

    private Integer value;
    private String name;
    CardColour(Integer value, String name) {
      this.value = value;
      this.name = name;
    }

    public Integer getValue() {
      return value;
    }

    public String getName() {
      return name;
    }

    public String toString() {
      return "CardColour{" +
          "name='" + name + '\'' +
          ", value=" + value +
          '}';
    }
  }

  /* Card Value */
  public static enum CardValue {
    TWO(1, "2"),
    THREE(2, "3"),
    FOUR(3, "4"),
    FIVE(4, "5"),
    SIX(5, "6"),
    SEVEN(6, "7"),
    EIGHT(7, "8"),
    NINE(8, "9"),
    TEN(9, "10"),
    JACK(10, "Bube"),
    QUEEN(11, "Dame"),
    KING(12, "K\u00f6nig"),
    ACE(0, "Ass");

    private Integer value;
    private String valueName;
    CardValue(Integer value, String valueName) {
      this.value = value;
      this.valueName = valueName;
    }

    public Integer getValue() {
      return value;
    }

    public String getValueName() {
      return valueName;
    }

    public String toString() {
      return "CardValue{" +
          "value=" + value +
          ", valueName='" + valueName + '\'' +
          '}';
    }

    /**
     * Returns a specified number of values descending from the highest value.
     * If {@code valueCount} is bigger than the number of values, all values
     * will be returned.
     * @param valueCount Number of values that will be returned.
     * @return An array of the values.
     */
    public static CardValue[] values(Integer valueCount) {
      final CardValue[] allValues = CardValue.values();
      final CardValue[] values;

      if(valueCount.compareTo(allValues.length) > 0)
        valueCount = allValues.length;
      else if(valueCount.compareTo(allValues.length) < 0)
        if(valueCount < 0)
          valueCount = 0;

      values = new CardValue[valueCount];
      System.arraycopy(allValues, allValues.length - valueCount, values, 0, valueCount);

      return values;
    }
  }

  /* Observer information */
  public static final String BECAME_MOVABLE = "movable";
  public static final String BECAME_NOT_MOVABLE = "not movable";

}
