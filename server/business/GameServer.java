package server.business;

import common.dto.DTOCard;
import common.dto.DTOClient;
import common.dto.message.*;
import common.game.GameProcess;
import common.game.rules.RuleException;
import common.i18n.I18nSupport;
import common.simon.Callbackable;
import common.simon.ServerInterface;
import common.simon.action.GameAction;
import common.utilities.Converter;
import common.utilities.LoggingUtility;
import common.utilities.Miscellaneous;
import common.utilities.constants.GameCardConstants;
import common.utilities.constants.GameConfigurationConstants;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.NameBindingException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 21:57
 */
public class GameServer extends Observable {
  private static Logger LOGGER = LoggingUtility.getLogger(GameServer.class.getName());
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS

  private static GameServer gameServer;

  private Integer port;
  private Boolean running;

  private DurakServices durakServices;
  private GameUpdater gameUpdater;
  private Registry registry;

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
    port = GameConfigurationConstants.DEFAULT_PORT;
    this.gameUpdater = new GameUpdater();
    running = false;
  }

  /* Methods */

  /**
   * Starts the server and sets a password.
   * @param password Server password.
   * @throws GameServerException Will be thrown and delivers the apropriate user message if
   * the server can not be started for a reason.
   */
  public void startServer(String password) throws GameServerException {
    if(!isServerRunning()) {
      final String name = GameConfigurationConstants.REGISTRY_NAME_SERVER;
      durakServices = new DurakServices(password);

      try {
        registry = Simon.createRegistry(port);
        registry.bind(name, durakServices);
        running = true;
      } catch (NameBindingException e) {
        LOGGER.warning("Name \"" + name + "\"already bound: " + e.getMessage());
        throw new GameServerException(I18nSupport.getValue(MSGS_BUNDLE, ""));
      } catch (UnknownHostException e) {
        LOGGER.warning("Could not find ip address: "+e.getMessage());
        throw new GameServerException(I18nSupport.getValue(MSGS_BUNDLE, "network.error"));
      } catch (IOException e) {
        LOGGER.severe("I/O exception: " + e.getMessage());
        throw new GameServerException(I18nSupport.getValue(MSGS_BUNDLE, "network.error"));
      }
      LOGGER.info(LoggingUtility.STARS+" Server started "+LoggingUtility.STARS);
    }
  }

  /**
   * Removes all client references out of the list and shuts the server down.
   */
  public void shutdownServer() {
    if(isServerRunning()) {
      broadcastMessage(BroadcastType.SERVER_SHUTDOWN);
      setChangedAndNotify(GUIObserverType.REMOVE_CLIENTS);
      gameUpdater.stopSession();
      registry.unbind(GameConfigurationConstants.REGISTRY_NAME_SERVER);
      registry.stop();
      running = false;
      LOGGER.info(LoggingUtility.STARS+" Server shut down "+LoggingUtility.STARS);
    }
  }

  public void startGame(Integer stackSize) {
    if(gameUpdater.invokeGame(stackSize)) {
      LOGGER.info(LoggingUtility.STARS+" Game started "+LoggingUtility.STARS);
    } else LOGGER.info("Not enogh player for a game");
  }

  public void stopGame() {
    gameUpdater.stopGame();
    LOGGER.info(LoggingUtility.STARS+" Game stopped "+LoggingUtility.STARS);
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
    List<Callbackable> callbackables = gameUpdater.getRemoteReferrences();
    for (Callbackable callbackable : callbackables) {
      sendMessage(callbackable,new MessageObject(type, sendingObject));
    }
  }

  public void broadcastMessage(Enum<?> type) {
    broadcastMessage(type, null);
  }

  public void sendMessage(Callbackable callbackable, MessageObject messageObject) {
    callbackable.callback(messageObject);
  }

  /**
   * Sends a MessageObject object to all clients. For each client the MessageObject
   * object contains as sendingObject the corresponding object of the list
   * {@code sendingObjects}.
   * @param type Type of the message.
   * @param client Clients to send the messages to.
   * @param sendingObjects Sending objects for the clients.
   * @throws GameServerException {@code clients} and {@code sendingObjects} have different sizes.
   */
  public void broadcastArray(Enum<?> type, Collection<Callbackable> clients,
                             Collection<?> sendingObjects)
      throws GameServerException {
    if(sendingObjects.size() != clients.size())
      throw new GameServerException("The number of clients and sending objects is not equal!");

    final Iterator<Callbackable> callbackableIterator = clients.iterator();
    final Iterator<?> objects = sendingObjects.iterator();
    while (callbackableIterator.hasNext()) {
      sendMessage(callbackableIterator.next(), new MessageObject(type, objects.next()));
    }
  }

  /**
   * Adds the client to the server list and notifies the gui and all clients.
   * @param callbackable Client remote reference.
   * @param client The client.
   * @return Returns true if client was added, else false.
   */
  boolean addClient(Callbackable callbackable, DTOClient client) {
    if(gameUpdater.addClient(callbackable, client))
      notifyClientLists(callbackable);
    else return false;

    LOGGER.info("Added client: "+client);
    return true;
  }

  /**
   * Removes a client from the server and notifies the gui and all clients.
   * @param callbackable Client remote reference.
   * @return Returns true if the client was removed, else false.
   */
  boolean removeClient(Callbackable callbackable) {
    final DTOClient client = getClient(callbackable);
    if(gameUpdater.removeClient(callbackable))
      GameServer.getServerInstance().notifyClientLists(null);
    else return false;

    LOGGER.info("Removed client: "+client);
    return true;
  }

  /**
   * Notifies the server gui and all clients which clients are currently in the list.
   * The first parameter is the remote object of the client that was added.
   * If this parameter is null, all clients and the server gui will
   * be notified. The second parameter will then be ignored.
   */
  private void notifyClientLists(Callbackable callbackable) {
    setChangedAndNotify(GUIObserverType.REFRESH_CLIENT_LIST);
    if(callbackable != null) {
      DTOClient client = gameUpdater.getClient(callbackable);
      System.out.println("OWN_INFO: "+client.name);
      sendMessage(callbackable, new MessageObject(MessageType.OWN_CLIENT_INFO,
          client));
    }
    broadcastOtherClients(BroadcastType.LOGIN_LIST);
  }

  /* sends to each client a list with all the other logged in clients */
  void broadcastOtherClients(Enum<?> type) {
    final List<DTOClient> clients = new ArrayList<DTOClient>();
    final List<Callbackable> callbackables = new ArrayList<Callbackable>();
    DTOClient currentClient;

    /* iterate the clients so that the original sequence is not changed */
    Miscellaneous.addAllToCollection(clients, gameUpdater.getClients());
    Miscellaneous.addAllToCollection(callbackables, gameUpdater.getRemoteReferrences());
    for (Callbackable callbackable : callbackables) {
      currentClient = gameUpdater.getClient(callbackable);
      System.out.println("Broadcast other than "+currentClient.name);
      final int index = Miscellaneous.findIndex(clients,currentClient,
          Miscellaneous.CLIENT_COMPARATOR);
      /* remove the client, send the rest of the list to all clients and add the current
       * client back to the list */
      clients.remove(index);
      for (DTOClient client : clients) {
        System.out.println("anderer client: "+client.name);
      }
      sendMessage(callbackable, new MessageObject(type, clients));
      clients.add(index,currentClient);
    }
  }

  /**
   * Validates an action by calling the game updater.
   * @param callbackable Client remote reference.
   * @param action Attached GameAction object.
   * @throws RuleException Will be thrown if the client broke a game rule.
   */
  void validateAction(Callbackable callbackable, GameAction action) throws RuleException {
    boolean nextRound = gameUpdater.getProcess().validateAction(
        action, gameUpdater.getClient(callbackable).hashCode());
    gameUpdater.updateMove(nextRound);
  }

  public void updateClient(Callbackable callbackable, DTOClient client) {
    final DTOClient oldClient = gameUpdater.updateClientInformation(callbackable, client);
    LOGGER.info("Updated client from "+oldClient+" to "+client);
    notifyClientLists(null);
  }

  public boolean clientNameExists(String name) {
    boolean exists = false;
    for (DTOClient client : gameUpdater.getClients()) {
      exists = exists || client.name.equals(name);
    }
    return exists;
  }

  /* Getter and Setter */

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

  public List<DTOClient> getClients() {
    return gameUpdater.getClients();
  }

  public DTOClient getClient(Callbackable callbackable) {
    return gameUpdater.getClient(callbackable);
  }
}

