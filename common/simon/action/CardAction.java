package common.simon.action;

import common.dto.DTOCard;
import common.dto.DTOClient;
import common.utilities.Miscellaneous;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.01.13
 * Time: 01:31
 *
 * This class is immutable if GameAction is also immutable.
 */
public final class CardAction extends GameAction implements Serializable {
  public enum CardActionType {ATTACK, DEFENSE}

  private List<DTOCard> attackCards;
  private List<DTOCard> defenderCards;
  private CardActionType cardActionType;

  /* Constructors */
  public CardAction(CardActionType cardActionType, List<DTOCard> attackCards, List<DTOCard> defenderCards,
                    DTOClient executor, GameAction.ActionType type) {
    super(executor, type);
    this.cardActionType = cardActionType;
    this.attackCards = new ArrayList<DTOCard>(attackCards.size());
    this.defenderCards = new ArrayList<DTOCard>(defenderCards.size());
    Collections.copy(this.attackCards, attackCards);
    Collections.copy(this.defenderCards, defenderCards);
  }

  /* Methods */
  /* Getter and Setter */

  public List<DTOCard> getAttackCards() {
    final List<DTOCard> other = new ArrayList<DTOCard>(attackCards.size());
    Collections.copy(other,attackCards);
    return other;
  }

  public List<DTOCard> getDefenderCards() {
    final List<DTOCard> other = new ArrayList<DTOCard>(defenderCards.size());
    Collections.copy(other,defenderCards);
    return other;
  }

  public CardActionType getCardActionType() {
    return cardActionType;
  }
}
