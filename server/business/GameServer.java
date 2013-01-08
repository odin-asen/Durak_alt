package server.business;

import common.dto.DTOCard;
import common.dto.DTOClient;
import common.dto.message.*;
import common.game.GameCardStack;
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
import java.net.InetSocketAddress;
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
  private InGameSpectatorHolder<Callbackable,DTOClient> clientHolder;
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
    this.clientHolder = new InGameSpectatorHolder<Callbackable, DTOClient>();
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
    }
  }

  public void shutdownServer() {
    if(isServerRunning()) {
      broadcastMessage(BroadcastType.SERVER_SHUTDOWN);
      registry.unbind(GameConfigurationConstants.REGISTRY_NAME_SERVER);
      registry.stop();
      running = false;
    }
  }

  public void startGame(Integer stackSize) {
    if(!GameProcess.getInstance().isGameInProcess()) {
      gameUpdater = new GameUpdater(stackSize, clientHolder);
      gameUpdater.invoke();
    }
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
    List<Callbackable> clients = clientHolder.getAllKeys();
    for (Callbackable client : clients) {
      sendMessage(client,new MessageObject(type, sendingObject));
    }
  }

  public void sendProcessUpdate(boolean nextRound) {
    gameUpdater.updateMove(nextRound);
  }

  public void broadcastMessage(Enum<?> type) {
    broadcastMessage(type, null);
  }

  public void sendMessage(Callbackable callbackable, MessageObject messageObject) {
    InetSocketAddress socketAddress = Simon.getRemoteInetSocketAddress(callbackable);
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
   * Adds the client to the server list if it does not exist already.
   * @param client The client.
   * @return Returns true if the client was added, else false.
   */
  boolean addClient(Callbackable callbackable, DTOClient client) {
    if(clientHolder.containsKey(callbackable))
      return false;

    final Boolean inProcess = GameProcess.getInstance().isGameInProcess();

    if(inProcess || client.spectating) {
      clientHolder.addSpectator(callbackable,client);
    } else {
      clientHolder.addInGameValue(callbackable,client);
      GameProcess.getInstance().setPlayer(client.toString()); //TODO für Player eine andere ID finden als DTOClient.toString()
    }
    notifyClientLists(callbackable, client);

    return true;
  }

  void removeClient(Callbackable callbackable) {
    final GameProcess process = GameProcess.getInstance();
    final DTOClient client = clientHolder.getValue(callbackable);
    if(process.removePlayer(client.toString())) {
      if(process.isGameInProcess())
        process.abortGame();
      clientHolder.removeKey(callbackable);
      GameServer.getServerInstance().notifyClientLists(null,null);
    }
  }

  /**
   * Notifies the server gui and all clients which clients are currently in the list.
   * The first parameter is the remote object of the client that was added.
   * If this parameter is null, all clients and the server gui will
   * be notified. The second parameter will then be ignored.
   */
  private void notifyClientLists(Callbackable callbackable, DTOClient addedClient) {
    setChangedAndNotify(GUIObserverType.REFRESH_CLIENT_LIST);
    if(callbackable != null)
      sendMessage(callbackable, new MessageObject(MessageType.OWN_CLIENT_INFO, addedClient));
    broadcastOtherClients(BroadcastType.LOGIN_LIST);
  }

  /* sends to each client a list with all the other logged in clients */
  void broadcastOtherClients(Enum<?> type) {
    final List<DTOClient> clients = clientHolder.getAllValues();
    DTOClient currentClient;

    /* iterate the clients so that the original sequence is not changed */
    for (Callbackable rmiObserver : clientHolder.getAllKeys()) {
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
    return clientHolder.getAllValues();
  }

  public DTOClient getClient(Callbackable callbackable) {
    return clientHolder.getValue(callbackable);
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
      result = server.addClient(callbackable, client);
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
    final GameProcess process = GameProcess.getInstance();
    final GameServer server = GameServer.getServerInstance();
    try {
      boolean nextRound = process.validateAction(action, action.getExecutor().toString());
      server.sendProcessUpdate(nextRound);
      actionDone = true;
    } catch (RuleException e) {
      LOGGER.info("User \'" + action.getExecutor().name
          + "\' breaks the rules with action " + action);
      server.sendMessage(callbackable,
          new MessageObject(MessageType.RULE_MESSAGE, e.getMessage()));
    }

    return actionDone;
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
  private Integer stackSize;
  private GameProcess process;
  private GameServer server;
  private InGameSpectatorHolder<Callbackable,DTOClient> clientHolder;

  public GameUpdater(Integer stackSize,
                     InGameSpectatorHolder<Callbackable,DTOClient> clientHolder) {
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
    server.broadcastMessage(GameUpdateType.STACK_UPDATE, Converter.toDTO(process.getStack()));
    sendPlayersInfos();
    server.broadcastOtherClients(GameUpdateType.INITIALISE_PLAYERS);
  }

  private void updateClients() {
    for (DTOClient info : clientHolder.getAllValues()) {
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
    for (Callbackable rmiObserver : clientHolder.getInGameKeys()) {
      final DTOClient client = clientHolder.getInGameValue(rmiObserver);
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

  public boolean containsKey(K key) {
    return inGameMap.containsKey(key) || spectatorMap.containsKey(key);
  }
}