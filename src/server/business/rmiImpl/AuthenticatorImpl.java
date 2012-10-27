package server.business.rmiImpl;

import dto.ClientInfo;
import rmi.Authenticator;
import server.business.GameServer;

import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 23:22
 */
public class AuthenticatorImpl implements Authenticator {
  private String password;

  /* Constructors */
  public AuthenticatorImpl() {
    password = "";
  }

  /* Methods */
  public boolean login(ClientInfo client, String password) throws RemoteException {
    if(password.equals(password)) {
      GameServer.getServerInstance().addClient(client);
      return true;
    }
    else return false;
  }

  public void logoff(ClientInfo client) throws RemoteException {
    GameServer.getServerInstance().removeClient(client);
  }

  /* Getter and Setter */
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
