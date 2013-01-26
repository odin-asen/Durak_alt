package server.business;

import common.dto.DTOClient;
import common.dto.message.*;
import common.game.GameProcess;
import common.game.rules.RuleException;
import common.i18n.I18nSupport;
import common.simon.Callable;
import common.simon.ServerInterface;
import common.simon.action.GameAction;
import common.utilities.Converter;
import common.utilities.LoggingUtility;
import common.utilities.Miscellaneous;
import common.utilities.constants.GameCardConstants;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.constants.PlayerConstants;
import de.root1.simon.ClosedListener;
import de.root1.simon.Lookup;
import de.root1.simon.Registry;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.NameBindingException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Logger;

import static common.i18n.BundleStrings.USER_MESSAGES;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 21:57
 */
public class GameServer extends Observable implements ClosedListener {
  private static Logger LOGGER = LoggingUtility.getLogger(GameServer.class.getName());

  private static GameServer gameServer;

  private Integer port;

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
    Simon.setDefaultKeepAliveInterval(5);
    Simon.setDefaultKeepAliveTimeout(5);
  }

  /* Methods */

  /**
   * Starts the server and sets a password.
   * @param password Server password.
   * @throws GameServerException Will be thrown and delivers the appropriate user message if
   * the server can not be started for a reason.
   */
  public void startServer(String password) throws GameServerException {
    if(!isServerRunning()) {
      final String name = GameConfigurationConstants.REGISTRY_NAME_SERVER;
      durakServices = new DurakServices(password);

      try {
        registry = Simon.createRegistry(port);
        registry.bind(name, durakServices);
      } catch (NameBindingException e) {
        LOGGER.warning("Name \"" + name + "\"already bound: " + e.getMessage());
        throw new GameServerException(I18nSupport.getValue(USER_MESSAGES, "service.already.running"));
      } catch (UnknownHostException e) {
        LOGGER.warning("Could not find ip address: "+e.getMessage());
        throw new GameServerException(I18nSupport.getValue(USER_MESSAGES, "network.error"));
      } catch (IOException e) {
        LOGGER.severe("I/O exception: " + e.getMessage());
        throw new GameServerException(I18nSupport.getValue(USER_MESSAGES, "address.might.be.used"));
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
      removeAllClients();
      registry.unbind(GameConfigurationConstants.REGISTRY_NAME_SERVER);
      registry.stop();
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
   * Stops the game. The boolean parameter specifies, if the game was canceled or properly
   * finished. Anyway, if the game is running and the method is called, the clients will be
   * notified that the game was canceled.
   * @param canceled If true, every client will be notified that it was canceled, else just finished.
   * @param reason String that will be send to the client if the game was canceled.
   */
  public void stopGame(boolean canceled, String reason) {
    boolean wasRunning = gameUpdate.stopGame(false);

    if(wasRunning)
      broadcastMessage(GameUpdateType.GAME_CANCELED, reason);
    else if(!canceled)
      broadcastMessage(GameUpdateType.GAME_FINISHED);
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
    List<Callable> callableList = gameUpdate.getRemoteReferences();
    broadcastMessage(type, callableList, sendingObject);
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
   * @param callables Remote references to send the message to.
   * @param sendingObject Sending object to send.
   */
  public void broadcastMessage(Enum<?> type, Collection<Callable> callables,
                               Object sendingObject) {
    for (Callable callable : callables) {
      sendMessage(callable, new MessageObject(type, sendingObject));
    }
  }

  /* sends to each client a list with all the other logged in clients */
  void broadcastOtherClients(Enum<?> type) {
    broadcastOtherClients(type, gameUpdate.getRemoteReferences());
  }

  void broadcastOtherClients(Enum<?> type, Collection<Callable> callables) {
    final List<DTOClient> clients = new ArrayList<DTOClient>();
    final List<Callable> callableList = new ArrayList<Callable>();
    DTOClient currentClient;

    /* iterate the clients so that the original sequence is not changed */
    Miscellaneous.addAllToCollection(clients, gameUpdate.getClients(callables));
    Miscellaneous.addAllToCollection(callableList, callables);
    for (Callable callable : callableList) {
      currentClient = gameUpdate.getClient(callable);
      final int index = Miscellaneous.findIndex(clients,currentClient,
          Miscellaneous.CLIENT_COMPARATOR);
      /* remove the client, send the rest of the list to all clients and add the current
       * client back to the list */
      clients.remove(index);
      sendMessage(callable, new MessageObject(type, clients));
      clients.add(index,currentClient);
    }
  }

  public void sendMessage(Callable callable, MessageObject messageObject) {
    callable.callback(messageObject);
  }

  /**
   * Sends a MessageObject object to all clients. For each client the MessageObject
   * object contains as sendingObject the corresponding object of the list
   * {@code sendingObjects}.
   * @param type Type of the message.
   * @param clients Clients to send the messages to.
   * @param sendingObjects Sending objects for the clients.
   * @throws GameServerException {@code clients} and {@code sendingObjects} have different sizes.
   */
  @SuppressWarnings("UnusedDeclaration")
  public void broadcastArray(Enum<?> type, Collection<Callable> clients,
                             Collection<?> sendingObjects)
      throws GameServerException {
    if(sendingObjects.size() != clients.size())
      throw new GameServerException("The number of clients and sending objects is not equal!");

    final Iterator<Callable> callableIterator = clients.iterator();
    final Iterator<?> objects = sendingObjects.iterator();
    while (callableIterator.hasNext()) {
      sendMessage(callableIterator.next(), new MessageObject(type, objects.next()));
    }
  }

  /**
   * Adds the client to the server list and notifies the gui and all clients.
   * @param callable Client remote reference.
   * @param client The client.
   * @return Returns true if client was added, else false.
   */
  boolean addClient(Callable callable, DTOClient client) {
    if(gameUpdate.addClient(callable, client)) {
      notifyClientLists(callable);
    } else return false;

    getClientLookup(callable).addClosedListener(callable, this);
    LOGGER.info("Added client: "+client);

    return true;
  }

  private Lookup getClientLookup(Callable callable) {
    final InetSocketAddress address = Simon.getRemoteInetSocketAddress(callable);
    return Simon.createNameLookup(address.getAddress(), address.getPort());
  }

  /**
   * Stops the game session and with it all remote references and registered listeners will be
   * removed.
   */
  private void removeAllClients() {
    final List<Callable> callables = gameUpdate.getRemoteReferences();
    for (Callable callable : callables)
      getClientLookup(callable).removeClosedListener(callable, this);
    gameUpdate.stopSession();
    setChangedAndNotify(GUIObserverType.CLIENT_LIST);
  }
  
  /**
   * Removes a client from the server and notifies the gui and all clients.
   * @param callable Client remote reference.
   * @return Returns true if the client was removed, else false.
   */
  boolean removeClient(Callable callable) {
    final DTOClient client = getClient(callable);
    if(gameUpdate.removeClient(callable)) {
      if(!client.spectating) {
        stopGame(true,
            I18nSupport.getValue(USER_MESSAGES, "game.canceled.player.0.logged.off", client.name));
      }
      notifyClientLists(null);
    } else return false;

    getClientLookup(callable).removeClosedListener(callable, this);
    LOGGER.info("Removed client: "+client);
    return true;
  }

  /**
   * Notifies the server gui and all clients which clients are currently in the list.
   * The first parameter is the remote object of the client that was added.
   * If this parameter is null, all clients and the server gui will
   * be notified. The second parameter will then be ignored.
   */
  private void notifyClientLists(Callable addedCallable) {
    setChangedAndNotify(GUIObserverType.CLIENT_LIST);
    if(addedCallable != null) {
      DTOClient client = gameUpdate.getClient(addedCallable);
      sendMessage(addedCallable, new MessageObject(MessageType.OWN_CLIENT_INFO, client));
    }
    broadcastOtherClients(BroadcastType.LOGIN_LIST);
  }

  /**
   * Validates an action by calling the game updater.
   * @param callable Client remote reference.
   * @param action Attached GameAction object.
   * @throws RuleException Will be thrown if the client broke a game rule.
   */
  void validateAction(Callable callable, GameAction action) throws RuleException {
    boolean nextRound = gameUpdate.getProcess().validateAction(
        action, gameUpdate.getPlayerID(callable));
    if(gameUpdate.updateMove(nextRound))
      stopGame(false, "");
  }

  /**
   * Updates the client and and all the lists if necessary.
   * @param callable Client remote reference
   * @param client Client information object.
   */
  public void updateClient(Callable callable, DTOClient client) {
    final DTOClient oldClient = gameUpdate.updateClientInformation(callable, client);
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

  /**
   * Called when a client connection was improperly closed.
   */
  public void closed() {
    /* refresh the clients */
    gameUpdate.refreshClients();
    setChangedAndNotify(GUIObserverType.CLIENT_LIST);
  }

  public void setPassword(String password) {
    durakServices.setPassword(password);
  }

  /* Getter and Setter */

  public void setPort(int port) {
    if(!isServerRunning())
      this.port = port;
  }

  @SuppressWarnings("UnusedDeclaration")
  public Integer getPort() {
    return port;
  }

  public boolean isServerRunning() {
    return registry != null && registry.isRunning();
  }

  public List<DTOClient> getClients() {
    return gameUpdate.getClients();
  }

  public DTOClient getClient(Callable callable) {
    return gameUpdate.getClient(callable);
  }
}

/******************************************/
/********** Simon Implementation **********/
/******************************************/
@SimonRemote(value={ServerInterface.class})
class DurakServices implements ServerInterface {
  private static final Logger LOGGER = LoggingUtility.getLogger(DurakServices.class.getName());

  private String password;

  DurakServices(String password) {
    this.password = password;
  }

  public boolean login(Callable callable, DTOClient client, String password) {
    boolean result = false;
    final GameServer server = GameServer.getServerInstance();

    if(this.password.equals(password)) {
      if(!server.clientNameExists(client.name))
        result = server.addClient(callable, client);
      else server.sendMessage(callable, new MessageObject(MessageType.STATUS_MESSAGE,
          I18nSupport.getValue(USER_MESSAGES, "status.name.0.already.exists", client.name)));
    } else {
      server.sendMessage(callable, new MessageObject(MessageType.STATUS_MESSAGE,
          I18nSupport.getValue(USER_MESSAGES,"status.permission.denied")));
    }

    return result;
  }

  public void logoff(Callable callable) {
    GameServer.getServerInstance().removeClient(callable);
  }

  public void sendChatMessage(Callable callable, String message) {
    final DTOClient client = GameServer.getServerInstance().getClient(callable);
    if(client != null) {
      ChatMessage chatMessage = new ChatMessage(System.currentTimeMillis(), client, message);
      GameServer.getServerInstance().broadcastMessage(BroadcastType.CHAT_MESSAGE, chatMessage);
    }
  }

  public boolean doAction(Callable callable, GameAction action) {
    boolean actionDone = false;
    final GameServer server = GameServer.getServerInstance();
    try {
      server.validateAction(callable, action);
      actionDone = true;
    } catch (RuleException e) {
      LOGGER.info("User \'" + action.getExecutor().name
          + "\' breaks the rules with action " + action);
      server.sendMessage(callable,
          new MessageObject(MessageType.RULE_MESSAGE, e.getMessage()));
    }

    return actionDone;
  }

  public void updateClient(Callable callable, DTOClient client) {
    GameServer.getServerInstance().updateClient(callable, client);
  }

  /* Getter and Setter */

  public void setPassword(String password) {
    this.password = password;
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getPassword() {
    return password;
  }
}

@SuppressWarnings("unchecked")
class GameUpdate {
  private Logger LOGGER = LoggingUtility.getLogger(GameUpdate.class.getName());
  private GameProcess<Integer> process;
  private GameServer server = null;
  private IngameSpectatorHolder<Callable,DTOClient> clientHolder;

  public GameUpdate() {
    this.clientHolder = new IngameSpectatorHolder<Callable, DTOClient>();
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
   * Gets the player's ID for the surpassed callable calling
   * {@link #getPlayerID(common.dto.DTOClient)}.
   * @param callable Surpassed client remote reference.
   * @return Should return a unique integer for this object.
   */
  public Integer getPlayerID(Callable callable) {
    return getPlayerID(getClient(callable));
  }

  /**
   * Invokes a game if it is not running already and if there are
   * enough players.
   * @param stackSize Stack size for the game.
   * @return Returns true, if the game was invoked, else false.
   */
  public boolean invokeGame(Integer stackSize) {
    boolean invoked = false;
    Integer cardsPerColour = stackSize / GameCardConstants.CardColour.values().length;
    if (!process.isGameInProcess() && process.getPlayerCount() > 1) {
      process.initialiseNewGame(cardsPerColour);
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
   * already registered players will be either deleted from the list or not. (This means the
   * process will be totally reset to the initial state.)
   * @param deletePlayers If true, players will be deleted.
   * @return True, game was running, else false.
   */
  public boolean stopGame(boolean deletePlayers) {
    boolean stopped = process.isGameInProcess();

    if(deletePlayers)
      process.reInitialise();
    else process.stopProcess();

    return stopped;
  }

  /**
   * Adds the client to the list if it dow not already exists.
   * @param callable Client remote reference.
   * @param client The client.
   * @return True if the client was added, else false.
   */
  boolean addClient(Callable callable, DTOClient client) {
    if(clientHolder.containsKey(callable))
      return false;

    /* Create a local reference of the DTOClient object on the server */
    final DTOClient localClient = new DTOClient("");
    localClient.setClientInfo(client);

    if(process.isGameInProcess() || localClient.spectating) {
      localClient.spectating = true;
      clientHolder.addSpectator(callable, localClient);
    } else {
      clientHolder.addInGameValue(callable,localClient);
      process.setPlayer(getPlayerID(localClient));
    }

    return true;
  }

  /**
   * Removes a client from the list if it exists and cancels the game if necessary.
   * @param callable Remote reference of the client.
   * @return True if the client was removed, else false.
   */
  boolean removeClient(Callable callable) {
    /* Client's player reference has also to be removed and if it was a player */
    /* In every case delete afterwards the client remote reference */
    process.removePlayer(getPlayerID(callable));
    return clientHolder.removeKey(callable);
  }

  /**
   * Changes the client information depending on the remote reference and returns the old
   * information. The lists will also be updated if necessary.
   * @param callable Remote reference of the client.
   * @param client The new client information.
   * @return Returns the old information.
   */
  public DTOClient updateClientInformation(Callable callable, DTOClient client) {
    DTOClient oldClient = getClient(callable);
    removeClient(callable);
    addClient(callable, client);
    return oldClient;
  }

  private void sendClientInit() {
    gameUpdateClients();
    server.broadcastMessage(GameUpdateType.STACK_UPDATE, Converter.toDTO(process.getStack()));
    informIngamePlayers();
    informSpectators();
  }

  /* Returns true, if there is a client that has changed. */
  private boolean gameUpdateClients() {
    boolean changed = false;
    for (DTOClient client : clientHolder.getInGameValues()) {
      final int cardCount = process.getPlayerCards(getPlayerID(client)).size();
      final PlayerConstants.PlayerType type = process.getPlayerType(getPlayerID(client));
      changed = changed || (cardCount != client.cardCount)  || (type != client.playerType);
      client.cardCount = cardCount;
      client.playerType = type;
    }
    return changed;
  }

  /**
   * Updates all players and spectators with the necessary data.
   * @param nextRound True, the process will go to the next round. False, the process updates
   *                  the current round.
   * @return True, the game has finished, else false.
   */
  public boolean updateMove(boolean nextRound) {
    if(nextRound) {
      if(!process.goToNextRound()) {
        LOGGER.warning("Couldn't go to next round! GameProcess#readyForNextRound: "
            + process.readyForNextRound());
      } else updateNextRound();
    } else updateCurrentRound();

    return process.gameHasFinished();
  }

  private void updateNextRound() {
    if(gameUpdateClients()) {
      informIngamePlayers();
      /* update player list */
      server.broadcastMessage(GameUpdateType.PLAYERS_UPDATE,
          Collections.list(Collections.enumeration(clientHolder.getInGameValues())));
    }
    informSpectators();
    server.broadcastMessage(GameUpdateType.STACK_UPDATE, Converter.toDTO(process.getStack()));
    sendNextRoundInfo(true, process.defenderTookCards(), false);
  }

  private void updateCurrentRound() {
    /* update player list */
    if(gameUpdateClients()) {
      server.broadcastMessage(GameUpdateType.PLAYERS_UPDATE,
          Collections.list(Collections.enumeration(clientHolder.getInGameValues())));
    }

    /* update ingame cards */
    if(process.cardsHaveChanged())
      server.broadcastMessage(GameUpdateType.IN_GAME_CARDS,
          Converter.toDTO(process.getAttackCards(), process.getDefenseCards()));

    sendNextRoundInfo(false, false, process.attackersReady());
  }

  private void sendNextRoundInfo(boolean nextRound, boolean defenderTookCards,
                                 boolean attackersFinished) {
    final List<Boolean> roundInfo = new ArrayList<Boolean>(3);
    roundInfo.add(nextRound);
    roundInfo.add(defenderTookCards);
    roundInfo.add(attackersFinished);
    server.broadcastMessage(GameUpdateType.NEXT_ROUND_INFO, roundInfo);
  }
  /**
   * Sends each inGame client his player info and all opponent's info
   * (number of cards, name, etc...)
   */
  private void informIngamePlayers() {
    for (Callable callable : clientHolder.getInGameKeys()) {
      final DTOClient client = clientHolder.getInGameValue(callable);
      server.sendMessage(callable, new MessageObject(GameUpdateType.CLIENT_CARDS,
          process.getPlayerCards(getPlayerID(client))));
      server.sendMessage(callable, new MessageObject(MessageType.OWN_CLIENT_INFO, client));
    }
    /* inform ingame clients about opponents */
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

  /**
   * Refreshes the client list and eventually cancels the game.
   */
  public void refreshClients() {
    if(clientHolder.refresh()) {
      /* If somehow a client lost connection to the server, */
      /* the players in the process should be restored */
      if(clientHolder.getInGameKeys().size() != process.getPlayerCount()) {
        stopGame(true);
        /* add the deleted players back to the list */
        for (DTOClient client : clientHolder.getInGameValues())
          process.setPlayer(getPlayerID(client));
      }
    }
  }

  public DTOClient getClient(Callable callable) {
    return clientHolder.getValue(callable);
  }

  public List<DTOClient> getClients() {
    return clientHolder.getAllValues();
  }

  public List<Callable> getRemoteReferences() {
    return clientHolder.getAllKeys();
  }

  public GameProcess<Integer> getProcess() {
    return process;
  }

  public List<DTOClient> getClients(Collection<Callable> callables) {
    final List<DTOClient> clients = new ArrayList<DTOClient>(callables.size());
    for (Callable callable : callables) {
      clients.add(clientHolder.getValue(callable));
    }
    return clients;
  }
}

class IngameSpectatorHolder<K,V> {
  private static final Logger LOGGER = LoggingUtility.getLogger(IngameSpectatorHolder.class.getName());
  private Map<K,V> ingameMap;
  private Map<K,V> spectatorMap;

  /* Constructors */

  IngameSpectatorHolder() {
    ingameMap = new HashMap<K,V>(6);
    spectatorMap = new HashMap<K,V>(6);
  }

  /* Methods */

  public void addInGameValue(K key, V value) {
    spectatorMap.remove(key);
    ingameMap.put(key, value);
  }

  public void addSpectator(K key, V value) {
    ingameMap.remove(key);
    spectatorMap.put(key, value);
  }

  public boolean removeKey(K key) {
    return  (ingameMap.remove(key) != null) || (spectatorMap.remove(key) != null);
  }

  /* Getter and Setter */

  /* Returns a copy of all values as one list */
  public List<V> getAllValues() {
    final List<V> values = new ArrayList<V>(ingameMap.size()+spectatorMap.size());
    Miscellaneous.addAllToCollection(values, ingameMap.values());
    Miscellaneous.addAllToCollection(values, spectatorMap.values());
    return values;
  }

  /* Returns a copy of all keys as one list */
  public List<K> getAllKeys() {
    final List<K> keys = new ArrayList<K>(ingameMap.size()+spectatorMap.size());
    Miscellaneous.addAllToCollection(keys, ingameMap.keySet());
    Miscellaneous.addAllToCollection(keys, spectatorMap.keySet());
    return keys;
  }

  public V getInGameValue(K key) {
    return ingameMap.get(key);
  }

  @SuppressWarnings("UnusedDeclaration")
  public V getSpectatorValue(K key) {
    return spectatorMap.get(key);
  }

  public V getValue(K key) {
    if(ingameMap.containsKey(key))
      return ingameMap.get(key);
    else if(spectatorMap.containsKey(key))
      return spectatorMap.get(key);
    else return null;
  }

  public Collection<K> getInGameKeys() {
    return ingameMap.keySet();
  }

  public Collection<K> getSpectatorKeys() {
    return spectatorMap.keySet();
  }

  public Collection<V> getInGameValues() {
    return ingameMap.values();
  }

  @SuppressWarnings("UnusedDeclaration")
  public Collection<V> getSpectatorValues() {
    return spectatorMap.values();
  }

  public boolean containsKey(K key) {
    return ingameMap.containsKey(key) || spectatorMap.containsKey(key);
  }

  public void clear() {
    ingameMap.clear();
    spectatorMap.clear();
  }

  /**
   * Returns true if something was changed. Otherwise false.
   */
  public boolean refresh() {
    boolean changed = false;
    Map<K,V> refreshedMap = getRefreshedMap(ingameMap, "ingameMap"); //NON-NLS
    if(refreshedMap.size() != ingameMap.size()) {
      ingameMap = refreshedMap;
      changed = true;
    }

    refreshedMap = getRefreshedMap(spectatorMap, "spectatorMap"); //NON-NLS
    if(refreshedMap.size() != spectatorMap.size()) {
      spectatorMap = refreshedMap;
      changed = true;
    }
    return changed;
  }

  private Map<K,V> getRefreshedMap(Map<K,V> map, String fieldName) {
    final Map<K,V> refreshedMap = new HashMap<K, V>(map.size());
    for (K k : map.keySet()) {
      try {
        refreshedMap.put(k,map.get(k));
      } catch (Exception e) {
        LOGGER.warning("Could not access to a key in "+fieldName);
      }
    }
    return refreshedMap;
  }
}