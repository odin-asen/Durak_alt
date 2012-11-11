package server.business.rmiImpl;

import dto.ClientInfo;
import dto.DTOCard;
import game.GameProcess;
import game.rules.RuleException;
import rmi.FinishAction;
import rmi.GameAction;
import server.business.GameServer;

import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:16
 */
public class DefenseAction implements GameAction {
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
        GameProcess.getInstance().validateAction(this);
        actionDone = true;
        reason = "";
        GameServer.getServerInstance().sendProcessUpdate(false);
      } catch (RuleException e) {
        reason = e.getMessage();
      }
    } else reason = "Wrong format of the cards. " +
        "First card is the defense card, the second the attacker card." +
        "\nPlease debug!";

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

  public ClientInfo getExecutor() {
    return executor;
  }
}
