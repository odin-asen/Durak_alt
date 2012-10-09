package server.business;

import dto.DTOCard;

import java.util.ArrayList;
import java.util.List;

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

  private List<DTOCard> cards;

  /* Constructors */
  public Player() {
    cards = new ArrayList<DTOCard>(6);
    leftPlayer = null;
    rightPlayer = null;
    isDefending = false;
    isAttacking = false;
  }


  /* Methods */
  public void pickUpCard(DTOCard newCard) {
    cards.add(newCard);
  }

  public void useCard(DTOCard card) {
    if(cards.contains(card))
      cards.remove(card);
  }


  /* Getter and Setter */
  public Boolean getDefending() {
    return isDefending;
  }

  public void setDefending(Boolean defending) {
    isDefending = defending;
    if(defending)
      setAttacking(false);
  }

  public Boolean getAttacking() {
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
}
