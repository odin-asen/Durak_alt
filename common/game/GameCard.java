package common.game;

import static common.utilities.constants.GameCardConstants.CardColour;
import static common.utilities.constants.GameCardConstants.CardValue;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:26
 *
 * This class is immutable.
 */
public class GameCard {
  private CardValue cardValue;
  private CardColour cardColour;

  public GameCard(CardValue value, CardColour colour) {
    cardValue = value;
    cardColour = colour;
  }

  /* Methods */
  @SuppressWarnings("ALL")
  public String toString() {
    return "GameCard = {" +
        " cardValue=" + cardValue +
        ", cardColour=" + cardColour +
        '}';
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GameCard)) return false;

    GameCard gameCard = (GameCard) o;

    return cardColour == gameCard.cardColour && cardValue == gameCard.cardValue;
  }

  public int hashCode() {
    int result = cardValue.hashCode();
    result = 31 * result + cardColour.hashCode();
    return result;
  }

  /**
   * Determines if the value of this card is higher than
   * the arguments.
   * @param card The argument.
   * @return Returns only true, if the argument is null or
   * this cards value is higher than the arguments.
   */
  public boolean hasHigherValue(GameCard card) {
    return card == null || this.getCardValue().compareTo(card.getCardValue()) > 0;
  }

  /**
   * Determines if the value of this card is lower than
   * the arguments.
   * @param card The argument.
   * @return Returns true, if the argument is null or
   * this cards value is lower than the arguments.
   */
  public boolean hasLowerValue(GameCard card) {
    return card == null || this.getCardValue().compareTo(card.getCardValue()) < 0;
  }

  public String getColourAndValue() {
    return cardColour.getName()+" "+cardValue.getValueName();
  }

  /* Getter and Setter */

  public CardValue getCardValue() {
    return cardValue;
  }

  public CardColour getCardColour() {
    return cardColour;
  }
}
