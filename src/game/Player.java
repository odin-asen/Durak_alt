package game;

import utilities.constants.GameConfigurationConstants;

import java.util.ArrayList;
import java.util.List;

import static utilities.constants.GameCardConstants.CardColour;

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
  private Boolean isDefending;
  private Boolean isAttacking;

  private String name;

  private List<GameCard> cards;

  /* Constructors */
  public Player(String name) {
    cards = new ArrayList<GameCard>(GameConfigurationConstants.INITIAL_CARD_COUNT);
    this.name = name;
    leftPlayer = null;
    rightPlayer = null;
    isDefending = false;
    isAttacking = false;
  }


  /* Methods */
  public void pickUpCard(GameCard newCard) {
    cards.add(newCard);
  }

  public void useCard(GameCard card) {
    if(cards.contains(card))
      cards.remove(card);
  }

  /**
   * Idle means not attacking and not defending.
   * @return True, if in idle, false, if either attacking or defending.
   */
  public boolean isIdle() {
    return !isDefending() && !isAttacking();
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
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean isDefending() {
    return isDefending;
  }

  public void setDefending(Boolean defending) {
    isDefending = defending;
    if(defending)
      setAttacking(false);
  }

  public Boolean isAttacking() {
    return isAttacking;
  }

  public void setAttacking(Boolean attacking) {
    isAttacking = attacking;
    if(attacking)
      setDefending(false);
  }

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
}
