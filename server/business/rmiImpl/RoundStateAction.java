package server.business.rmiImpl;

import common.dto.DTOClient;
import common.dto.DTOCard;
import common.game.GameProcess;
import common.rmi.FinishAction;
import common.rmi.GameAction;
import server.business.GameServer;

import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 10.11.12
 * Time: 23:52
 */
public class RoundStateAction implements GameAction {
  /* Methods */

  public boolean doAction(DTOClient client, FinishAction finish, DTOCard... cards) throws RemoteException {
    Boolean goToNextRound = false;

    if(!finish.equals(FinishAction.NOT_FINISHING)) {
      if(doFinishing(client, finish)) {
        goToNextRound = true;
      }
      GameServer.getServerInstance().sendProcessUpdate(goToNextRound);
    }

    return goToNextRound;
  }

  private Boolean doFinishing(DTOClient client, FinishAction finish) {
    final Boolean goToNextRound;

    if(finish.equals(FinishAction.GO_TO_NEXT_ROUND)) {
      goToNextRound = GameProcess.getInstance().nextRound(client.playerType, false);
    } else if(finish.equals(FinishAction.TAKE_CARDS)) {
      goToNextRound = GameProcess.getInstance().nextRound(client.playerType, true);
    } else goToNextRound = false;

    return goToNextRound;
  }

  /* Getter and Setter */
  public String getRefusedReason() throws RemoteException {
    return "";
  }
}
