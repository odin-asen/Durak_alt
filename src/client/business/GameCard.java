package client.business;

import utilities.GameCardConstants;

import java.util.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:26
 */
public class GameCard extends Observable{
  private Short cardType;
  private Boolean movable;
  private Short cardValue;
  private Short cardColour;

  public GameCard() {
    this.movable = false;
    this.cardType = GameCardConstants.CARD_TYPE_DEFAULT;
  }

  public Boolean isMovable() {
    return movable;
  }

  public void setMovable(Boolean movable) {
    if(movable && !this.movable) {
      this.movable = movable;
      this.setChanged();
      this.notifyObservers(GameCardConstants.BECAME_MOVABLE);
      this.clearChanged();
    } else if(!movable && this.movable) {
      this.movable = movable;
      this.setChanged();
      this.notifyObservers(GameCardConstants.BECAME_NOT_MOVABLE);
      this.clearChanged();
    }
  }

  public void setCardType(Short type) {
    this.cardType = (Short) setValue(Arrays.asList(GameCardConstants.VALID_CARD_TYPES),
        type, GameCardConstants.CARD_TYPE_DEFAULT);
  }

  public Short getCardType() {
    return cardType;
  }

  public Short getCardValue() {
    return cardValue;
  }

  public void setCardValue(Short cardValue) {
    this.cardValue = (Short) setValue(Arrays.asList(GameCardConstants.VALID_CARD_VALUES),
        cardValue, GameCardConstants.CARD_VALUE_TWO);
  }

  public Short getCardColour() {
    return cardColour;
  }

  public void setCardColour(Short cardColour) {
    this.cardColour = (Short) setValue(Arrays.asList(GameCardConstants.VALID_CARD_COLOURS),
        cardColour, GameCardConstants.CARD_TYPE_DEFAULT);
  }

  private Object setValue(List<Short> valueSet, Object valueToSet, Object defaultValue) {
    for (Object value : valueSet) {
      if(value.equals(valueToSet)) {
        return valueToSet;
      }
    }
    return defaultValue;
  }
}
