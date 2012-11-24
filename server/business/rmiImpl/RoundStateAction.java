package server.business.rmiImpl;

import common.dto.ClientInfo;
import common.dto.DTOCard;
import common.game.GameProcess;
import common.rmi.FinishAction;
import common.rmi.GameAction;
import server.business.GameServer;

import java.rmi.RemoteException;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 10.11.12
 * Time: 23:52
 */
public class RoundStateAction implements GameAction {
  private ClientInfo executor;

  /* Constructors */
  /* Methods */
  public boolean doAction(ClientInfo client, FinishAction finish, DTOCard... cards) throws RemoteException {
    Boolean goToNextRound = false;
    executor = client;

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

  public List<List<DTOCard>> getCardLists() {
    return null;
  }

  public ClientInfo getExecutor() {
    return executor;
  }
}
