package game;

import dto.DTOCard;

import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 17:20
 *
 * This class implies the rules of the game and provides methods that clarify,
 * if a move can be done or not.
 */
public abstract class RuleChecker {
  private Short trumpColour;

  public RuleChecker() {

  }

  public Short getTrumpColour() {
    return trumpColour;
  }

  public void setTrumpColour(Short trumpColour) {
    this.trumpColour = trumpColour;
  }

  /**
   * Returns true or false whether the move can be done or not.
   * @param attackerCard Card to attack with.
   * @param currentCards Cards already on the table.
   * @return True, if move can be done, false, if not.
   */
  public boolean canDoAttackMove(DTOCard attackerCard, List<DTOCard> currentCards) {
    return false;
  }

  /**
   * Returns true or false whether the defense move can be done or not.
   * @param defenderCard Card to defend with.
   * @param attackerCard Card that will be defended.
   * @return True, if move can be done, false, if not.
   */
  public boolean canDoDefendMove(DTOCard defenderCard, DTOCard attackerCard) {
    return false;
  }

  /**
   * Returns the index of the player with the smallest trump on the hand.
   * @param playerHands Cards of each player.
   * @return Index of the player that begins.
   */
  public Integer getStartPlayer(List<List<DTOCard>> playerHands) {
    return 0;
  }
}
