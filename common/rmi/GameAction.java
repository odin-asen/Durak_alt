package common.rmi;

import common.dto.ClientInfo;
import common.dto.DTOCard;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 22:56
 *
 * This interface implies an action that can be made within a game.
 * An action can be something like attack with a card or defense or etc.
 */
public interface GameAction extends Remote {
  /**
   * Executes the action with the specified cards.
   * @param cards Primary card, e.g. an attack needs just one card.
   * @return True, action is done, false action was refused.
   */
  public boolean doAction(ClientInfo client, FinishAction finish, DTOCard ...cards) throws RemoteException;

  /**
   * This method returns a reason why the doAction returned false. If doAction
   * returns true, this method should return an empty String.
   * @return Returns a reason for the refusing of the action. If the reason was
   * already picked and the method will be called a second time before using
   * doAction, then null will be returned.
   */
  public String getRefusedReason() throws RemoteException;
}
