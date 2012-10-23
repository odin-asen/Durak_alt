package server.business.rmiImpl;

import rmi.RMIObservable;
import rmi.RMIObserver;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 22.10.12
 * Time: 17:02
 */
public class RMIObservableImpl implements RMIObservable {
  private static final Logger LOGGER = Logger.getLogger(RMIObservableImpl.class.getName());

  private Vector<RMIObserver> observers;

  /* Constructors */
  public RMIObservableImpl() {
    observers = new Vector<RMIObserver>();
  }

  /* Methods */
  public void registerInterest(RMIObserver observer)
      throws RemoteException, ServerNotActiveException {
    observers.add(observer);
  }

  public void notifyObservers(Serializable notification)
      throws RemoteException, ServerNotActiveException {
    for (int index = 0; index < observers.size(); index++) {
      notifyObserver(index, notification);
    }
  }

  /* Getter and Setter */
  public Vector<RMIObserver> getObservers() {
    return observers;
  }

  public void notifyObserver(Integer observerIndex, Serializable notification) {
    try {
      if(observerIndex < observers.size())
        observers.get(observerIndex).incomingMessage(notification);
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Client " + observers.get(observerIndex)
          + " could not been notified: " + e.getMessage());
    }
  }
}
