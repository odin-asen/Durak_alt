package client.business.client;

import dto.message.MessageObject;
import rmi.RMIObservable;
import rmi.RMIObserver;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 22.10.12
 * Time: 23:41
 */
public class ServerMessageHandler extends UnicastRemoteObject implements RMIObserver {
  private static Logger LOGGER = Logger.getLogger(ServerMessageHandler.class.getName());

  private RMIObservable server;

  public ServerMessageHandler(Registry serverRegistry, String service) throws RemoteException {
    try {
      this.server = (RMIObservable) serverRegistry.lookup(service);
    }
    catch (Exception exception) {
      LOGGER.log(Level.SEVERE, "Unable to connect and register with server!");
    }
  }

  public RMIObservable getServer() {
    return server;
  }

  public void incomingMessage(final Serializable notificationObject) throws RemoteException {
    if(notificationObject instanceof MessageObject) {
      GameClient.getClient().receiveServerMessage((MessageObject) notificationObject);
    }
  }
}