/******************************************/
/********** Simon Implementation **********/
/******************************************/
@SimonRemote(value={ServerInterface.class})
class DurakServices implements ServerInterface {
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS
  private static final Logger LOGGER = LoggingUtility.getLogger(DurakServices.class.getName());

  private String password;

  DurakServices(String password) {
    this.password = password;
  }

  public boolean login(Callbackable callbackable, DTOClient client, String password) {
    boolean result = false;
    final GameServer server = GameServer.getServerInstance();

    if(this.password.equals(password)) {
      if(!server.clientNameExists(client.name))
        result = server.addClient(callbackable, client);
      else server.sendMessage(callbackable, new MessageObject(MessageType.STATUS_MESSAGE,
          I18nSupport.getValue(MSGS_BUNDLE, "status.name.0.already.exists", client.name)));
    } else {
      server.sendMessage(callbackable, new MessageObject(MessageType.STATUS_MESSAGE,
          I18nSupport.getValue(MSGS_BUNDLE,"status.permission.denied")));
    }
    return result;
  }

  public void logoff(Callbackable callbackable) {
    GameServer.getServerInstance().removeClient(callbackable);
  }

  public void sendChatMessage(Callbackable callbackable, String message) {
    final DTOClient client = GameServer.getServerInstance().getClient(callbackable);
    if(client != null) {
      ChatMessage chatMessage = new ChatMessage(new Long(System.currentTimeMillis()),
          client, message);
      GameServer.getServerInstance().broadcastMessage(BroadcastType.CHAT_MESSAGE, chatMessage);
    }
  }

