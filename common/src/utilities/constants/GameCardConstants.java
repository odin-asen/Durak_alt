package utilities.constants;

import resources.I18nSupport;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 14:27
 */
public interface GameCardConstants {
  String BUNDLE_NAME = "general.cards"; //NON-NLS

  /* Card Color */
  public static enum CardColour {
    SPADES(0, I18nSupport.getValue(BUNDLE_NAME,"colour.spades")),
    CLUBS(1, I18nSupport.getValue(BUNDLE_NAME,"colour.clubs")),
    HEARTS(2, I18nSupport.getValue(BUNDLE_NAME,"colour.hearts")),
    DIAMONDS(3, I18nSupport.getValue(BUNDLE_NAME,"colour.diamonds"));

    private final Integer value;
    private final String name;
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

    @SuppressWarnings("ALL")
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
    JACK(10, I18nSupport.getValue(BUNDLE_NAME,"value.jack")),
    QUEEN(11, I18nSupport.getValue(BUNDLE_NAME,"value.queen")),
    KING(12, I18nSupport.getValue(BUNDLE_NAME,"value.king")),
    ACE(0, I18nSupport.getValue(BUNDLE_NAME,"value.ace"));

    private final Integer value;
    private final String valueName;
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

    @SuppressWarnings("ALL")
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
}
