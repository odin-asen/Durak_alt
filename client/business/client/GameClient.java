package client.business.client;

import common.dto.DTOCard;
import common.dto.DTOClient;
import common.dto.message.MessageObject;
import common.i18n.I18nSupport;
import common.simon.Callbackable;
import common.simon.ServerInterface;
import common.simon.action.CardAction;
import common.simon.action.FinishAction;
import common.simon.action.GameAction;
import common.utilities.LoggingUtility;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.constants.PlayerConstants;
import de.root1.simon.Lookup;
import de.root1.simon.Simon;
import de.root1.simon.annotation.SimonRemote;
import de.root1.simon.exceptions.EstablishConnectionFailed;
import de.root1.simon.exceptions.LookupFailedException;

import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 01:37
 */
public class GameClient extends Observable {
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS
  private static GameClient client;

  private static final Logger LOGGER =
      LoggingUtility.getLogger(GameClient.class.getName());

  private String address;
  private Integer port;
  private Boolean connected;

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
    this.address = GameConfigurationConstants.DEFAULT_IP_ADDRESS;
    this.port = GameConfigurationConstants.DEFAULT_PORT;
    this.connected = false;
    messageReceiver = new ServerMessageReceiver();
  }

  /* Methods */

  public void receiveServerMessage(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  public void setChangedAndNotify(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  public void connect(DTOClient dtoClient, String password)
      throws GameClientException {
    if (!isConnected()) {
      try {
        nameLookup = Simon.createNameLookup(address, port);
        server = (ServerInterface) nameLookup.lookup(
            GameConfigurationConstants.REGISTRY_NAME_SERVER);
        connected = server.login(messageReceiver, dtoClient, password);
      } catch (UnknownHostException e) {
        LOGGER.info("Failed connection try to " + address + ":" + port);
        throw new GameClientException(I18nSupport.getValue(MSGS_BUNDLE, "server.0.not.found",
            address +":"+ port));
      } catch (LookupFailedException e) {
        LOGGER.severe("LookupFailedException occured while connection: "+e.getMessage());
        throw new GameClientException(I18nSupport.getValue(MSGS_BUNDLE, "could.not.find.service"));
      } catch (EstablishConnectionFailed e) {
        LOGGER.severe("EstablishConnectionFailed occured while connecting: " + e.getMessage());
        throw new GameClientException(I18nSupport.getValue(MSGS_BUNDLE, "could.not.find.service"));
      }
    }
  }

  public void disconnect() {
    if(isConnected()) {
      server.logoff(messageReceiver);
      nameLookup.release(server);
      connected = false;
    }
  }

  /**
   * For attacks the card sequence is not important. If a defense request
   * is send, the card sequence is first the defense card, second the attacker card.
   * @param dtoClient Client information that will be send to the server.
   * @param cards Used cards for this action.
   * @return True or false, if the server accepts this action or not.
   */
  public Boolean sendAction(DTOClient dtoClient, List<DTOCard> attackCards,
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
  public Boolean sendAction(DTOClient dtoClient, DTOCard attackCard, DTOCard defenseCard) {
    final List<DTOCard> attackCards = new ArrayList<DTOCard>(1);
    final List<DTOCard> defenseCards = new ArrayList<DTOCard>(1);
    return sendAction(dtoClient, attackCard, defenseCard);
  }

  public Boolean finishRound(DTOClient dtoClient, FinishAction.FinishType type) {
    return server.doAction(messageReceiver,
      new FinishAction(type,dtoClient, GameAction.ActionType.ROUND_REQUEST));
  }

  public void sendChatMessage(String text) {
    server.sendChatMessage(messageReceiver, text);
  }

  /* Getter and Setter */
  public String getSocketAddress() {
    return address + ":" + port;
  }

  public void setConnection(String serverAddress, int serverPort) {
    this.address = serverAddress;
    this.port = serverPort;
  }

  public Boolean isConnected() {
    return connected;
  }
}

@SimonRemote(value = {Callbackable.class})
class ServerMessageReceiver implements Callbackable {
  public void callback(Object parameter) {
    if(parameter instanceof MessageObject) {
      GameClient.getClient().receiveServerMessage((MessageObject) parameter);
    }
  }
}