package common.rmi;

import common.dto.DTOClient;

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
  public boolean login(DTOClient client, String password) throws RemoteException;

  /**
   * Logs the specified client off the server.
   * @param client Specified client.
   * @throws RemoteException
   */
  public void logoff(DTOClient client) throws RemoteException;

  /**
   * This method returns a reason why the login returned false. If login
   * returns true, this method should return an empty String.
   * @return Returns a reason for refusing the login.
   */
  public String getRefusedReason() throws RemoteException;
}
