package game;

import static utilities.constants.GameCardConstants.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:26
 */
public class GameCard {
  private CardType cardType;
  private Boolean movable;      //TODO löschen und getter und setter in GameCardWidget übertragen
  private CardValue cardValue;
  private CardColour cardColour;

  public GameCard() {
    this.movable = false;
    this.cardType = CardType.DEFAULT;
  }

  public Boolean isMovable() {
    return movable;
  }

  public void setMovable(Boolean movable) {
    if(movable && !this.movable) {
      this.movable = movable;
    } else if(!movable && this.movable) {
      this.movable = movable;
    }
  }

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

  public String toString() {
    return "GameCard{" +
        "cardType=" + cardType +
        ", movable=" + movable +
        ", cardValue=" + cardValue +
        ", cardColour=" + cardColour +
        '}';
  }

  /**
   * Determines if the value of this card is higher than
   * the arguments.
   * @param card The argument.
   * @return Returns only true, if the argument is null or the
   * this cards value is higher than the arguments.
   */
  public boolean hasHigherValue(GameCard card) {
    if(card != null) {
      if(this.getCardValue().compareTo(card.getCardValue())<=0)
        return false;
      else return true;
    }
    //TODO compareTo methode muss getestet werden, da jetzt Enums verglichen werden
    return true;
  }

  /**
   * Determines if the value of this card is lower than
   * the arguments.
   * @param card The argument.
   * @return Returns only true, if the argument is not null
   * and the this cards value is lower than the arguments.
   */
  public boolean hasLowerValue(GameCard card) {
    if(card != null) {
      if(this.getCardValue().compareTo(card.getCardValue())>=0)
        return false;
      else return true;
    }
    //TODO compareTo methode muss getestet werden, da jetzt Enums verglichen werden
    return false;
  }
}
