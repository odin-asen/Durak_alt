package server.business.rmiImpl;

import dto.DTOCard;
import rmi.GameAction;

import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:16
 */
public class AttackAction implements GameAction {
  /* Constructors */
  /* Methods */
  public boolean doAction(DTOCard card, DTOCard... otherCards) throws RemoteException {
    System.out.println("attack");
    return false;
  }

  /* Getter and Setter */
}
