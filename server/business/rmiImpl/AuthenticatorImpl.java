package server.business.rmiImpl;

import common.dto.DTOClient;
import common.i18n.I18nSupport;
import common.rmi.Authenticator;
import server.business.GameServer;
import server.business.GameServerException;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 23:22
 */
public class AuthenticatorImpl implements Authenticator  {
  private static final String BUNDLE_NAME = "user.messages"; //NON-NLS
  private String password;
  private String reason;

  /* Constructors */
  public AuthenticatorImpl() {
    this("");
  }

  public AuthenticatorImpl(String password) {
    this.password = password;
    reason = "";
  }

  /* Methods */
  public boolean login(DTOClient client, String password)
      throws RemoteException {
    if(this.password.equals(password)) {
      final GameServer server = GameServer.getServerInstance();
      try {
        server.addClient(client);
      } catch (NotBoundException e) {
        reason = I18nSupport.getValue(BUNDLE_NAME,"client.service.not.running");
        return false;
      } catch (GameServerException e) {
        reason = e.getMessage();
        return false;
      }
    } else {
      reason = I18nSupport.getValue(BUNDLE_NAME,"status.permission.denied");
      return false;
    }

    return true;
  }

  public void logoff(DTOClient client) throws RemoteException {
    final GameServer server = GameServer.getServerInstance();
    server.removeClient(client);
  }

  public String getRefusedReason() throws RemoteException {
    return reason;
  }

  /* Getter and Setter */

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
