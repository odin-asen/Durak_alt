package server.business.rmiImpl;

import rmi.RMIObservable;
import rmi.RMIObserver;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.StringTokenizer;
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
  private String parseSocket(String objectString) {
    String socket = "";
    StringTokenizer tokenizer = new StringTokenizer(objectString, "[]");
    while(tokenizer.hasMoreElements()) {
      final StringTokenizer socketTokenizer = new StringTokenizer(tokenizer.nextToken(), ".:");
      if(socketTokenizer.countTokens() == 5) {
        for (int i = 0; i < 4; i++) {
          if(!socket.isEmpty())
            socket = socket+".";
          socket = socket + socketTokenizer.nextToken();
        }
        socket = socket + ":" + socketTokenizer.nextToken();
        return socket;
      }
    }

    return "";
  }

  public void registerInterest(RMIObserver observer)
      throws RemoteException, ServerNotActiveException {
    final String socket = parseSocket(observer.toString());
    boolean adding = true;
    for (RMIObserver rmiObserver : observers) {
      if(socket.equals(parseSocket(rmiObserver.toString())))
        adding = false;
    }
    if(adding) {
      System.out.println("Adding "+socket);
      observers.add(observer);
    }
  }

  public void notifyObservers(Serializable notification)
      throws RemoteException, ServerNotActiveException {
    for (int index = 0; index < observers.size(); index++) {
      notifyObserver(index, notification);
    }
  }

  public void removeObserver(RMIObserver observer) {
    observers.remove(observer);
  }

  public void notifyObserver(Integer observerIndex, Serializable notification) {
    try {
      if(observerIndex < observers.size())
        observers.get(observerIndex).incomingMessage(notification);
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Client " + observers.get(observerIndex)
          + " could not been notified: " + e.getMessage());
      removeObserver(observers.get(observerIndex));
    }
  }

  /* Getter and Setter */
  public Vector<RMIObserver> getObservers() {
    return observers;
  }
}