  public boolean doAction(Callbackable callbackable, GameAction action) {
    boolean actionDone = false;
    final GameServer server = GameServer.getServerInstance();
    try {
      server.validateAction(callbackable, action);
      actionDone = true;
    } catch (RuleException e) {
      LOGGER.info("User \'" + action.getExecutor().name
          + "\' breaks the rules with action " + action);
      server.sendMessage(callbackable,
          new MessageObject(MessageType.RULE_MESSAGE, e.getMessage()));
    }

    return actionDone;
  }

  public void updateClient(Callbackable callbackable, DTOClient client) {
    GameServer.getServerInstance().updateClient(callbackable, client);
  }

  /* Getter and Setter */

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }
}

class GameUpdater {
  private Logger LOGGER = LoggingUtility.getLogger(GameUpdater.class.getName());
  private GameProcess<Integer> process;
  private GameServer server = null;
  private InGameSpectatorHolder<Callbackable,DTOClient> clientHolder;

  public GameUpdater() {
    this.clientHolder = new InGameSpectatorHolder<Callbackable, DTOClient>();
    this.process = new GameProcess<Integer>();
  }

  /**
   * Invokes a game if it is not running already and if there are
   * enough players.
   * @param stackSize Stacksize for the game.
   * @return Returns true, if the game was invoked, else false.
   */
  public boolean invokeGame(Integer stackSize) {
    boolean invoked = false;
    Integer cardsPerColour = stackSize / GameCardConstants.CardColour.values().length;
    if (!process.isGameInProcess() && process.initialiseNewGame(cardsPerColour)) {
      server = GameServer.getServerInstance();
      sendClientInit();
      invoked = true;
    }
    return invoked;
  }

