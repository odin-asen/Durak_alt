package server.business;

import dto.ClientInfo;
import dto.DTOCard;
import dto.message.*;
import game.GameCardStack;
import game.GameProcess;
import rmi.RMIObserver;
import rmi.RMIService;
import server.business.rmiImpl.*;
import utilities.Converter;
import utilities.Miscellaneous;
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

  private ServerClientHolder clientHolder;

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
    clientHolder = new ServerClientHolder();
    running = false;
  }

  /* Methods */
  private void registerService(Registry registry, Class implementationClass, RMIService service)
      throws RemoteException, IllegalAccessException, InstantiationException {
    final Object implementation = implementationClass.newInstance();
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
    registerService(registry, RoundStateAction.class, RMIService.ROUND_STATE_ACTION);
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
      LOGGER.info("Port " + port + " already used, getting this registry.");
      registry = LocateRegistry.getRegistry(port);
    }

    return registry;
  }

  public void shutdownServer() throws NotBoundException, RemoteException {
    if(isServerRunning()) {
      try {
        getRMIObservable().notifyObservers(new MessageObject(BroadcastType.SERVER_SHUTDOWN));
      } catch (ServerNotActiveException e) {
        LOGGER.warning("Server is not active: " +e.getMessage());
      }
      unregisterServices(registry);
      running = false;
    }
  }

  public void startGame(Integer stackSize) {
    gameUpdater = new GameUpdater(stackSize, clientHolder);
    gameUpdater.invoke();
  }

  void setChangedAndNotify(Enum<?> type) {
    setChangedAndNotify(type, null);
  }

  void setChangedAndNotify(Enum<?> type, Object sendingObject) {
    setChanged();
    notifyObservers(new MessageObject(type, sendingObject));
  }

  public void broadcastMessage(Enum<?> type, Object sendingObject) {
    final RMIObservableImpl observable = getRMIObservable();
    try {
      observable.notifyObservers(new MessageObject(type, sendingObject));
    } catch (RemoteException e) {
      LOGGER.severe(e.getMessage());
    } catch (ServerNotActiveException e) {
      LOGGER.severe(e.getMessage());
    }
  }

  public void sendProcessUpdate(boolean nextRound) {
    gameUpdater.updateMove(nextRound);
  }

  public void broadcastMessage(Enum<?> type) {
    broadcastMessage(type, null);
  }

  public void sendMessage(RMIClient rmiClient, MessageObject messageObject) {
    getRMIObservable().notifyObserver(rmiClient.rmiObserver, messageObject);
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
  public void broadcastArray(Enum<?> type, List<RMIClient> rmiClients, List<?> sendingObjects)
      throws GameServerException {
    if(sendingObjects.size() != rmiClients.size())
      throw new GameServerException("The number of server threads and sending objects is not equal!");
    for (int i = 0; i < sendingObjects.size(); i++) {
        getRMIObservable().notifyObserver(rmiClients.get(i).rmiObserver,
            new MessageObject(type, sendingObjects.get(i)));
    }
  }

  public void addClient(ClientInfo client) {
    final RMIClient addedClient;
    final RMIObserver lastObserver = getRMIObservable().getObservers().lastElement();
    final Boolean inProcess = GameProcess.getInstance().isGameInProcess();

    if(inProcess || client.spectating) { //TODO testen, ob nach einem Neustart des Spiels, ein Spieler, der sich für Zuschauen entschiden hat, auch hier rein kommt
      addedClient = clientHolder.addSpectator(new RMIClient(lastObserver, client));
      if(addedClient != null)
        notifyClientLists(addedClient);
    } else {
      addedClient = clientHolder.addInGameClient(new RMIClient(lastObserver, client));
      if(addedClient != null) {
        GameProcess.getInstance().addPlayer();
        notifyClientLists(addedClient);
      }
    }
  }

  private void notifyClientLists(RMIClient addedClient) {
    setChangedAndNotify(GUIObserverType.REFRESH_CLIENT_LIST, addedClient.clientInfo);
    sendMessage(addedClient, new MessageObject(MessageType.LOGIN_NUMBER, addedClient.clientInfo));
    broadcastMessage(BroadcastType.LOGIN_LIST, clientHolder.getAllClientInfo());
  }

  public void removeClient(ClientInfo toRemove) {
    RMIClient reference = clientHolder.getIsEqualInGameClient(toRemove);
    if(reference != null) {
      deletePlayer(reference);
      removeAndUpdateClients(clientHolder.getInGameClients(), reference);
    } else {
      reference = clientHolder.getIsEqualSpectator(toRemove);
      if(reference != null)
        removeAndUpdateClients(clientHolder.getSpectators(), reference);
    }
  }

  private void removeAndUpdateClients(List<RMIClient> clientList, RMIClient toRemove) {
    sendResetClientNumber(toRemove);
    clientHolder.removeClient(toRemove);
    getRMIObservable().removeObserver(toRemove.rmiObserver);
    setChangedAndNotify(GUIObserverType.REFRESH_CLIENT_LIST, toRemove.clientInfo);
    try {
      final List<ClientInfo> clientInfoList = new ArrayList<ClientInfo>();
      for (RMIClient client : clientList)
        clientInfoList.add(client.clientInfo);
      broadcastArray(MessageType.LOGIN_NUMBER, clientList, clientInfoList);
      broadcastMessage(BroadcastType.LOGIN_LIST, clientInfoList);
    } catch (GameServerException e) {
      LOGGER.severe(e.getMessage());
    }
  }

  private void deletePlayer(RMIClient rmiClient) {
    final GameProcess process = GameProcess.getInstance();
    process.removePlayer(rmiClient.clientInfo.loginNumber);
    if(process.isGameInProcess())
      process.abortGame();
  }

  private void sendResetClientNumber(RMIClient client) {
    final ClientInfo newLoginNumber = new ClientInfo("", (short) -1);
    newLoginNumber.setClientInfo(client.clientInfo);
    newLoginNumber.loginNumber = GameConfigurationConstants.NO_LOGIN_NUMBER;
    sendMessage(client, new MessageObject(MessageType.LOGIN_NUMBER, newLoginNumber));
  }

  /* Getter and Setter */
  public List<ClientInfo> getClients() {
    return clientHolder.getAllInGameClientInfo();
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
}

class GameUpdater {
  private Logger LOGGER = Logger.getLogger(GameUpdater.class.getName());
  private Integer stackSize;
  private GameProcess process;
  private GameServer server;
  private ServerClientHolder clientHolder;

  public GameUpdater(Integer stackSize, ServerClientHolder clientHolder) {
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
    try {
      updateClients();
      server.broadcastMessage(GameUpdateType.STACK_UPDATE, Converter.toDTO(GameCardStack.getInstance()));
      server.broadcastArray(GameUpdateType.CLIENT_CARDS, clientHolder.getInGameClients(), process.getPlayerCards());
      server.broadcastMessage(GameUpdateType.INITIALISE_PLAYERS, clientHolder.getAllInGameClientInfo());
    } catch (GameServerException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
    }
  }

  private void updateClients() {
    final List<List<DTOCard>> clientCardCounts = process.getPlayerCards();
    final List<PlayerConstants.PlayerType> types = process.getPlayerTypes();
    for (int index = 0; index < clientHolder.getInGameClients().size(); index++) {
      final RMIClient rmiClient = clientHolder.getInGameClients().get(index);
      rmiClient.clientInfo.cardCount = clientCardCounts.get(index).size();
      rmiClient.clientInfo.playerType = types.get(index);
    }
  }

  public void updateMove(boolean nextRound) {
    final List<List<DTOCard>> allCards;
    updateClients();

    try {
      if(nextRound) {
        allCards = null;
        server.broadcastArray(GameUpdateType.CLIENT_CARDS,
            clientHolder.getInGameClients(), process.getPlayerCards());
        server.broadcastMessage(GameUpdateType.STACK_UPDATE,
            Converter.toDTO(GameCardStack.getInstance()));
      } else allCards = Converter.toDTO(process.getAttackCards(), process.getDefenseCards());
      server.broadcastMessage(GameUpdateType.INGAME_CARDS, allCards);
      server.broadcastMessage(GameUpdateType.PLAYERS_UPDATE, clientHolder.getAllInGameClientInfo());
      server.broadcastMessage(GameUpdateType.NEXT_ROUND_AVAILABLE, process.nextRoundAvailable());
    } catch (GameServerException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
    }

    if(process.gameHasFinished()) {
      server.broadcastMessage(GameUpdateType.GAME_FINISHED);
    }
  }
}

class ServerClientHolder {
  private List<RMIClient> inGameClients;
  private List<RMIClient> spectators;

  /* Constructors */
  ServerClientHolder() {
    inGameClients = new ArrayList<RMIClient>();
    spectators = new ArrayList<RMIClient>();
  }

  /* Methods */
  /**
   * Adds an rmiClient to the server and returns this rmiClient.
   * @param rmiClient ClientInfo object to add to the server
   * @return Returns the added ClientInfo object or null, if the object wasn't
   * added.
   */
  public RMIClient addInGameClient(RMIClient rmiClient) {
    final Integer nextNumber = inGameClients.size();
    if(rmiClient.clientInfo.loginNumber.equals(GameConfigurationConstants.NO_LOGIN_NUMBER) ||
        nextNumber.shortValue() < rmiClient.clientInfo.loginNumber) {
      rmiClient.clientInfo.loginNumber = nextNumber.shortValue();
      inGameClients.add(rmiClient);
      return rmiClient;
    }

    return null;
  }

  public RMIClient addSpectator(RMIClient rmiClient) {
    final Integer nextNumber = GameConfigurationConstants.SPECTATOR_START_NUMBER
        + spectators.size();
    if(rmiClient.clientInfo.loginNumber.equals(GameConfigurationConstants.NO_LOGIN_NUMBER) ||
        nextNumber.shortValue() < rmiClient.clientInfo.loginNumber) {
      rmiClient.clientInfo.loginNumber = nextNumber.shortValue();
      spectators.add(rmiClient);
      return rmiClient;
    }

    return null;
  }

  public void removeClient(RMIClient rmiClient) {
    inGameClients.remove(rmiClient);
    spectators.remove(rmiClient);
    refreshAllClients();
  }

  private void refreshAllClients() {
    refreshClientListNumbers(inGameClients);
    refreshClientListNumbers(spectators);
  }

  private void refreshClientListNumbers(List<RMIClient> list) {
    for (int index = 0; index < list.size(); index++) {
      final ClientInfo client = list.get(index).clientInfo;
      if(client.loginNumber != index)
        client.loginNumber = (short) index;
    }
  }

  private RMIClient getIsEqualClient(List<RMIClient> list, ClientInfo info) {
    for (RMIClient client : list) {
      if(client.clientInfo.isEqual(info)) {
        client.clientInfo.setClientInfo(info);
        return client;
      }
    }
    return null;
  }

  /* Getter and Setter */
  public RMIClient getIsEqualInGameClient(ClientInfo info) {
    return getIsEqualClient(inGameClients, info);
  }

  public RMIClient getIsEqualSpectator(ClientInfo info) {
    return getIsEqualClient(spectators, info);
  }

  public List<RMIClient> getInGameClients() {
    return inGameClients;
  }

  public List<RMIClient> getSpectators() {
    return spectators;
  }

  public List<ClientInfo> getAllClientInfo() {
    final List<ClientInfo> all = new ArrayList<ClientInfo>();
    Miscellaneous.addAllToCollection(all, getAllInGameClientInfo());
    Miscellaneous.addAllToCollection(all, getAllSpectatorClientInfo());
    return all;
  }

  public List<ClientInfo> getAllSpectatorClientInfo() {
    final List<ClientInfo> all = new ArrayList<ClientInfo>();
    for (RMIClient client : spectators)
      all.add(client.clientInfo);
    return all;
  }

  public List<ClientInfo> getAllInGameClientInfo() {
    final List<ClientInfo> all = new ArrayList<ClientInfo>();
    for (RMIClient client : inGameClients)
      all.add(client.clientInfo);
    return all;
  }
}

class RMIClient {
  RMIObserver rmiObserver;
  ClientInfo clientInfo;

  RMIClient(RMIObserver rmiObserver, ClientInfo clientInfo) {
    this.rmiObserver = rmiObserver;
    this.clientInfo = clientInfo;
  }
}