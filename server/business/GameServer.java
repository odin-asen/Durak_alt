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
  private GameUpdate gameUpdate;
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
    this.gameUpdate = new GameUpdate();
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
      gameUpdate.stopSession();
      registry.unbind(GameConfigurationConstants.REGISTRY_NAME_SERVER);
      registry.stop();
      running = false;
      LOGGER.info(LoggingUtility.STARS+" Server shut down "+LoggingUtility.STARS);
    }
  }

  public boolean startGame(Integer stackSize) {
    if(gameUpdate.invokeGame(stackSize)) {
      LOGGER.info(LoggingUtility.STARS+" Game started "+LoggingUtility.STARS);
      return true;
    } else LOGGER.info("Not enough player for a game");
    return false;
  }

  /**
   * Stops the game. The boolean parameter specifies, if the game was aborted or properly
   * finished.
   * @param aborted If true, every client will be notified that it was aborted, else just finished.
   */
  public void stopGame(boolean aborted, String reason) {
    gameUpdate.stopGame(false);
    if(aborted)
      broadcastMessage(GameUpdateType.GAME_ABORTED, reason);
    else broadcastMessage(GameUpdateType.GAME_FINISHED);
    setChangedAndNotify(GUIObserverType.GAME_FINISHED);
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

  /**
   * Sends the same message to every client.
   * @param type Type that indicates the message type.
   * @param sendingObject Sending object to send.
   */
  public void broadcastMessage(Enum<?> type, Object sendingObject) {
    List<Callbackable> callbackables = gameUpdate.getRemoteReferrences();
    broadcastMessage(type, callbackables, sendingObject);
  }

  /**
   * Sends the same message to every client.
   * @param type Type that indicates the message type.
   */
  public void broadcastMessage(Enum<?> type) {
    broadcastMessage(type, null);
  }

  /**
   * Sends the same message to every client remote reference in the list.
   * @param type Type that indicates the message type.
   * @param callbackables Remote references to send the message to.
   * @param sendingObject Sending object to send.
   */
  public void broadcastMessage(Enum<?> type, Collection<Callbackable> callbackables,
                               Object sendingObject) {
    for (Callbackable callbackable : callbackables) {
      sendMessage(callbackable, new MessageObject(type, sendingObject));
    }
  }

  /* sends to each client a list with all the other logged in clients */
  void broadcastOtherClients(Enum<?> type) {
    broadcastOtherClients(type, gameUpdate.getRemoteReferrences());
  }

  void broadcastOtherClients(Enum<?> type, Collection<Callbackable> callbackables) {
    final List<DTOClient> clients = new ArrayList<DTOClient>();
    final List<Callbackable> callbackableList = new ArrayList<Callbackable>();
    DTOClient currentClient;

    /* iterate the clients so that the original sequence is not changed */
    Miscellaneous.addAllToCollection(clients, gameUpdate.getClients(callbackables));
    Miscellaneous.addAllToCollection(callbackableList, callbackables);
    for (Callbackable callbackable : callbackableList) {
      currentClient = gameUpdate.getClient(callbackable);
      final int index = Miscellaneous.findIndex(clients,currentClient,
          Miscellaneous.CLIENT_COMPARATOR);
      /* remove the client, send the rest of the list to all clients and add the current
       * client back to the list */
      clients.remove(index);
      sendMessage(callbackable, new MessageObject(type, clients));
      clients.add(index,currentClient);
    }
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
    if(gameUpdate.addClient(callbackable, client))
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
    if(gameUpdate.removeClient(callbackable)) {
      if(!client.spectating) {
        stopGame(true, I18nSupport.getValue(MSGS_BUNDLE, "game.abort.player.0.logged.off",
            client.name));
      }
      notifyClientLists(null);
    } else return false;

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
    setChangedAndNotify(GUIObserverType.CLIENT_LIST);
    if(callbackable != null) {
      DTOClient client = gameUpdate.getClient(callbackable);
      sendMessage(callbackable, new MessageObject(MessageType.OWN_CLIENT_INFO,
          client));
    }
    broadcastOtherClients(BroadcastType.LOGIN_LIST);
  }

  /**
   * Validates an action by calling the game updater.
   * @param callbackable Client remote reference.
   * @param action Attached GameAction object.
   * @throws RuleException Will be thrown if the client broke a game rule.
   */
  void validateAction(Callbackable callbackable, GameAction action) throws RuleException {
    boolean nextRound = gameUpdate.getProcess().validateAction(
        action, gameUpdate.getPlayerID(callbackable));
    gameUpdate.updateMove(nextRound);
  }

  /**
   * Updates the client and and all the lists if necessary.
   * @param callbackable Client remote reference
   * @param client Client information object.
   */
  public void updateClient(Callbackable callbackable, DTOClient client) {
    final DTOClient oldClient = gameUpdate.updateClientInformation(callbackable, client);
    notifyClientLists(null);
    LOGGER.info("Updated client from "+oldClient+" to "+client);
  }

  public boolean clientNameExists(String name) {
    boolean exists = false;
    for (DTOClient client : gameUpdate.getClients()) {
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
    return gameUpdate.getClients();
  }

  public DTOClient getClient(Callbackable callbackable) {
    return gameUpdate.getClient(callbackable);
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

class GameUpdate {
  private Logger LOGGER = LoggingUtility.getLogger(GameUpdate.class.getName());
  private GameProcess<Integer> process;
  private GameServer server = null;
  private InGameSpectatorHolder<Callbackable,DTOClient> clientHolder;

  public GameUpdate() {
    this.clientHolder = new InGameSpectatorHolder<Callbackable, DTOClient>();
    this.process = new GameProcess<Integer>();
  }

  /**
   * Gets the player's ID for the surpassed client. The hashCode method of this object
   * should work like it should work for the use of a HashMap.
   * @param client Surpassed client.
   * @return Should return a unique integer for this object.
   */
  private int getPlayerID(DTOClient client) {
    return client.hashCode();
  }

  /**
   * Gets the player's ID for the surpassed callbackable calling
   * {@link #getPlayerID(common.dto.DTOClient)}.
   * @param callbackable Surpassed client remote reference.
   * @return Should return a unique integer for this object.
   */
  public Integer getPlayerID(Callbackable callbackable) {
    return getPlayerID(getClient(callbackable));
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
    stopGame(true);
    clientHolder.clear();
  }

  /**
   * Stops the game process. All settings will be reset. Depending on the parameter all
   * already registered players will be either delted from the list or not. (This means the
   * process will be totally reset to the initial state.
   * @param deletePlayers If true, players will be deleted.
   */
  public void stopGame(boolean deletePlayers) {
    /* stop the game with deleting the players */
    process.reInitialise();
    if(!deletePlayers) {
      /* add the deleted players back to the list */
      for (DTOClient client : clientHolder.getInGameValues()) {
        process.setPlayer(getPlayerID(client));
      }
    }
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
      localClient.spectating = true;
      clientHolder.addSpectator(callbackable,localClient);
    } else {
      clientHolder.addInGameValue(callbackable,localClient);
      process.setPlayer(getPlayerID(localClient));
    }

    return true;
  }

  /**
   * Removes a client from the list if it exists and aborts the game if necessary.
   * @param callbackable Remote reference of the client.
   * @return True if the client was removed, else false.
   */
  boolean removeClient(Callbackable callbackable) {
    boolean removed = false;
    /* Client's player reference has also to be removed and if it was a player */
    /* In every case delete afterwards the client remote reference */
    process.removePlayer(getPlayerID(callbackable));
    removed = clientHolder.removeKey(callbackable);

    return removed;
  }

  /**
   * Changes the client information depending on the remote reference and returns the old
   * information. The lists will also be updated if necessary.
   * @param callbackable Remote reference of the client.
   * @param client The new client information.
   * @return Returns the old information.
   */
  public DTOClient updateClientInformation(Callbackable callbackable, DTOClient client) {
    DTOClient oldClient = clientHolder.getValue(callbackable);
    removeClient(callbackable);
    addClient(callbackable, client);
    return oldClient;
  }

  private void sendClientInit() {
    gameUpdateClients();
    server.broadcastMessage(GameUpdateType.STACK_UPDATE, Converter.toDTO(process.getStack()));
    informInGamePlayers();
    informSpectators();
  }

  private void gameUpdateClients() {
    for (DTOClient client : clientHolder.getInGameValues()) {
      client.cardCount = process.getPlayerCards(getPlayerID(client)).size();
      client.playerType = process.getPlayerType(getPlayerID(client));
    }
  }

  public void updateMove(boolean nextRound) {
    final List<List<DTOCard>> allCards;
    gameUpdateClients();

    if(nextRound) {
      allCards = null;
      informInGamePlayers();
      informSpectators();
      server.broadcastMessage(GameUpdateType.STACK_UPDATE,
          Converter.toDTO(process.getStack()));
    } else allCards = Converter.toDTO(process.getAttackCards(), process.getDefenseCards());

    /* update ingame cards, player list and if the next round is available */
    /* send it to all connected clients, not just inGame clients */
    server.broadcastMessage(GameUpdateType.INGAME_CARDS, allCards);
    server.broadcastMessage(GameUpdateType.PLAYERS_UPDATE,
        Collections.list(Collections.enumeration(clientHolder.getAllValues())));
    server.broadcastMessage(GameUpdateType.NEXT_ROUND_AVAILABLE, process.nextRoundAvailable());

    if(process.gameHasFinished()) {   //TODO ausprobieren, ob dieser Block an den Anfang kann oder nicth
      server.stopGame(false, "");
    }
  }

  /**
   * Sends each inGame client his player info and all opponent's info
   * (number of cards, name, etc...)
   */
  private void informInGamePlayers() {
    for (Callbackable callbackable : clientHolder.getInGameKeys()) {
      final DTOClient client = clientHolder.getInGameValue(callbackable);
      server.sendMessage(callbackable, new MessageObject(GameUpdateType.CLIENT_CARDS,
          process.getPlayerCards(getPlayerID(client))));
      server.sendMessage(callbackable, new MessageObject(MessageType.OWN_CLIENT_INFO, client));
    }
    /* inform in game clients about opponents */
    server.broadcastOtherClients(GameUpdateType.INITIALISE_PLAYERS, clientHolder.getInGameKeys());
  }

  /**
   * Sends each spectator the inGame player's info (number of cards, name, etc...)
   */
  private void informSpectators() {
    final List<DTOClient> clients = new ArrayList<DTOClient>();
    for (DTOClient client : clientHolder.getInGameValues()) {
      clients.add(client);
    }
    server.broadcastMessage(GameUpdateType.INITIALISE_PLAYERS, clientHolder.getSpectatorKeys(),
        clients);
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

  public List<DTOClient> getClients(Collection<Callbackable> callbackables) {
    final List<DTOClient> clients = new ArrayList<DTOClient>(callbackables.size());
    for (Callbackable callbackable : callbackables) {
      clients.add(clientHolder.getValue(callbackable));
    }
    return clients;
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

  public boolean removeKey(K key) {
    boolean removed = false;
    removed = removed || (inGameMap.remove(key) != null);
    removed = removed || (spectatorMap.remove(key) != null);
    return removed;
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

  public void clear() {
    inGameMap.clear();
    spectatorMap.clear();
  }
}