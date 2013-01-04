package server.business.rmiImpl;

import common.dto.ClientInfo;
import common.dto.DTOCard;
import common.game.GameProcess;
import common.game.rules.RuleException;
import common.rmi.FinishAction;
import common.rmi.GameAction;
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
  private String reason;

  /* Constructors */
  /* Methods */
  public boolean doAction(ClientInfo client, FinishAction finish, DTOCard ...cards) throws RemoteException {
    Boolean actionDone = false;
    final List<List<DTOCard>> cardLists = setCards(cards);

    if(cardLists != null) {
      try {
        GameProcess.getInstance().validateAction(GameProcess.ValidationAction.DEFENSE,
            client.toString(), cardLists);
        actionDone = true;
        reason = "";
        GameServer.getServerInstance().sendProcessUpdate(false);
      } catch (RuleException e) {
        reason = e.getMessage();
      }
    }

    return actionDone;
  }

  public String getRefusedReason() {
    final String lastText = reason;
    reason = null;
    return lastText;
  }

  private List<List<DTOCard>> setCards(DTOCard[] cards) {
    List<List<DTOCard>> cardLists = new ArrayList<List<DTOCard>>(2);

    if(cards.length >= 2) {
      cardLists.add(new ArrayList<DTOCard>());
      cardLists.add(new ArrayList<DTOCard>());
      cardLists.get(0).add(cards[0]);
      cardLists.get(1).add(cards[1]);
    } else {
      reason = DEBUG_MESSAGE_WRONG_FORMAT;
      return null;
    }

    return cardLists;
  }

  /* Getter and Setter */
}
