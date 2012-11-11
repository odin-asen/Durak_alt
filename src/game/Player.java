package game;

import utilities.constants.GameConfigurationConstants;

import java.util.ArrayList;
import java.util.List;

import static utilities.constants.GameCardConstants.CardColour;
import static utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 19:32
 *
 * This class represents a player in a game.
 */
public class Player {
  private Player leftPlayer;
  private Player rightPlayer;

  private List<GameCard> cards;
  private PlayerType type;

  /* Constructors */
  public Player() {
    cards = new ArrayList<GameCard>(GameConfigurationConstants.INITIAL_CARD_COUNT);
    leftPlayer = null;
    rightPlayer = null;
    type = PlayerType.DEFAULT;
  }


  /* Methods */
  public void pickUpCard(GameCard newCard) {
    cards.add(newCard);
  }

  public void useCard(GameCard card) {
    final GameCard found = getCard(card);
    if(found != null)
      cards.remove(found);
  }

  public GameCard getCard(GameCard card) {
    for (GameCard gameCard : cards) {
      if(gameCard.equals(card))
        return gameCard;
    }

    return null;
  }

  /**
   * Returns the card with the smallest value of a specified colour.
   * @param colour Colour of the card.
   * @return Returns the smallest card of the specified colour or
   * null if no card of this colour could be found.
   */
  public GameCard getSmallestValue(CardColour colour) {
    GameCard card = null;
    for (GameCard gameCard : cards) {
      if(gameCard.getCardColour().equals(colour))
        if(gameCard.hasLowerValue(card))
          card = gameCard;
    }
    return card;
  }

  /* Getter and Setter */
  public Player getLeftPlayer() {
    return leftPlayer;
  }

  public void setLeftPlayer(Player leftPlayer) {
    this.leftPlayer = leftPlayer;
  }

  public Player getRightPlayer() {
    return rightPlayer;
  }

  public void setRightPlayer(Player rightPlayer) {
    this.rightPlayer = rightPlayer;
  }

  public List<GameCard> getCards() {
    return cards;
  }

  public PlayerType getType() {
    return type;
  }

  public void setType(PlayerType type) {
    this.type = type;
  }
}
