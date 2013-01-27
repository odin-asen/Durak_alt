package client.business.client;

import common.dto.DTOCard;
import common.dto.DTOClient;
import common.dto.message.MessageObject;
import common.dto.message.MessageType;
import common.i18n.I18nSupport;
import common.simon.Callable;
import common.simon.ServerInterface;
import common.simon.action.CardAction;
import common.simon.action.FinishAction;
import common.simon.action.GameAction;
import common.utilities.LoggingUtility;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.constants.PlayerConstants;
import de.root1.simon.ClosedListener;
import de.root1.simon.Lookup;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.logging.Logger;

import static common.i18n.BundleStrings.USER_MESSAGES;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 01:37
 */
public class GameClient extends Observable implements ClosedListener {
  private static GameClient client;

  private static final Logger LOGGER =
      LoggingUtility.getLogger(GameClient.class.getName());

  private boolean connected;

  private ServerMessageReceiver messageReceiver;
  private Lookup nameLookup;
  private ServerInterface server;

  /* Constructors */
  public static GameClient getClient() {
    if (client == null) {
      client = new GameClient();
    }
    return client;
  }

  private GameClient() {
    this.connected = false;
    messageReceiver = new ServerMessageReceiver();
    Simon.setDefaultKeepAliveInterval(5);
    Simon.setDefaultKeepAliveTimeout(5);
  }

  /* Methods */

  public void receiveServerMessage(MessageObject object) {
    setChangedAndNotify(object);
  }

  public void setChangedAndNotify(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  /**
   * Connects the GameClient to the server.
   * @param serverAddress Address of the server to connect to.
   * @param serverPort Port of the server to connect to.
   * @param password Specifies the password that a server might need.
   * @param dtoClient Specifies the client representation that will be logged in at the server.
   * @throws GameClientException Thrown when a connection could not be established. A user
   * message will be delivered with the Exception.
   */
  public boolean connect(String serverAddress, int serverPort, String password,
                         DTOClient dtoClient) throws GameClientException {
    if (connected)
      return connected;

    try {
      nameLookup = Simon.createNameLookup(serverAddress, serverPort);
      server = (ServerInterface) nameLookup.lookup(
          GameConfigurationConstants.REGISTRY_NAME_SERVER);
      nameLookup.addClosedListener(server, this);
      connected = server.login(messageReceiver, dtoClient, password);
      LOGGER.info(LoggingUtility.STARS+" Connected to "+getSocketAddress()
          +" "+LoggingUtility.STARS);
    } catch (UnknownHostException e) {
      LOGGER.warning("Failed connection try to " + getSocketAddress());
      throw new GameClientException(I18nSupport.getValue(USER_MESSAGES, "server.0.not.found",
          getSocketAddress()));
    } catch (LookupFailedException e) {
      LOGGER.warning("LookupFailedException occurred while connection: "+e.getMessage());
      throw new GameClientException(I18nSupport.getValue(USER_MESSAGES, "could.not.find.service"));
    } catch (EstablishConnectionFailed e) {
      LOGGER.warning("EstablishConnectionFailed occurred while connecting: " + e.getMessage());
      throw new GameClientException(I18nSupport.getValue(USER_MESSAGES, "server.0.not.found",
          getSocketAddress()));
    }
    return connected;
  }

  /**
   * Disconnects the client if necessary and connects it to a given connection.
   * The connection parameter will also be set in the client.
   * @param serverAddress Server's address to connect to.
   * @param serverPort Server's port to connect to.
   * @param dtoClient Specifies the client representation that will be logged in at the server.
   * @param password Specifies the password that a server might need.
   * @return Returns a boolean value that shows if the client is connected or not.
   * @throws GameClientException Thrown when a connection could not be established. A user
   * message will be delivered with the Exception.
   */
  public boolean reconnect(String serverAddress, Integer serverPort,
                           DTOClient dtoClient, String password) throws GameClientException {
    if(connected) {
      disconnect(false);
      try { /* Wait for SIMON after a disconnection before it will be connected. */
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        LOGGER.warning("Thread sleep failed: "+e.getMessage());
      }
    }
    /* setup a new connection */
    return connect(serverAddress, serverPort, password, dtoClient);
  }

  public void disconnect(boolean shutdown) {
    if(connected) {
      connected = false;
      if (!shutdown) {
        server.logoff(messageReceiver);
        nameLookup.release(server);
        LOGGER.info(LoggingUtility.STARS+" Disconnected from "+getSocketAddress()
            +" "+LoggingUtility.STARS);
      }
    }
  }

  /**
   * Sends an action request to the server.
   * @param dtoClient Client information that will be send to the server.
   *                  The player is decides the action type.
   * @param attackCards Used attack cards for this action.
   * @param defenseCards Used defense cards for this action.
   * @return True or false, if the server accepts this action or not.
   */
  public boolean sendAction(DTOClient dtoClient, List<DTOCard> attackCards,
                            List<DTOCard> defenseCards) {
    if(dtoClient.playerType.equals(PlayerConstants.PlayerType.FIRST_ATTACKER) ||
       dtoClient.playerType.equals(PlayerConstants.PlayerType.SECOND_ATTACKER)) {
      GameAction action = new CardAction(CardAction.CardActionType.ATTACK,
          attackCards, defenseCards, dtoClient, GameAction.ActionType.CARD_ACTION);
      return server.doAction(messageReceiver, action);
    } else {
      GameAction action = new CardAction(CardAction.CardActionType.DEFENSE,
          attackCards, defenseCards, dtoClient, GameAction.ActionType.CARD_ACTION);
      return server.doAction(messageReceiver, action);
    }
  }

  /**
   * Overloads {@link GameClient#sendAction(common.dto.DTOClient, java.util.List, java.util.List)}.
   */
  public boolean sendAction(DTOClient dtoClient, DTOCard attackCard, DTOCard defenseCard) {
    final List<DTOCard> attackCards = new ArrayList<DTOCard>(1);
    final List<DTOCard> defenseCards = new ArrayList<DTOCard>(1);
    attackCards.add(attackCard);
    defenseCards.add(defenseCard);
    return sendAction(dtoClient, attackCards, defenseCards);
  }

  public boolean finishRound(DTOClient dtoClient, FinishAction.FinishType type) {
    return server.doAction(messageReceiver,
      new FinishAction(type,dtoClient, GameAction.ActionType.ROUND_REQUEST));
  }

  public void sendChatMessage(String text) {
    server.sendChatMessage(messageReceiver, text);
  }

  public void sendClientUpdate(DTOClient dtoClient) {
    if(connected)
      server.updateClient(messageReceiver, dtoClient);
  }

  /**
   * Called when the server closed improperly.
   */
  public void closed() {
    if(connected) {
      nameLookup.release(server);
      LOGGER.info(LoggingUtility.STARS + " Lost server connection " + LoggingUtility.STARS);
      connected = false;
      setChangedAndNotify(new MessageObject(MessageType.LOST_CONNECTION));
    }
  }

  /* Getter and Setter */

  public String getSocketAddress() {
    if(nameLookup != null)
      return nameLookup.getServerAddress().getHostAddress() + ":" + nameLookup.getServerPort();
    else return I18nSupport.getValue(USER_MESSAGES, "no.address");
  }

  public boolean isConnected() {
    return connected;
  }
}

@SimonRemote(value = {Callable.class})
class ServerMessageReceiver implements Callable {
  public void callback(Object parameter) {
    if(parameter instanceof MessageObject) {
      GameClient.getClient().receiveServerMessage((MessageObject) parameter);
    }
  }
}