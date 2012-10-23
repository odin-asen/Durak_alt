package server.business.rmiImpl;

import dto.DTOCard;
import rmi.GameAction;

import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:16
 */
public class DefenseAction implements GameAction {
  /* Constructors */
  /* Methods */
  public boolean doAction(DTOCard card, DTOCard... otherCards) throws RemoteException {
    System.out.println("defense");
    return false;
  }

  /* Getter and Setter */
}
