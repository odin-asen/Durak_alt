package server.business.rmiImpl;

import common.rmi.RMIObservable;
import common.rmi.RMIObserver;
import common.utilities.LoggingUtility;

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
  private static final Logger LOGGER = LoggingUtility.getLogger(RMIObservableImpl.class.getName());

  private Vector<RMIObserver> observers;

  /* Constructors */

  public RMIObservableImpl() {
    observers = new Vector<RMIObserver>();
  }

  /* Methods */
  public void registerInterest(RMIObserver observer)
      throws RemoteException {
    observers.addElement(observer);
  }

  public void notifyObservers(Object parameter)
      throws RemoteException {
    for (RMIObserver observer : observers)
      try {
        notifyObserver(observer, parameter);
      } catch (RemoteException e) {
        LOGGER.warning(e.getMessage());
      }
  }

  public void removeAllSubscribers() throws RemoteException {
    observers.removeAllElements();
  }

  public void removeObserver(RMIObserver observer) {
    observers.remove(observer);
  }

  public void notifyObserver(RMIObserver observer, Object parameter)
      throws RemoteException {
    try {
      observer.update(parameter);
    } catch (ServerNotActiveException e) {
      LOGGER.info("Observer " + observer + " is not active and will be removed!");
      removeObserver(observer);
    }
  }

  /* Getter and Setter */
  public Vector<RMIObserver> getObservers() {
    return observers;
  }
}