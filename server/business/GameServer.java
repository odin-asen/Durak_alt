package server.business;

import common.dto.ClientInfo;
import common.dto.DTOCard;
import common.dto.message.*;
import common.game.GameCardStack;
import common.game.GameProcess;
import common.rmi.RMIObserver;
import common.rmi.RMIService;
import common.utilities.Converter;
import common.utilities.Miscellaneous;
import common.utilities.constants.GameCardConstants;
import common.utilities.constants.PlayerConstants;
import server.business.rmiImpl.*;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 21:57
 */
public class GameServer extends Observable {
  private static Logger LOGGER = Logger.getLogger(GameServer.class.getName());

  private static GameServer gameServer;

  private static final String RMI_SERVER_HOST = "java.rmi.server.hostname"; //NON-NLS

  private Map<RMIService,Remote> services;
  private Registry registry;

  private InGameSpectatorHolder<RMIObserver,ClientInfo> clientHolder;

  private Integer port;
  private Boolean running;
  private String password;

  private GameUpdater gameUpdater;

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
    port = Registry.REGISTRY_PORT;
    services = new HashMap<RMIService,Remote>();
    clientHolder = new InGameSpectatorHolder<RMIObserver,ClientInfo>();
    running = false;
  }

  /* Methods */
  private void registerService(Registry registry, String serverAddress,
                               Class implementationClass, RMIService service)
      throws RemoteException, IllegalAccessException, InstantiationException {
    final Object implementation = implementationClass.newInstance();
    if(implementation instanceof Remote) {
      Remote stub = UnicastRemoteObject.exportObject((Remote) implementation, 0);
      registry.rebind(service.getServiceName(serverAddress), stub);
      services.put(service, (Remote) implementation);
    }
  }

  private void initServices(Registry registry, String serverAddress)
      throws RemoteException, IllegalAccessException, InstantiationException {
    registerService(registry, serverAddress, AttackAction.class, RMIService.ATTACK_ACTION);
    registerService(registry, serverAddress, AuthenticatorImpl.class, RMIService.AUTHENTICATION);
    registerService(registry, serverAddress, ChatHandlerImpl.class, RMIService.CHAT);
    registerService(registry, serverAddress, DefenseAction.class, RMIService.DEFENSE_ACTION);
    registerService(registry, serverAddress, RoundStateAction.class, RMIService.ROUND_STATE_ACTION);
    registerService(registry, serverAddress, RMIObservableImpl.class, RMIService.OBSERVABLE);
  }

  private void initImplementations() {
    Object implementation = services.get(RMIService.AUTHENTICATION);
    if(implementation != null) {
      AuthenticatorImpl impl = (AuthenticatorImpl) implementation;
      impl.setPassword(password);
    }
  }

  private void unregisterServices(Registry registry, String serverAddress) throws RemoteException, NotBoundException {
    for (RMIService rmiService : services.keySet()) {
      registry.unbind(rmiService.getServiceName(serverAddress));
      UnicastRemoteObject.unexportObject(services.get(rmiService), true);
    }
    UnicastRemoteObject.unexportObject(registry,true);
  }

  public void startServer(String serverAddress) throws InstantiationException, IllegalAccessException, RemoteException {
    if(!isServerRunning()) {
      System.setProperty(RMI_SERVER_HOST, serverAddress);
      registry = getSafeRegistry(port);
      initServices(registry, serverAddress);
      initImplementations();
      running = true;
    }
  }

  private Registry getSafeRegistry(Integer port) throws RemoteException {
    Registry registry;

    try {
      registry = LocateRegistry.createRegistry(port);
    } catch (RemoteException e) {
      LOGGER.info("Port " + port + " already used, getting this registry.");
      registry = LocateRegistry.getRegistry(port);
    }

    return registry;
  }

  public void shutdownServer() throws NotBoundException, RemoteException {
    if(isServerRunning()) {
      getRMIObservable().notifyObservers(new MessageObject(BroadcastType.SERVER_SHUTDOWN));
      unregisterServices(registry, System.getProperty(RMI_SERVER_HOST, "127.0.0.1"));
      running = false;
    }
  }

  public void startGame(Integer stackSize) {
    gameUpdater = new GameUpdater(stackSize, clientHolder);
    gameUpdater.invoke();
  }

  /**
   * Calls {@link GameServer#setChangedAndNotify(Enum, Object)} with null
   * as second parameter.
   * @param type Type that defines the MessageObject.
   */
  void setChangedAndNotify(Enum<?> type) {
    setChangedAndNotify(type, null);
  }

  /**
   * Notifies all java.util.Observer objects that are added to the server.
   * It sends a MessageObject object as update parameter to all observers.
   * @param type Type that defines the MessageObject.
   * @param sendingObject Object that is corresponding to the type.
   */
  void setChangedAndNotify(Enum<?> type, Object sendingObject) {
    setChanged();
    notifyObservers(new MessageObject(type, sendingObject));
  }

  public void broadcastMessage(Enum<?> type, Object sendingObject) {
    try {
      getRMIObservable().notifyObservers(new MessageObject(type, sendingObject));
    } catch (RemoteException e) {
      LOGGER.severe("Could not notify observer: "+e.getMessage()+" occured!");
    }
  }

  public void sendProcessUpdate(boolean nextRound) {
    gameUpdater.updateMove(nextRound);
  }

  public void broadcastMessage(Enum<?> type) {
    broadcastMessage(type, null);
  }

  public void sendMessage(RMIObserver observer, MessageObject messageObject) {
    try {
      getRMIObservable().notifyObserver(observer, messageObject);
    } catch (RemoteException e) {
      LOGGER.severe("Could not notify observer: " + e.getMessage() + " occured!");
    }
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
  public void broadcastArray(Enum<?> type, Collection<RMIObserver> clients,
                             Collection<?> sendingObjects)
      throws GameServerException {
    if(sendingObjects.size() != clients.size())
      throw new GameServerException("The number of server threads and sending objects is not equal!");
    final Iterator<RMIObserver> observers = clients.iterator();
    final Iterator<?> objects = sendingObjects.iterator();
    while (observers.hasNext()) {
      try {
        getRMIObservable().notifyObserver(observers.next(),
          new MessageObject(type, objects.next()));
      } catch (RemoteException e) {
        LOGGER.severe("Could not notify observer: " + e.getMessage() + " occured!");
      }
    }
  }

  /**
   * Adds the client to the server.
   * @param client The client.
   * @throws NotBoundException If the client observer service is not bound
   * for the specified ip address and port.
   */
  public void addClient(ClientInfo client)
      throws NotBoundException, RemoteException, GameServerException {
    final Boolean inProcess = GameProcess.getInstance().isGameInProcess();
    if(Miscellaneous.containsClientAddress(clientHolder.getAllValues(),client))
      throw new GameServerException("Client already exists!");

    RMIObserver observer = addObserver(client);

    if(inProcess || client.spectating) {
      clientHolder.addSpectator(observer,client);
    } else {
      clientHolder.addInGameValue(observer,client);
      GameProcess.getInstance().setPlayer(client.toString());
    }
    notifyClientLists(observer, client);
  }

  public void removeClient(ClientInfo client) {
    final GameProcess process = GameProcess.getInstance();
    if(process.removePlayer(client.toString())) {
      if(process.isGameInProcess())
        process.abortGame();
      final RMIObserver observer = findObserver(client);
      if(observer != null) {
        clientHolder.removeKey(observer);
        getRMIObservable().removeObserver(observer);
      }
      notifyClientLists(null,null);
    }
  }

  private RMIObserver addObserver(ClientInfo client)
      throws NotBoundException, RemoteException {
    final Registry registry = LocateRegistry.getRegistry(client.ipAddress, client.port);
    final RMIObserver observer =
        (RMIObserver) registry.lookup(RMIService.OBSERVER.getServiceName(client.ipAddress));
    getRMIObservable().registerInterest(observer);
    return observer;
  }

  private RMIObserver findObserver(ClientInfo client) {
    for (RMIObserver observer : clientHolder.getAllKeys()) {
      final ClientInfo info = clientHolder.getValue(observer);
      if(info.toString().equals(client.toString())) {
        return observer;
      }
    }
    return null;
  }

  /**
   * Notifies the server gui and all clients which clients are currently in the list.
   * The first parameter is the observer of the client that was added.
   * If this parameter is null, only all other clients and the server gui will
   * be notified.
   */
  private void notifyClientLists(RMIObserver observer, ClientInfo addedClient) {
    setChangedAndNotify(GUIObserverType.REFRESH_CLIENT_LIST);
    if(observer != null)
      sendMessage(observer, new MessageObject(MessageType.OWN_CLIENT_INFO, addedClient));
    broadcastOtherClients(BroadcastType.LOGIN_LIST);
  }

  /* sends to each client a list with all the other logged in clients */
  void broadcastOtherClients(Enum<?> type) {
    final List<ClientInfo> clients = clientHolder.getAllValues();
    ClientInfo currentClient;

    /* iterate the clients so that the original sequence is not changed */
    for (RMIObserver rmiObserver : clientHolder.getAllKeys()) {
      currentClient = clientHolder.getValue(rmiObserver);
      final int index = Miscellaneous.findIndex(clients,currentClient,
          Miscellaneous.CLIENT_COMPARATOR);
      /* remove the client, send the rest of the list to all clients and add the current
       * client back to the list */
      clients.remove(index);
      sendMessage(rmiObserver, new MessageObject(type, clients));
      clients.add(index,currentClient);
    }
  }

  /* Getter and Setter */

  public RMIObservableImpl getRMIObservable() {
    return (RMIObservableImpl) services.get(RMIService.OBSERVABLE);
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

  public List<ClientInfo> getClients() {
    return clientHolder.getAllValues();
  }

  /* Inner classes */
}

class GameUpdater {
  private Logger LOGGER = Logger.getLogger(GameUpdater.class.getName());
  private Integer stackSize;
  private GameProcess process;
  private GameServer server;
  private InGameSpectatorHolder<RMIObserver,ClientInfo> clientHolder;

  public GameUpdater(Integer stackSize,
                     InGameSpectatorHolder<RMIObserver,ClientInfo> clientHolder) {
    this.stackSize = stackSize;
    this.clientHolder = clientHolder;
    this.process = GameProcess.getInstance();
    this.server = GameServer.getServerInstance();
  }

  public void invoke() {
    Integer cardsPerColour = stackSize / GameCardConstants.CardColour.values().length;
    process.initialiseNewGame(cardsPerColour);
    sendClientInit();
  }

  private void sendClientInit() {
    updateClients();
    server.broadcastMessage(GameUpdateType.STACK_UPDATE, Converter.toDTO(GameCardStack.getInstance()));
    sendPlayersInfos();
    server.broadcastOtherClients(GameUpdateType.INITIALISE_PLAYERS);
  }

  private void updateClients() {
    for (ClientInfo info : clientHolder.getAllValues()) {
      info.cardCount = process.getPlayerCards(info.toString()).size();
      info.playerType = process.getPlayerType(info.toString());
    }
  }

  public void updateMove(boolean nextRound) {
    final List<List<DTOCard>> allCards;
    updateClients();

    if(nextRound) {
      allCards = null;
      sendPlayersInfos();
      server.broadcastMessage(GameUpdateType.STACK_UPDATE,
          Converter.toDTO(GameCardStack.getInstance()));
    } else allCards = Converter.toDTO(process.getAttackCards(), process.getDefenseCards());

    /* update ingame cards, player list and if the next round is available */
    server.broadcastMessage(GameUpdateType.INGAME_CARDS, allCards);
    server.broadcastMessage(GameUpdateType.PLAYERS_UPDATE,
        Collections.list(Collections.enumeration(clientHolder.getInGameValues())));
    server.broadcastMessage(GameUpdateType.NEXT_ROUND_AVAILABLE, process.nextRoundAvailable());

    if(process.gameHasFinished()) {
      server.broadcastMessage(GameUpdateType.GAME_FINISHED);
    }
  }

  /* sends each client his player info */
  private void sendPlayersInfos() {
    for (RMIObserver rmiObserver : clientHolder.getInGameKeys()) {
      final ClientInfo client = clientHolder.getInGameValue(rmiObserver);
      server.sendMessage(rmiObserver, new MessageObject(GameUpdateType.CLIENT_CARDS,
          process.getPlayerCards(client.toString())));
      server.sendMessage(rmiObserver, new MessageObject(MessageType.OWN_CLIENT_INFO, client));
    }
  }
}

class InGameSpectatorHolder<K,V> {
  private Map<K,V> inGameMap;
  private Map<K,V> spectatorMap;

  /* Constructors */

  InGameSpectatorHolder() {
    inGameMap = new HashMap<K,V>(6);
    spectatorMap = new HashMap<K,V>(6);
  }

  /* Methods */

  public void addInGameValue(K key, V value) {
    spectatorMap.remove(key);
    inGameMap.put(key, value);
  }

  public void addSpectator(K key, V value) {
    inGameMap.remove(key);
    spectatorMap.put(key, value);
  }

  public void removeKey(K key) {
    inGameMap.remove(key);
    spectatorMap.remove(key);
  }

  /* Getter and Setter */

  /* Returns a copy of all values as one list */
  public List<V> getAllValues() {
    final List<V> values = new ArrayList<V>(inGameMap.size()+spectatorMap.size());
    Miscellaneous.addAllToCollection(values, inGameMap.values());
    Miscellaneous.addAllToCollection(values, spectatorMap.values());
    return values;
  }

  /* Returns a copy of all keys as one list */
  public List<K> getAllKeys() {
    final List<K> keys = new ArrayList<K>(inGameMap.size()+spectatorMap.size());
    Miscellaneous.addAllToCollection(keys, inGameMap.keySet());
    Miscellaneous.addAllToCollection(keys, spectatorMap.keySet());
    return keys;
  }

  public Map<K,V> getInGameMap() {
    return inGameMap;
  }

  public Map<K,V> getSpectatorMap() {
    return spectatorMap;
  }

  public V getInGameValue(K key) {
    return inGameMap.get(key);
  }

  public V getSpectatorValue(K key) {
    return spectatorMap.get(key);
  }

  public V getValue(K key) {
    if(inGameMap.containsKey(key))
      return inGameMap.get(key);
    else if(spectatorMap.containsKey(key))
      return spectatorMap.get(key);
    else return null;
  }

  public Collection<K> getInGameKeys() {
    return inGameMap.keySet();
  }

  public Collection<K> getSpectatorKeys() {
    return spectatorMap.keySet();
  }

  public Collection<V> getInGameValues() {
    return inGameMap.values();
  }

  public Collection<V> getSpectatorValues() {
    return spectatorMap.values();
  }
}