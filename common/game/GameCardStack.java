package common.game;

import common.utilities.constants.GameConfigurationConstants;

import java.util.*;

import static common.utilities.constants.GameCardConstants.*;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 19:49
 *
 * This class represents the card stack of the game.
 */
public class GameCardStack extends Observable {
  private Integer stackSize;

  private Deque<GameCard> cardStack;
  private GameCard trumpCard;

  public GameCardStack() {
    cardStack = new ArrayDeque<GameCard>();
    trumpCard = null;
  }

  /**
   * Initialises a shuffled stack with the number of {@code cardColourNumber} cards
   * for each of the 4 card colours.
   * <p>If {@code cardColourNumber} is higher than</p>
   * {@link utilities.constants.GameConfigurationConstants#MAXIMUM_COLOUR_CARD_COUNT}
   * <p>it will be limited to this value.</p>
   * @param cardColourNumber Number of cards for each colour.
   */
  public void initialiseStack(Integer cardColourNumber) {
    final List<GameCard> cardList;

    stackSize = returnStackSize(cardColourNumber);

    cardList = getSortedStack(stackSize/CardColour.values().length);

    Collections.shuffle(cardList);
    for (GameCard gameCard : cardList) {
      cardStack.add(gameCard);
    }
    trumpCard = cardStack.getLast();
  }

  /**
   * Initialises a new shuffled stack with the number that was used for all card colours
   * at the initialising of the stack.
   */
  public void reshuffleStack() {
    final List<GameCard> cardList;

    cardList = getSortedStack(stackSize/CardColour.values().length);
    Collections.shuffle(cardList);
    for (GameCard gameCard : cardList) {
      cardStack.add(gameCard);
    }
  }

  /**
   * Returns the stack size for the specified number of cards for each colour.
   * @param cardColourNumber Number of cards for each colour
   * @return An Integer object that represents the stack size.
   */
  private Integer returnStackSize(Integer cardColourNumber) {
    final int compareResult = cardColourNumber.compareTo(GameConfigurationConstants.MAXIMUM_COLOUR_CARD_COUNT);
    if(compareResult > 0)
      cardColourNumber = GameConfigurationConstants.MAXIMUM_COLOUR_CARD_COUNT;
    else if (compareResult < 0)
      if(cardColourNumber < 0)
        cardColourNumber = 0;

    return cardColourNumber*CardColour.values().length;
  }

  private List<GameCard> getSortedStack(Integer cardsPerColour) {
    final List<GameCard> list = new ArrayList<GameCard>(cardsPerColour*CardColour.values().length);
    final CardValue[] values = CardValue.values(cardsPerColour);

    for (CardColour cardColour : CardColour.values()) {
      for (CardValue cardValue : values) {
        final GameCard card = new GameCard();
        card.setCardColour(cardColour);
        card.setCardValue(cardValue);
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
    return trumpCard;
  }

  @SuppressWarnings("ALL")
  public String toString() {
    String cards = "GameCardStack = {" +
        "cardStack= ";
    for (GameCard gameCard : cardStack) {
      cards = cards + gameCard.toString() +'\n';
    }
    cards = cards + "}";
    return cards;
  }

  /* Getter and Setter */
  public Deque<GameCard> getCardStack() {
    return cardStack;
  }

  public void setCardStack(Deque<GameCard> cardStack) {
    this.cardStack = cardStack;
  }

  public void setTrumpCard(GameCard trumpCard) {
    this.trumpCard = trumpCard;
  }
}
