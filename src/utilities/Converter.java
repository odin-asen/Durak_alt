package utilities;

import client.business.GameCard;
import dto.DTOCard;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 15:04
 */
public class Converter {

  public static DTOCard toDTO(GameCard card) {
    DTOCard dto = new DTOCard();
    dto.cardColor = card.getCardColour();
    dto.cardType = card.getCardType();
    dto.cardValue = card.getCardValue();
    return dto;
  }

  public static GameCard fromDTO(DTOCard dto) {
    GameCard card = new GameCard();
    card.setCardColour(dto.cardColor);
    card.setCardType(dto.cardType);
    card.setCardValue(dto.cardValue);
    return card;
  }

  public static String getCardColourName(Short value) {
    final String text;
    if(GameCardConstants.CARD_COLOUR_CLUBS.equals(value)) {
      text = GameCardConstants.CARD_COLOUR_NAME_CLUBS;
    } else if(GameCardConstants.CARD_COLOUR_DIAMONDS.equals(value)) {
      text = GameCardConstants.CARD_COLOUR_NAME_DIAMONDS;
    } else if(GameCardConstants.CARD_COLOUR_HEARTS.equals(value)) {
      text = GameCardConstants.CARD_COLOUR_NAME_HEARTS;
    } else if(GameCardConstants.CARD_COLOUR_SPADE.equals(value)) {
      text = GameCardConstants.CARD_COLOUR_NAME_SPADE;
    } else {
      text = "";
    }
    return text;
  }

  public static String getCardValueName(Short value) {
    final String text;
    boolean isPictureCard = false;
    for (Short validCardValue : GameCardConstants.VALID_CARD_VALUES) {
      if(validCardValue.equals(value)) {
        isPictureCard = isPictureCard(value);
      }
    }

    if(!isPictureCard)
      text = Short.toString(value);
    else {
      if(GameCardConstants.CARD_VALUE_ACE.equals(value))
        text = GameCardConstants.CARD_VALUE_TEXT_ACE;
      else if(GameCardConstants.CARD_VALUE_KING.equals(value))
        text = GameCardConstants.CARD_VALUE_TEXT_KING;
      else if(GameCardConstants.CARD_VALUE_QUEEN.equals(value))
        text = GameCardConstants.CARD_VALUE_TEXT_QUEEN;
      else
        text = GameCardConstants.CARD_VALUE_TEXT_JACK;
    }

    return text;
  }

  public static boolean isPictureCard(Short value) {
    if(GameCardConstants.CARD_VALUE_ACE.equals(value) ||
       GameCardConstants.CARD_VALUE_KING.equals(value) ||
       GameCardConstants.CARD_VALUE_QUEEN.equals(value) ||
       GameCardConstants.CARD_VALUE_JACK.equals(value))
      return true;
    else
      return false;
  }
}
