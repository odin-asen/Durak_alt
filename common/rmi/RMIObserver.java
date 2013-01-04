package common.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;

/**
 * User: Timm Herrmann
 * Date: 22.10.12
 * Time: 16:54
 */
public interface RMIObserver extends Remote {
  /**
   * This method should be used by the server to notify a client.
   * @param parameter Notification for the client.
   * @throws RemoteException
   * @throws ServerNotActiveException
   */
  public void update(Object parameter)
      throws RemoteException, ServerNotActiveException;
}
