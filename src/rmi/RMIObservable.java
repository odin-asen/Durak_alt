package rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;

/**
 * User: Timm Herrmann
 * Date: 22.10.12
 * Time: 16:58
 */
public interface RMIObservable extends Remote {
  public void registerInterest(RMIObserver client)
      throws RemoteException, ServerNotActiveException;
  public void notifyObservers(Serializable notification)
      throws RemoteException, ServerNotActiveException;
}
