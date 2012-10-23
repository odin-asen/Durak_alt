package server.business.rmiImpl;

import dto.ClientInfo;
import rmi.Authenticator;

import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 23:22
 */
public class AuthenticatorImpl implements Authenticator {
  /* Constructors */
  /* Methods */
  public boolean login(ClientInfo client, String password) throws RemoteException {
    System.out.println(client);
    if(password.equals(""))
      return true;
    else return false;
  }

  public void logoff(ClientInfo client) throws RemoteException {

  }

  /* Getter and Setter */
}
