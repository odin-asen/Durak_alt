package dto;

import utilities.constants.GameCardConstants;

import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 14:34
 */
public class DTOCard implements Serializable {
  public short cardType;
  public boolean movable;
  public short cardValue;
  public short cardColour;

  /* Constructors */
  public DTOCard() {
    cardType = GameCardConstants.CARD_TYPE_DEFAULT;
    cardValue = GameCardConstants.CARD_VALUE_ACE;
    cardColour = GameCardConstants.CARD_COLOUR_CLUBS;
    movable = false;
  }

  /* Methods */
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
