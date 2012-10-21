package dto;

import java.io.Serializable;

import static utilities.constants.GameCardConstants.*;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 14:34
 */
public class DTOCard implements Serializable {
  public CardType cardType;
  public boolean movable;
  public CardValue cardValue;
  public CardColour cardColour;

  /* Constructors */
  public DTOCard() {
    cardType = CardType.DEFAULT;
    cardValue = CardValue.ACE;
    cardColour = CardColour.CLUBS;
    movable = false;
  }

  /* Methods */
  public String getColourAndValue() {
    return cardColour.getName()+" "+cardValue.getValueName();
  }

  public String toString() {
    return "DTOCard{" +
        "cardType=" + cardType +
        ", movable=" + movable +
        ", cardValue=" + cardValue +
        ", cardColour=" + cardColour +
        '}';
  }
  /* Getter and Setter */
}