  /**
   * Stops the game and removes all clients and their references.
   */
  public void stopSession() {
    process.reInitialiseGame();
    clientHolder.removeAll();
  }

  public void stopGame() {
    process.reInitialiseGame();
  }

  /**
   * Adds the client to the list if it dow not already exists.
   * @param callbackable Client remote reference.
   * @param client The client.
   * @return True if the client was added, else false.
   */
  boolean addClient(Callbackable callbackable, DTOClient client) {
    if(clientHolder.containsKey(callbackable))
      return false;

    /* Create a local reference of the DTOClient object on the server */
    final DTOClient localClient = new DTOClient("");
    localClient.setClientInfo(client);

    if(process.isGameInProcess() || localClient.spectating) {
      clientHolder.addSpectator(callbackable,localClient);
    } else {
      clientHolder.addInGameValue(callbackable,localClient);
      process.setPlayer(localClient.hashCode());
    }

    return true;
  }

  /**
   * Removes a client from the list if it exists.
   * @param callbackable Remote reference of the client.
   * @return True if client existed, else false.
   */
  boolean removeClient(Callbackable callbackable) {
    if(process.removePlayer(clientHolder.getValue(callbackable).hashCode())) {
      if(process.isGameInProcess())
        process.reInitialiseGame();
      clientHolder.removeKey(callbackable);
    } else return false;

    return true;
  }

  /**
   * Changes the client information depending on the remote reference and returns the old
   * information.
   * @param callbackable Remote reference of the client.
   * @param client The new client information.
   * @return Returns the old information.
   */
  public DTOClient updateClientInformation(Callbackable callbackable, DTOClient client) {
    DTOClient oldClient = clientHolder.getValue(callbackable);
    oldClient.setClientInfo(client);
    return oldClient;
  }

  private void sendClientInit() {
    updateClients();
    server.broadcastMessage(GameUpdateType.STACK_UPDATE, Converter.toDTO(process.getStack()));
    sendPlayersInfos();
    server.broadcastOtherClients(GameUpdateType.INITIALISE_PLAYERS);
  }

  private void updateClients() {
    for (Callbackable callbackable : clientHolder.getAllKeys()) {
      final DTOClient client = clientHolder.getValue(callbackable);
      client.cardCount = process.getPlayerCards(client.hashCode()).size();
      client.playerType = process.getPlayerType(client.hashCode());
    }
  }

  public void updateMove(boolean nextRound) {
    final List<List<DTOCard>> allCards;
    updateClients();

    if(nextRound) {
      allCards = null;
      sendPlayersInfos();
      server.broadcastMessage(GameUpdateType.STACK_UPDATE,
          Converter.toDTO(process.getStack()));
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
    for (Callbackable callbackable : clientHolder.getInGameKeys()) {
      final DTOClient client = clientHolder.getInGameValue(callbackable);
      server.sendMessage(callbackable, new MessageObject(GameUpdateType.CLIENT_CARDS,
          process.getPlayerCards(client.hashCode())));
      server.sendMessage(callbackable, new MessageObject(MessageType.OWN_CLIENT_INFO, client));
    }
  }

  public DTOClient getClient(Callbackable callbackable) {
    return clientHolder.getValue(callbackable);
  }

  public List<DTOClient> getClients() {
    return clientHolder.getAllValues();
  }

  public List<Callbackable> getRemoteReferrences() {
    return clientHolder.getAllKeys();
  }

  public GameProcess<Integer> getProcess() {
    return process;
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

  public boolean containsKey(K key) {
    return inGameMap.containsKey(key) || spectatorMap.containsKey(key);
  }

  public void removeAll() {
    inGameMap.clear();
    spectatorMap.clear();
  }
}