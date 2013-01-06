package common.rmi;

import common.dto.DTOClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 22:59
 */
public interface ChatHandler extends Remote {
  public void sendMessage(DTOClient client, String message) throws RemoteException;
}
