package utilities;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 14:27
 */
public interface GameCardConstants {
  /* Card Type */
  public static final Short CARD_TYPE_DEFAULT = 0;
  public static final Short CARD_TYPE_ATTACK = 1;
  public static final Short CARD_TYPE_DEFENSE = 2;
  public static final Short[] VALID_CARD_TYPES = {
      CARD_TYPE_DEFAULT, CARD_TYPE_ATTACK, CARD_TYPE_DEFENSE
  };

  /* Card Color */
  public static final Short CARD_COLOUR_SPADE = 0;
  public static final Short CARD_COLOUR_CLUBS = 1;
  public static final Short CARD_COLOUR_HEARTS = 2;
  public static final Short CARD_COLOUR_DIAMONDS = 3;
  public static final Short[] VALID_CARD_COLOURS = {
      CARD_COLOUR_SPADE, CARD_COLOUR_CLUBS, CARD_COLOUR_HEARTS,
      CARD_COLOUR_DIAMONDS
  };

  /* Card Value */
  public static final Short CARD_VALUE_TWO = 2;
  public static final Short CARD_VALUE_THREE = 3;
  public static final Short CARD_VALUE_FOUR = 4;
  public static final Short CARD_VALUE_FIVE = 5;
  public static final Short CARD_VALUE_SIX = 6;
  public static final Short CARD_VALUE_SEVEN = 7;
  public static final Short CARD_VALUE_EIGHT = 8;
  public static final Short CARD_VALUE_NINE = 9;
  public static final Short CARD_VALUE_TEN = 10;
  public static final Short CARD_VALUE_JACK = 11;
  public static final Short CARD_VALUE_QUEEN = 12;
  public static final Short CARD_VALUE_KING = 13;
  public static final Short CARD_VALUE_ACE = 14;
  public static final Short[] VALID_CARD_VALUES = {
      CARD_VALUE_TWO, CARD_VALUE_THREE, CARD_VALUE_FOUR, CARD_VALUE_FIVE,
      CARD_VALUE_SIX, CARD_VALUE_SEVEN, CARD_VALUE_EIGHT, CARD_VALUE_NINE,
      CARD_VALUE_TEN, CARD_VALUE_JACK, CARD_VALUE_QUEEN, CARD_VALUE_KING,
      CARD_VALUE_ACE
  };

  /* Observer information */
  public static final String BECAME_MOVABLE = "movable";
  public static final String BECAME_NOT_MOVABLE = "not movable";

  /* Card colour names */
  public static final String CARD_COLOUR_NAME_CLUBS = "Kreuz";
  public static final String CARD_COLOUR_NAME_DIAMONDS = "Karo";
  public static final String CARD_COLOUR_NAME_HEARTS = "Herz";
  public static final String CARD_COLOUR_NAME_SPADE = "Pik";

  /* Card value texts */
  public static final String CARD_VALUE_TEXT_ACE = "Ass";
  public static final String CARD_VALUE_TEXT_KING = "K\u00dfnig";
  public static final String CARD_VALUE_TEXT_QUEEN = "Dame";
  public static final String CARD_VALUE_TEXT_JACK = "Bube";
}
