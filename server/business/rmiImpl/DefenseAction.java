package server.business.rmiImpl;

import dto.ClientInfo;
import dto.DTOCard;
import game.GameProcess;
import game.rules.RuleException;
import rmi.FinishAction;
import rmi.GameAction;
import server.business.GameServer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:16
 */
public class DefenseAction implements GameAction {
  private static final String DEBUG_MESSAGE_WRONG_FORMAT = "Wrong format of the cards. " + //NON-NLS
      "First card is the defense card, the second the attacker card." + //NON-NLS
      "\nPlease debug!"; //NON-NLS
  private ClientInfo executor;
  private DTOCard defenseCard;
  private DTOCard attackCard;
  private String reason;

  /* Constructors */
  /* Methods */
  public boolean doAction(ClientInfo client, FinishAction finish, DTOCard ...cards) throws RemoteException {
    this.executor = client;
    Boolean actionDone = false;

    if(setCards(cards)) {
      try {
        GameProcess.getInstance().validateAction(GameProcess.ValidationAction.DEFENSE, this);
        actionDone = true;
        reason = "";
        GameServer.getServerInstance().sendProcessUpdate(false);
      } catch (RuleException e) {
        reason = e.getMessage();
      }
    } else reason = DEBUG_MESSAGE_WRONG_FORMAT;

    return actionDone;
  }

  public String getRefusedReason() {
    final String lastText = reason;
    reason = null;
    return lastText;
  }

  private boolean setCards(DTOCard[] cards) {
    if(cards.length >= 2) {
      defenseCard = cards[0];
      attackCard = cards[1];
    } else return false;

    return true;
  }

  /* Getter and Setter */
  public DTOCard getDefendCard() {
    return defenseCard;
  }

  public DTOCard getAttackCard() {
    return attackCard;
  }

  /**
   * Returns two lists. The first list contains the attacker cards.
   * The second list contains the defender cards.
   * @return Returns a list of lists with the cards related to this action.
   */
  public List<List<DTOCard>> getCardLists() {
    final List<List<DTOCard>> cardLists = new ArrayList<List<DTOCard>>();
    final List<DTOCard> attackCards = new ArrayList<DTOCard>();
    final List<DTOCard> defenseCards = new ArrayList<DTOCard>();
    attackCards.add(attackCard);
    defenseCards.add(defenseCard);
    cardLists.add(attackCards);
    cardLists.add(defenseCards);
    return cardLists;
  }
  public ClientInfo getExecutor() {
    return executor;
  }
}
