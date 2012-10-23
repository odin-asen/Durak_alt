package client.business.client;

import dto.message.MessageObject;
import rmi.Authenticator;
import rmi.ChatHandler;
import rmi.GameAction;
import rmi.RMIService;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 01:37
 */
public class GameClient extends Observable {
  private static Logger LOGGER = Logger.getLogger(GameClient.class.getName());

  private static GameClient client;

  public static final String DEFAULT_SERVER_ADDRESS = "localhost";
  public static final Integer DEFAULT_SERVER_PORT = Registry.REGISTRY_PORT;

  private Map<RMIService,Remote> services;
  private Registry registry;

  private String serverAddress;
  private Integer port;
  private Boolean connected;

  /* Constructors */
  public static GameClient getClient() {
    if (client == null) {
      client = new GameClient();
    }
    return client;
  }

  public static GameClient getClient(String serverAddress, Integer port) {
    if (client == null) {
      client = new GameClient();
    }
    client.setPort(port);
    client.setServerAddress(serverAddress);

    return client;
  }

  private GameClient() {
    this.serverAddress = DEFAULT_SERVER_ADDRESS;
    this.port = DEFAULT_SERVER_PORT;
    this.connected = false;
    services = new HashMap<RMIService,Remote>();
  }

  /* Methods */
  private void lookupServices(Registry registry, RMIService service)
      throws RemoteException, NotBoundException {
    final Remote remote = registry.lookup(service.getServiceName());
    services.put(service, remote);
  }

  private void initServices(Registry registry) throws RemoteException, NotBoundException {
    lookupServices(registry, RMIService.ATTACK_ACTION);
    lookupServices(registry, RMIService.AUTHENTICATION);
    lookupServices(registry, RMIService.CHAT);
    lookupServices(registry, RMIService.DEFENSE_ACTION);
    lookupServices(registry, RMIService.OBSERVER);
  }

  private void unregisterServices(Registry registry) throws RemoteException, NotBoundException {
    for (RMIService rmiService : services.keySet()) {
      registry.unbind(rmiService.getServiceName());
    }
  }

  public void sendServerMessage(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  public void setChangedAndNotify(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  public void connect()
      throws RemoteException, NotBoundException, ServerNotActiveException {
    if (!isConnected()) {
      registry = LocateRegistry.getRegistry(serverAddress, port);
      initServices(registry);
      getRMIObservable().getServer().registerInterest(getRMIObservable());
      connected = true;
    }
  }

  public void disconnect() throws NotBoundException, RemoteException {
    if(isConnected()) {
      unregisterServices(registry);
      connected = false;
    }
  }

  private ServerMessageHandler getRMIObservable() {
    return (ServerMessageHandler) services.get(RMIService.OBSERVER);
  }

  /**
   * Sends a message to the server and returns the answer of it.
   *
   * @param message Message to send.
   * @return A MessageObject as answer of the server.
   */
  public MessageObject send(MessageObject message) {
    return null; //TODO senden implementieren mit RMI Objekten
  }

  /* Getter and Setter */
  public String getSocketAddress() {
    return serverAddress + ":" + port;
  }

  public void setServerAddress(String serverAddress) {
    if(!isConnected())
      this.serverAddress = serverAddress;
  }

  public void setPort(Integer port) {
    if(!isConnected())
      this.port = port;
  }

  public Boolean isConnected() {
    return connected;
  }

  public Authenticator getAuthenticator() {
    return (Authenticator) services.get(RMIService.AUTHENTICATION);
  }

  public ChatHandler getChatHandler() {
    return (ChatHandler) services.get(RMIService.CHAT);
  }

  public GameAction getGameActionAttack() {
    return (GameAction) services.get(RMIService.ATTACK_ACTION);
  }

  public GameAction getGameActionDefend() {
    return (GameAction) services.get(RMIService.DEFENSE_ACTION);
  }
}