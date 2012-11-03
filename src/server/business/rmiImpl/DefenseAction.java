package server.business.rmiImpl;

import dto.ClientInfo;
import dto.DTOCard;
import game.GameProcess;
import game.rules.RuleException;
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
  public boolean doAction(ClientInfo client, DTOCard ...cards) throws RemoteException {
    this.executor = client;
    Boolean actionDone = false;

    if(setCards(cards)) {
      try {
        GameProcess.getInstance().validateAction(this);
        actionDone = true;
        reason = "";
        GameServer.getServerInstance().sendProcessUpdate();
      } catch (RuleException e) {
        reason = e.getMessage();
      }
    } else reason = "Wrong format of the cards. Please debug!";

    return actionDone;
  }

  public String getRefusedReason() {
    return reason;
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