package server.business;

import dto.ClientInfo;
import dto.DTOCard;
import dto.message.*;
import game.GameCardStack;
import game.GameProcess;
import rmi.RMIService;
import server.business.rmiImpl.*;
import utilities.Converter;
import utilities.constants.GameCardConstants;
import utilities.constants.GameConfigurationConstants;
import utilities.constants.PlayerConstants;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
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
  private Registry registry;

  private List<ClientInfo> clients;
  private Integer port;
  private Boolean running;
  private String password;

  /* Constructors */
  public static GameServer getServerInstance() {
    if(gameServer == null) {
      gameServer = new GameServer();
    }
    return gameServer;
  }

  public static GameServer getServerInstance(Integer port, String password) {
    if(gameServer == null) {
      gameServer = new GameServer();
    }
    gameServer.setPort(port);
    gameServer.setPassword(password);

    return gameServer;
  }

  private GameServer() {
    this.port = Registry.REGISTRY_PORT;
    this.services = new HashMap<RMIService,Remote>();
    this.clients = new ArrayList<ClientInfo>();
    this.running = false;
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

  private void initImplementations() {
    Object implementation = services.get(RMIService.AUTHENTICATION);
    if(implementation != null) {
      AuthenticatorImpl impl = (AuthenticatorImpl) implementation;
      impl.setPassword(password);
    }
  }

  private void unregisterServices(Registry registry) throws RemoteException, NotBoundException {
    for (RMIService rmiService : services.keySet()) {
      registry.unbind(rmiService.getServiceName());
      UnicastRemoteObject.unexportObject(services.get(rmiService),true);
    }
  }

  public void startServer() throws InstantiationException, IllegalAccessException, RemoteException {
    if(!isServerRunning()) {
      registry = getSafeRegistry(port);
      initServices(registry);
      initImplementations();
      running = true;
    }
  }

  private Registry getSafeRegistry(Integer port) throws RemoteException {
    Registry registry;

    try {
      registry = LocateRegistry.createRegistry(port);
    } catch (RemoteException e) {
      LOGGER.info("Port "+ port + " already used, getting this registry.");
      registry = LocateRegistry.getRegistry(port);
    }

    return registry;
  }

  public void shutdownServer() throws NotBoundException, RemoteException {
    if(isServerRunning()) {
      try {
        getRMIObservable().notifyObservers(new MessageObject(BroadcastType.SERVER_SHUTDOWN));
      } catch (ServerNotActiveException e) {
        LOGGER.warning("Server is not active: "+e.getMessage());
      }
      unregisterServices(registry);
      running = false;
    }
  }

  public void startGame(Integer stackSize) {
    new GameInitialiser(stackSize, clients).invoke();
  }

  void setChangedAndNotify(Enum<?> type) {
    setChangedAndNotify(type, null);
  }

  void setChangedAndNotify(Enum<?> type, Object sendingObject) {
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

  public void sendProcessUpdate() {
    final GameProcess process = GameProcess.getInstance();
    final List<List<DTOCard>> allCards =
        Converter.toDTO(process.getAttackCards(), process.getDefenseCards());
    broadcastMessage(GameUpdateType.INGAME_CARDS, allCards);
  }

  public void broadcastMessage(Enum<?> type) {
    broadcastMessage(type, null);
  }

  public void sendMessage(ClientInfo client, MessageObject messageObject) {
    int index = getClientIndex(client);
    if(index != -1)
      getRMIObservable().notifyObserver(index, messageObject);
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

  public Boolean clientExists(ClientInfo client) {
    for (ClientInfo clientInfo : clients) {
      if(client.isEqual(clientInfo))
        return true;
    }

    return false;
  }

  private int getClientIndex(ClientInfo client) {
    for (int index = 0; index < clients.size(); index++) {
      if(client.isEqual(clients.get(index)))
        return index;
    }

    return -1;
  }

  public ClientInfo getRMIReference(ClientInfo info) {
    for (ClientInfo client : clients) {
      if(client.isEqual(info))
        return client;
    }
    return null;
  }

  /**
   * Adds a client to the server and returns this client.
   * @param client ClientInfo object to add to the server
   * @return Returns the added ClientInfo object or null, if the object wasn't
   * added.
   */
  public ClientInfo addClient(ClientInfo client) {
    final Integer nextNumber = clients.size();
    if(client.getLoginNumber().equals(GameConfigurationConstants.NO_LOGIN_NUMBER) ||
       nextNumber.shortValue() < client.getLoginNumber()) {
      client.setLoginNumber(nextNumber.shortValue());
      clients.add(client);
      GameProcess.getInstance().addPlayer();
      setChangedAndNotify(GUIObserverType.ADD_CLIENT, client);
      sendMessage(client,new MessageObject(MessageType.LOGIN_NUMBER, client));
      broadcastMessage(BroadcastType.LOGIN_LIST, clients);
    }

    return null;
  }

  public void removeClient(ClientInfo client) {
    final ClientInfo reference = getRMIReference(client);
    if(clients.remove(reference)) {
      GameProcess.getInstance().removePlayer(reference.getLoginNumber());
      refreshClients();
      setChangedAndNotify(GUIObserverType.REMOVE_CLIENT, client);
      broadcastMessage(BroadcastType.LOGIN_LIST, clients);
    }
  }

  private void refreshClients() {
    for (int index = 0; index < clients.size(); index++) {
      final ClientInfo client = clients.get(index);
      if(client.getLoginNumber() != index) {
        client.setLoginNumber((short) index);
        sendMessage(client, new MessageObject(MessageType.LOGIN_NUMBER, client));
      }
    }
  }

  /* Getter and Setter */
  public List<ClientInfo> getClients() {
    return clients;
  }

  private RMIObservableImpl getRMIObservable() {
    return (RMIObservableImpl) services.get(RMIService.OBSERVER);
  }

  public void setPort(int port) {
    if(!isServerRunning())
      this.port = port;
  }

  public Integer getPort() {
    return port;
  }

  public Boolean isServerRunning() {
    return running;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  /* Inner classes */
  private class GameInitialiser {
    private Integer stackSize;
    private GameProcess process;
    private GameServer server;
    private List<ClientInfo> clients;

    public GameInitialiser(Integer stackSize, List<ClientInfo> clients) {
      this.stackSize = stackSize;
      this.clients = clients;
      this.process = GameProcess.getInstance();
      this.server = GameServer.getServerInstance();
    }

    public void invoke() {
      GameProcess process = GameProcess.getInstance();
      Integer cardsPerColour = stackSize / GameCardConstants.CardColour.values().length;
      process.initialiseNewGame(cardsPerColour);
      sendClientInit();
    }

    private void sendClientInit() {
      try {
        initialiseClients();
        server.broadcastMessage(GUIObserverType.INITIALISE_STACK, Converter.toDTO(GameCardStack.getInstance()));
        server.broadcastArray(GUIObserverType.INITIALISE_CARDS, process.getPlayerCards());
        server.broadcastMessage(GUIObserverType.INITIALISE_PLAYERS, clients);
      } catch (GameServerException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
      }
    }

    private void initialiseClients() {
      List<PlayerConstants.PlayerType> types = process.getPlayerTypes();
      for (int index = 0; index < clients.size(); index++) {
        clients.get(index).setCardCount(GameConfigurationConstants.INITIAL_CARD_COUNT);
        clients.get(index).setPlayerType(types.get(index));
      }
    }
  }
}