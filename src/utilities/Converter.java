package utilities;


import dto.DTOCard;
import dto.DTOCardStack;
import game.GameCard;
import game.GameCardStack;
import game.Player;
import utilities.constants.GameCardConstants;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 15:04
 */
@SuppressWarnings("unchecked")
public class Converter {
  private static final Logger LOGGER = Logger.getLogger(Converter.class.getName());

  public static DTOCard toDTO(GameCard card) {
    DTOCard dto = new DTOCard();
    dto.cardColour = card.getCardColour();
    dto.cardType = card.getCardType();
    dto.cardValue = card.getCardValue();
    return dto;
  }

  public static GameCard fromDTO(DTOCard dto) {
    GameCard card = new GameCard();
    card.setCardColour(dto.cardColour);
    card.setCardType(dto.cardType);
    card.setCardValue(dto.cardValue);
    return card;
  }

  public static DTOCardStack toDTO(GameCardStack stack) {
    DTOCardStack dto = new DTOCardStack();
    try {
      final Deque<GameCard> cardDeque = stack.getCardStack();
      dto.cardStack = (Deque<DTOCard>) Class.forName(cardDeque.getClass().getName()).newInstance();
      for (GameCard card  : cardDeque) {
        dto.cardStack.add(Converter.toDTO(card));
      }
    } catch (InstantiationException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    } catch (IllegalAccessException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    }

    return dto;
  }

  public static GameCardStack fromDTO(DTOCardStack dto) {
    GameCardStack stack = GameCardStack.getInstance();
    try {
      final Deque<GameCard> cardDeque = (Deque<GameCard>) Class.forName(dto.cardStack.getClass().getName()).newInstance();
      for (DTOCard card  : dto.cardStack) {
        cardDeque.add(Converter.fromDTO(card));
      }
      stack.setCardStack(cardDeque);
    } catch (InstantiationException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    } catch (IllegalAccessException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    }

    return stack;
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
    return GameCardConstants.CARD_VALUE_ACE.equals(value) ||
        GameCardConstants.CARD_VALUE_KING.equals(value) ||
        GameCardConstants.CARD_VALUE_QUEEN.equals(value) ||
        GameCardConstants.CARD_VALUE_JACK.equals(value);
  }

  public static List<List<DTOCard>> playerCardsToDTO(List<Player> playerList) {
    final List<List<DTOCard>> playersHands = new ArrayList<List<DTOCard>>(playerList.size());
    for (Player player : playerList) {
      final List<DTOCard> cards = new ArrayList<DTOCard>();
      for (GameCard gameCard : player.getCards()) {
        cards.add(Converter.toDTO(gameCard));
      }
      playersHands.add(cards);
    }
    return playersHands;
  }
}
