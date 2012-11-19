package client.business.client;

import dto.ClientInfo;
import dto.DTOCard;
import dto.message.MessageObject;
import rmi.*;
import utilities.constants.PlayerConstants;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 01:37
 */
public class GameClient extends Observable {
  private static GameClient client;

  public static final String DEFAULT_SERVER_ADDRESS = "localhost";
  public static final Integer DEFAULT_SERVER_PORT = Registry.REGISTRY_PORT;

  private Map<RMIService,Remote> services;
  private ServerMessageHandler serverObserver;

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
    lookupServices(registry, RMIService.ROUND_STATE_ACTION);
    serverObserver = new ServerMessageHandler(registry, RMIService.OBSERVER.getServiceName());
  }

  public void receiveServerMessage(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  public void setChangedAndNotify(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  public boolean connect(ClientInfo info, String password)
      throws RemoteException, NotBoundException, ServerNotActiveException {
    if (!isConnected()) {
      Registry registry = LocateRegistry.getRegistry(serverAddress, port);
      initServices(registry);
      getRMIObserver().getServer().registerInterest(getRMIObserver());
      connected = true;
      return getAuthenticator().login(info, password);
    }

    return false;
  }

  public void disconnect(ClientInfo info) throws NotBoundException, RemoteException {
    if(isConnected()) {
      getAuthenticator().logoff(info);
      connected = false;
    }
  }

  private ServerMessageHandler getRMIObserver() {
    return serverObserver;
  }

  /**
   * For attacks the card sequence is not important. If a defense request
   * is send, the card sequence is first the defense card, second the attacker card.
   * @param info Client information that will be send to the server.
   * @param cards Used cards for this action.
   * @return True or false, if the server accepts this action or not.
   * @throws RemoteException
   */
  public Boolean sendAction(ClientInfo info, DTOCard... cards) throws RemoteException {
    if(info.playerType.equals(PlayerConstants.PlayerType.FIRST_ATTACKER) ||
       info.playerType.equals(PlayerConstants.PlayerType.SECOND_ATTACKER))
      return getGameActionAttack().doAction(info, FinishAction.NOT_FINISHING, cards);
    else if(info.playerType.equals(PlayerConstants.PlayerType.DEFENDER))
      return getGameActionDefend().doAction(info, FinishAction.NOT_FINISHING, cards);
    else return false;
  }

  public Boolean finishRound(ClientInfo info, Boolean takeCards) throws RemoteException {
    final FinishAction finish;
    if(takeCards)
      finish = FinishAction.TAKE_CARDS;
    else finish = FinishAction.GO_TO_NEXT_ROUND;

    return getGameActionRound().doAction(info, finish);
  }

  public String getActionDeniedReason(ClientInfo info) throws RemoteException {
    if(info.playerType.equals(PlayerConstants.PlayerType.FIRST_ATTACKER) ||
        info.playerType.equals(PlayerConstants.PlayerType.SECOND_ATTACKER))
      return getGameActionAttack().getRefusedReason();
    else if(info.playerType.equals(PlayerConstants.PlayerType.DEFENDER))
      return getGameActionDefend().getRefusedReason();
    else return null;
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

  private GameAction getGameActionAttack() {
    return (GameAction) services.get(RMIService.ATTACK_ACTION);
  }

  private GameAction getGameActionDefend() {
    return (GameAction) services.get(RMIService.DEFENSE_ACTION);
  }

  private GameAction getGameActionRound() {
    return (GameAction) services.get(RMIService.ROUND_STATE_ACTION);
  }
}