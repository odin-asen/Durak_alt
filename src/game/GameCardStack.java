package game;

import utilities.constants.GameCardConstants;
import utilities.constants.GameConfigurationConstants;

import java.util.*;

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

  private Deque<GameCard> cardStack;

  public static GameCardStack getInstance() {
    return ourInstance;
  }

  private GameCardStack() {
    cardStack = new ArrayDeque<GameCard>();
  }

  /**
   * Initialises a shuffled stack with the number of {@code cardNumber} cards
   * for each of the 4 card colours.
   * <p>If {@code cardNumber} is higher than</p>
   * {@link utilities.constants.GameConfigurationConstants#MAXIMUM_COLOUR_CARD_COUNT}
   * <p>it will be limited to this value.</p>
   * @param cardNumber Number of cards for each colour.
   */
  public void initialiseStack(Integer cardNumber) {
    final List<GameCard> cardList;

    cardNumber = checkCardNumber(cardNumber);

    cardList = getSortedStack(cardNumber);
    Collections.shuffle(cardList);
    for (GameCard gameCard : cardList) {
      cardStack.add(gameCard);
    }
  }

  private Integer checkCardNumber(Integer cardNumber) {
    final int compareResult = cardNumber.compareTo(GameConfigurationConstants.MAXIMUM_COLOUR_CARD_COUNT);
    if(compareResult > 0)
      cardNumber = GameConfigurationConstants.MAXIMUM_COLOUR_CARD_COUNT;
    else if (compareResult < 0)
      cardNumber = 0;
    return cardNumber;
  }

  private List<GameCard> getSortedStack(Integer cardNumber) {
    final List<GameCard> list = new ArrayList<GameCard>(cardNumber);
    for (Short validCardColour : GameCardConstants.VALID_CARD_COLOURS) {
      for (int index = GameCardConstants.VALID_CARD_VALUES.length-1; index >=0; index++) {
        final Short validCardValue = GameCardConstants.VALID_CARD_VALUES[index];
        final GameCard card = new GameCard();
        card.setCardColour(validCardColour);
        card.setCardValue(validCardValue);
        card.setCardType(GameCardConstants.CARD_TYPE_DEFAULT);
        card.setMovable(false);
        list.add(card);
      }
    }
    return list;
  }

  public GameCard drawCard() {
    if(cardStack.isEmpty())
      return null;

    return cardStack.pop();
  }

  public Integer getStackSize() {
    return cardStack.size();
  }

  public GameCard getTrumpCard() {
    return cardStack.getLast();
  }

  public String toString() {
    String cards = "GameCardStack{\n"+
        "cardStack= ";
    for (GameCard gameCard : cardStack) {
      cards = cards + gameCard.toString() +'\n';
    }
    cards = cards + "\n}";
    return cards;
  }

  /* Getter and Setter */
  public Deque<GameCard> getCardStack() {
    return cardStack;
  }

  public void setCardStack(Deque<GameCard> cardStack) {
    this.cardStack = cardStack;
  }
}
