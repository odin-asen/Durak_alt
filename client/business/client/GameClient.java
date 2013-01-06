package client.business.client;

import client.business.ConnectionInfo;
import common.dto.DTOClient;
import common.dto.DTOCard;
import common.dto.message.MessageObject;
import common.rmi.*;
import common.utilities.LoggingUtility;
import common.utilities.Miscellaneous;
import common.utilities.constants.PlayerConstants;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
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
  private static GameClient client;

  public static final String DEFAULT_IP_ADDRESS = "127.0.0.1"; //NON-NLS
  public static final Integer DEFAULT_PORT = Registry.REGISTRY_PORT;
  private static final Logger LOGGER =
      LoggingUtility.getLogger(GameClient.class.getName());

  private Map<RMIService,Remote> services;

  private String serverAddress;
  private String clientAddress;
  private Integer serverPort;
  private Integer clientPort;
  private Boolean connected;
  private RMIObserverImpl observer;

  /* Constructors */
  public static GameClient getClient() {
    if (client == null) {
      client = new GameClient();
    }
    return client;
  }

  private GameClient() {
    this.serverAddress = DEFAULT_IP_ADDRESS;
    this.serverPort = DEFAULT_PORT;
    this.clientAddress = DEFAULT_IP_ADDRESS;
    this.clientPort = DEFAULT_PORT;
    this.connected = false;
    services = new HashMap<RMIService,Remote>();
  }

  /* Methods */
  private void lookupService(Registry registry, RMIService service)
      throws RemoteException, NotBoundException {
    final Remote remote = registry.lookup(service.getServiceName(serverAddress));
    services.put(service, remote);
  }

  private void initServices(Registry registry) throws RemoteException, NotBoundException {
    lookupService(registry, RMIService.ATTACK_ACTION);
    lookupService(registry, RMIService.AUTHENTICATION);
    lookupService(registry, RMIService.CHAT);
    lookupService(registry, RMIService.DEFENSE_ACTION);
    lookupService(registry, RMIService.ROUND_STATE_ACTION);
    lookupService(registry, RMIService.OBSERVABLE);
  }

  public void receiveServerMessage(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  public void setChangedAndNotify(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  public void connect(DTOClient info, String password)
      throws GameClientException {
    if (!isConnected()) {
      try {
        Registry serverRegistry = LocateRegistry.getRegistry(serverAddress, serverPort);
        Registry clientRegistry = Miscellaneous.getSafeRegistry(clientPort);

        observer = new RMIObserverImpl();
        Remote stub = UnicastRemoteObject.exportObject((Remote) observer, 0);
        clientRegistry.rebind(RMIService.OBSERVER.getServiceName(clientAddress), stub);

        initServices(serverRegistry);

        connected = getAuthenticator().login(info, password);
        if(!connected)
          throw new GameClientException(getAuthenticator().getRefusedReason());
      } catch (RemoteException e) {
        LOGGER.severe("RemoteException occured while connecting: " + e.getMessage());
        throw new GameClientException("Could not connect to the server! Please be sure that it is running.");
      } catch (NotBoundException e) {
        LOGGER.severe("NotBoundException occured while connecting: "+ e.getMessage());
        throw new GameClientException("Could not establish a connection. The server is not running!");
      }
    }
  }

  public void disconnect(DTOClient client) {
    if(isConnected()) {
      try {
        connected = false;
        getAuthenticator().logoff(client);
        Miscellaneous.getSafeRegistry(clientPort).unbind(RMIService.OBSERVER.getServiceName(clientAddress));
        UnicastRemoteObject.unexportObject(observer, true);
      } catch (RemoteException e) {
        LOGGER.info(e.getMessage());
      } catch (NotBoundException e) {
        LOGGER.info(e.getMessage());
      }
    }
  }

  /**
   * For attacks the card sequence is not important. If a defense request
   * is send, the card sequence is first the defense card, second the attacker card.
   * @param info Client information that will be send to the server.
   * @param cards Used cards for this action.
   * @return True or false, if the server accepts this action or not.
   * @throws RemoteException
   */
  public Boolean sendAction(DTOClient info, DTOCard... cards) throws RemoteException {
    if(info.playerType.equals(PlayerConstants.PlayerType.FIRST_ATTACKER) ||
       info.playerType.equals(PlayerConstants.PlayerType.SECOND_ATTACKER))
      return getGameActionAttack().doAction(info, FinishAction.NOT_FINISHING, cards);
    else
      return info.playerType.equals(PlayerConstants.PlayerType.DEFENDER) &&
          getGameActionDefend().doAction(info, FinishAction.NOT_FINISHING, cards);
  }

  public Boolean finishRound(DTOClient info, Boolean takeCards) throws RemoteException {
    final FinishAction finish;
    if(takeCards)
      finish = FinishAction.TAKE_CARDS;
    else finish = FinishAction.GO_TO_NEXT_ROUND;

    return getGameActionRound().doAction(info, finish);
  }

  public String getActionDeniedReason(DTOClient info) throws RemoteException {
    if(info.playerType.equals(PlayerConstants.PlayerType.FIRST_ATTACKER) ||
        info.playerType.equals(PlayerConstants.PlayerType.SECOND_ATTACKER))
      return getGameActionAttack().getRefusedReason();
    else if(info.playerType.equals(PlayerConstants.PlayerType.DEFENDER))
      return getGameActionDefend().getRefusedReason();
    else return null;
  }

  /* Getter and Setter */
  public String getSocketAddress() {
    return serverAddress + ":" + serverPort;
  }

  public void setConnection(ConnectionInfo info) {
    serverAddress = info.getServerAddress();
    serverPort = info.getServerPort();
    clientAddress = info.getClientAddress();
    clientPort = info.getClientPort();
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

  private RMIObservable getRMIObservable() {
    return (RMIObservable) services.get(RMIService.OBSERVABLE);
  }
}

class RMIObserverImpl implements RMIObserver {
  public void update(Object parameter) throws RemoteException {
    if(parameter instanceof MessageObject) {
      GameClient.getClient().receiveServerMessage((MessageObject) parameter);
    }
  }
}