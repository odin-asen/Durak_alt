package rmi;

import dto.ClientInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 23:17
 */
public interface Authenticator extends Remote {
  /**
   * Logs the specified client to the server. A password may be required.
   * @param client Specified client.
   * @param password Server password.
   * @return Returns true, if the client is logged in, else false.
   * @throws RemoteException
   */
  public boolean login(ClientInfo client, String password) throws RemoteException;

  /**
   * Logs the specified client off the server.
   * @param client Specified client.
   * @throws RemoteException
   */
  public void logoff(ClientInfo client) throws RemoteException;
}
