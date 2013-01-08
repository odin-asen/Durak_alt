package common.simon.action;

import common.dto.DTOCard;
import common.dto.DTOClient;
import common.utilities.Converter;
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
  public CardAction(CardActionType cardActionType, List<DTOCard> attackCards,
                    List<DTOCard> defenderCards, DTOClient executor, GameAction.ActionType type) {
    super(executor, type);
    this.cardActionType = cardActionType;
    int size = attackCards != null ? attackCards.size() : 0;
    this.attackCards = new ArrayList<DTOCard>(size);
    size = defenderCards != null ? defenderCards.size() : 0;
    this.defenderCards = new ArrayList<DTOCard>(size);
    Miscellaneous.addAllToCollection(this.attackCards, attackCards);
    Miscellaneous.addAllToCollection(this.defenderCards, defenderCards);
  }

  /* Methods */

  @SuppressWarnings("HardCodedStringLiteral")
  public String toString() {
    return "CardAction{" +
        "executor=" + executor +
        ", actionType=" + type +
        ", attackCards=" + Converter.getCollectionString(attackCards) +
        ", defenderCards=" + Converter.getCollectionString(defenderCards) +
        ", cardActionType=" + cardActionType +
        '}';
  }

  /* Getter and Setter */

  public List<DTOCard> getAttackCards() {
    final List<DTOCard> other = new ArrayList<DTOCard>(attackCards.size());
    Miscellaneous.addAllToCollection(other,attackCards);
    return other;
  }

  public List<DTOCard> getDefenderCards() {
    final List<DTOCard> other = new ArrayList<DTOCard>(defenderCards.size());
    Miscellaneous.addAllToCollection(other,defenderCards);
    return other;
  }

  public CardActionType getCardActionType() {
    return cardActionType;
  }
}
