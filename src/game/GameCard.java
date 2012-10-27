package game;

import static utilities.constants.GameCardConstants.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:26
 */
public class GameCard {
  private CardType cardType;
  private CardValue cardValue;
  private CardColour cardColour;

  public GameCard() {
    this.cardType = CardType.DEFAULT;
  }

  /* Methods */
  public String toString() {
    return "GameCard{" +
        "cardType=" + cardType +
        ", cardValue=" + cardValue +
        ", cardColour=" + cardColour +
        '}';
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

  /* Getter and Setter */
  public void setCardType(CardType type) {
    this.cardType = type;
  }

  public CardType getCardType() {
    return cardType;
  }

  public CardValue getCardValue() {
    return cardValue;
  }

  public void setCardValue(CardValue cardValue) {
    this.cardValue = cardValue;
  }

  public CardColour getCardColour() {
    return cardColour;
  }

  public void setCardColour(CardColour cardColour) {
    this.cardColour = cardColour;
  }
}
