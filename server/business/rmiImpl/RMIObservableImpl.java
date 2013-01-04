package server.business.rmiImpl;

import common.rmi.RMIObservable;
import common.rmi.RMIObserver;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 22.10.12
 * Time: 17:02
 */
public class RMIObservableImpl implements RMIObservable {
  private static final Logger LOGGER = Logger.getLogger(RMIObservableImpl.class.getName());

  private Vector<RMIObserver> clients;

  /* Constructors */

  public RMIObservableImpl() {
    clients = new Vector<RMIObserver>();
  }

  /* Methods */
  public void registerInterest(RMIObserver observer)
      throws RemoteException {
    clients.addElement(observer);
  }

  public void notifyObservers(Object parameter)
      throws RemoteException {
    for (RMIObserver client : clients)
      notifyObserver(client, parameter);
  }

  public void removeAllSubscribers() throws RemoteException {
    clients.removeAllElements();
  }

  public void removeObserver(RMIObserver observer) {
    clients.remove(observer);
  }

  public void notifyObserver(RMIObserver observer, Object parameter)
      throws RemoteException {
    try {
      observer.update(parameter);
    } catch (ServerNotActiveException e) {
      LOGGER.severe("Client " + observer + " is not active and will be removed!");
      removeObserver(observer);
    }
  }

  /* Getter and Setter */
  public Vector<RMIObserver> getClients() {
    return clients;
  }
}