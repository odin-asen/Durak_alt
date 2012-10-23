package server.business;

import dto.message.GUIObserverType;
import dto.message.MessageObject;
import rmi.RMIService;
import server.business.exception.GameServerException;
import server.business.rmiImpl.*;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 21:57
 *
 *
 */
public class GameServer extends Observable {
  private static Logger LOGGER = Logger.getLogger(GameServer.class.getName());

  private static GameServer gameServer;
  public static final String SERVER_PING_ANSWER = "Hello! Is it me you're looking for?";

  private Map<RMIService,Remote> services;
  private Integer port;

  /* Constructors */
  public static GameServer getServerInstance() {
    if(gameServer == null) {
      gameServer = new GameServer();
    }
    return gameServer;
  }

  public static GameServer getServerInstance(Integer port) {
    if(gameServer == null) {
      gameServer = new GameServer();
    }
    gameServer.setPort(port);

    return gameServer;
  }

  private GameServer() {
    this.port = Registry.REGISTRY_PORT;
    services = new HashMap<RMIService,Remote>();
  }

  /* Methods */
  private void registerService(Registry registry, Class implementationClass, RMIService service)
      throws RemoteException, IllegalAccessException, InstantiationException {
    Object implementation = implementationClass.newInstance();
    if(implementation instanceof Remote) {
      Remote stub = UnicastRemoteObject.exportObject((Remote) implementation, 0);
      registry.rebind(service.getServiceName(), stub);

      services.put(service, (Remote) implementation);
    }
  }

  private void initServices(Registry registry) throws RemoteException, IllegalAccessException, InstantiationException {
    registerService(registry, AttackAction.class, RMIService.ATTACK_ACTION);
    registerService(registry, AuthenticatorImpl.class, RMIService.AUTHENTICATION);
    registerService(registry, ChatHandlerImpl.class, RMIService.CHAT);
    registerService(registry, DefenseAction.class, RMIService.DEFENSE_ACTION);
    registerService(registry, RMIObservableImpl.class, RMIService.OBSERVER);
  }

  private void unregisterServices(Registry registry) throws RemoteException, NotBoundException {
    for (RMIService rmiService : services.keySet()) {
      registry.unbind(rmiService.getServiceName());
    }
  }

  public void startServer() throws InstantiationException, IllegalAccessException, RemoteException {
    Registry registry = LocateRegistry.createRegistry(port);

    initServices(registry);
  }

  public void shutdownServer() throws NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(port);
    unregisterServices(registry);
  }

  void setChangedAndNotify(GUIObserverType type) {
    this.setChanged();
    this.notifyObservers(new MessageObject(type));
  }

  void setChangedAndNotify(GUIObserverType type, Object sendingObject) {
    this.setChanged();
    this.notifyObservers(new MessageObject(type, sendingObject));
  }

  public void broadcastMessage(Enum<?> type, Object sendingObject) {
    RMIObservableImpl observable = getRMIObservable();
    try {
      observable.notifyObservers(new MessageObject(type, sendingObject));
    } catch (RemoteException e) {
      LOGGER.severe(e.getMessage());
    } catch (ServerNotActiveException e) {
      LOGGER.severe(e.getMessage());
    }
  }

  public void broadcastMessage(Enum<?> type) {
    broadcastMessage(type, null);
  }

  private RMIObservableImpl getRMIObservable() {
    return (RMIObservableImpl) services.get(RMIService.OBSERVER);
  }

  /**
   * Sends a MessageObject object to all clients. For each client the MessageObject
   * object contains as sendingObject the corresponding object of the list
   * {@code sendingObjects}.
   * @param type Type of the message.
   * @param sendingObjects Sending objects for the clients.
   * @throws GameServerException The number of server threads and the size of
   * {@code sendingObjects} is not equal.
   */
  public void broadcastArray(Enum<?> type, List<?> sendingObjects)
      throws GameServerException {
    if(sendingObjects.size() != getRMIObservable().getObservers().size())
      throw new GameServerException("The number of server threads and sending objects is not equal!");
    for (int i = 0; i < sendingObjects.size(); i++) {
      getRMIObservable().notifyObserver(i, new MessageObject(type, sendingObjects.get(i)));
    }
  }

  public boolean isServerRunning() {
    return !services.isEmpty();
  }

  /* Getter and Setter */
  public void setPort(int port) {
    if(!isServerRunning())
      this.port = port;
  }

  public int getPort() {
    return port;
  }
}

//  static MessageObject getAnswer(MessageObject messageObject, ServerThread serverThread) throws GameServerException {
//    final Enum<?> type = messageObject.getType();
//    final MessageObject answer = new MessageObject(type);
//    Object sendingObject = null;
//    final GameServer gameServer = GameServer.getServerInstance();
//
//    if(MessageType.SERVER_PING.equals(type)) { /* Answer the ping request */
//      sendingObject = GameServer.SERVER_PING_ANSWER;
//    } else if(MessageType.LOGIN.equals(type)) {
//      /* Provide the user information of the server and its logged clients */
//      /* and add it to the list, update all other user */
//      sendingObject = loginUserToServer(messageObject, serverThread);
//      gameServer.broadcastMessage(LOGIN_LIST, gameServer.getClients());
//    } else if(MessageType.CHAT_MESSAGE.equals(type)) {
//      sendingObject = clientChatAnswer(serverThread.getClientInfo(),
//          messageObject.getSendingObject().toString());
//      gameServer.broadcastMessage(BroadcastType.CHAT_MESSAGE, sendingObject);
//    } else if(MessageType.GAME_ACTION.equals(type)) {
//      //TODO Kartenaktion prüfen und antwort senden, z.B. angriff nicht zulässig
//    } else {
//      throw new GameServerException("Unknown MessageType to handle: "+type);
//    }
//    answer.setSendingObject(sendingObject);
//    return answer;
//  }
//
//  private static Object clientChatAnswer(ClientInfo client, String message) {
//    List<Object> sendingObject = new ArrayList<Object>();
//    sendingObject.add(new Long(System.currentTimeMillis()));
//    sendingObject.add(client);
//    sendingObject.add(message);
//    return sendingObject;
//  }
//
//  /**
//   * Adds a client to the server.
//   * @return Returns information for the client like, e.g. which clients are
//   * also logged in.
//   */
//  private static Object loginUserToServer(MessageObject messageObject, ServerThread serverThread) {
//    final GameServer gameServer = GameServer.getServerInstance();
//    final List<Object> list = new ArrayList<Object>();
//
//    serverThread.setClientInfo((ClientInfo) messageObject.getSendingObject());
//    GameServer.getServerInstance().setChangedAndNotify(GUIObserverType.CLIENT_CONNECTED, messageObject.getSendingObject());
//    GameProcess.getInstance().addPlayer(new Player(
//        ((ClientInfo) messageObject.getSendingObject()).getClientName()));
//    list.add(Converter.toDTO(GameCardStack.getInstance()));
//    list.add(gameServer.getClients());
//
//    return list;
//  }
//}