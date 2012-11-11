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
import java.util.Collections;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:16
 */
public class AttackAction implements GameAction {
  private ClientInfo executor;
  private List<DTOCard> cards;
  private String reason;

  /* Constructors */
  /* Methods */
  public boolean doAction(ClientInfo client, FinishAction finish, DTOCard ...cards) throws RemoteException {
    this.cards = new ArrayList<DTOCard>();
    this.executor = client;
    Collections.addAll(this.cards, cards);
    Boolean actionDone = false;
    final GameProcess process = GameProcess.getInstance();

    try {
      process.validateAction(this);
      actionDone = true;
      reason = "";
      GameServer.getServerInstance().sendProcessUpdate(false);
    } catch (RuleException e) {
      reason = e.getMessage();
    }

    return actionDone;
  }

  /* Getter and Setter */
  public String getRefusedReason() {
    final String lastText = reason;
    reason = null;
    return lastText;
  }

  public List<DTOCard> getCards() {
    return cards;
  }

  public ClientInfo getExecutor() {
    return executor;
  }
}
