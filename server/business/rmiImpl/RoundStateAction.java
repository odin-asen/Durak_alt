package server.business.rmiImpl;

import dto.ClientInfo;
import dto.DTOCard;
import game.GameProcess;
import rmi.FinishAction;
import rmi.GameAction;
import server.business.GameServer;

import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 10.11.12
 * Time: 23:52
 */
public class RoundStateAction implements GameAction {
  /* Constructors */
  /* Methods */
  public boolean doAction(ClientInfo client, FinishAction finish, DTOCard... cards) throws RemoteException {
    Boolean goToNextRound = false;

    if(!finish.equals(FinishAction.NOT_FINISHING)) {
      if(doFinishing(client, finish)) {
        goToNextRound = true;
      }
      GameServer.getServerInstance().sendProcessUpdate(goToNextRound);
    }

    return goToNextRound;
  }

  private Boolean doFinishing(ClientInfo client, FinishAction finish) {
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
