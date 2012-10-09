package server.business;

import dto.DTOCard;
import utilities.GameCardConstants;

import java.util.*;

import static server.ServerConfigurationConstants.*;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 19:49
 *
 * This represents the card stack of the game. Its implemented as Singleton
 * thus there should be only one card stack in a game session.
 */
public class GameCardStack {
  private static GameCardStack ourInstance = new GameCardStack();

  private Deque<DTOCard> cardStack;

  public static GameCardStack getInstance() {
    return ourInstance;
  }

  private GameCardStack() {
    cardStack = new ArrayDeque<DTOCard>();
  }

  /**
   * Initializes a shuffled stack with the number of {@code cardNumber} cards
   * for each of the 4 card colours.
   * <p>If {@code cardNumber} is higher than</p>
   * {@link server.ServerConfigurationConstants#MAXIMUM_COLOUR_CARD_COUNT}
   * <p>it will be limited to this value.</p>
   * @param cardNumber Number of cards for each colour.
   */
  public void initializeStack(Integer cardNumber) {
    final List<DTOCard> cardList;

    cardNumber = checkCardNumber(cardNumber);

    cardList = getSortedStack(cardNumber);
    Collections.shuffle(cardList);
    for (DTOCard dtoCard : cardList) {
      cardStack.add(dtoCard);
    }
  }

  private Integer checkCardNumber(Integer cardNumber) {
    final int compareResult = cardNumber.compareTo(MAXIMUM_COLOUR_CARD_COUNT);
    if(compareResult > 0)
      cardNumber = new Integer(MAXIMUM_COLOUR_CARD_COUNT);
    else if (compareResult < 0)
      cardNumber = new Integer(0);
    return cardNumber;
  }

  private List<DTOCard> getSortedStack(Integer cardNumber) {
    final List<DTOCard> list = new ArrayList<DTOCard>(cardNumber);
    for (Short validCardColour : GameCardConstants.VALID_CARD_COLOURS) {
      for (int index = GameCardConstants.VALID_CARD_VALUES.length-1; index >=0; index++) {
        final Short validCardValue = GameCardConstants.VALID_CARD_VALUES[index];
        final DTOCard card = new DTOCard();
        card.cardColor = validCardColour;
        card.cardValue = validCardValue;
        card.cardType = GameCardConstants.CARD_TYPE_DEFAULT;
        card.movable = false;
        list.add(card);
      }
    }
    return list;
  }

  public DTOCard drawCard() {
    if(cardStack.isEmpty())
      return null;

    return cardStack.pop();
  }

  public Integer getStackSize() {
    return cardStack.size();
  }

  public DTOCard getTrumpCard() {
    return cardStack.getLast();
  }
}
