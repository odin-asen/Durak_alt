package rmi;

import dto.DTOCard;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
   * Executes the cards with the specified cards.
   * @param card Primary card, e.g. an attack needs just one card.
   * @param otherCards Other cards that are used for the action.
   * @return True, action is done, false action could not be done.
   */
  public boolean doAction(DTOCard card, DTOCard ...otherCards) throws RemoteException;
}
