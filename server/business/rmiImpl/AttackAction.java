package server.business.rmiImpl;

import common.dto.DTOClient;
import common.dto.DTOCard;
import common.game.GameProcess;
import common.game.rules.RuleException;
import common.rmi.FinishAction;
import common.rmi.GameAction;
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
  private String reason;

  /* Constructors */
  /* Methods */
  public boolean doAction(DTOClient client, FinishAction finish, DTOCard ...cards) throws RemoteException {
    final List<List<DTOCard>> cardLists = new ArrayList<List<DTOCard>>(1);
    Boolean actionDone = false;
    final GameProcess process = GameProcess.getInstance();

    /* convert array to list list */
    cardLists.add(new ArrayList<DTOCard>());
    Collections.addAll(cardLists.get(0), cards);

    try {
      process.validateAction(GameProcess.ValidationAction.ATTACK,
          client.toString(), cardLists);
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
}
