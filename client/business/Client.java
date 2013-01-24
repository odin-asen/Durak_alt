package client.business;

import common.dto.DTOClient;
import common.i18n.I18nSupport;
import common.utilities.constants.PlayerConstants;

/**
 * User: Timm Herrmann
 * Date: 06.01.13
 * Time: 15:54
 *
 * This class is a singleton class to be sure that each program can only create one
 * client object. The Client class represents the information of the user that is accessable to
 * all other clients that are connected to the same server.
 */
public class Client {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static Client ownClient;

  private String name;
  private Integer cardCount;
  private Boolean spectating;
  private PlayerConstants.PlayerType playerType;

  /* Constructors */

  private Client() {
    String name = System.getProperty("user.name", //NON-NLS
        I18nSupport.getValue(CLIENT_BUNDLE, "default.player.name"));
    setName(name);
    setCardCount(0);
    setPlayerType(PlayerConstants.PlayerType.DEFAULT);
    setSpectating(false);
  }

  public static Client getOwnInstance() {
    if (ownClient == null) {
      ownClient = new Client();
    }
    return ownClient;
  }

  /* Methods */

  public void setClient(DTOClient info) {
    cardCount = info.cardCount;
    name = info.name;
    playerType = info.playerType;
    spectating = info.spectating;
  }

  /* Getter and Setter */

  public Integer getCardCount() {
    return cardCount;
  }

  public void setCardCount(Integer cardCount) {
    if(cardCount < 0 || cardCount == null)
      cardCount = 0;
    this.cardCount = cardCount;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if(name == null)
      name = "";
    this.name = name;
  }

  public PlayerConstants.PlayerType getPlayerType() {
    return playerType;
  }

  public void setPlayerType(PlayerConstants.PlayerType playerType) {
    this.playerType = playerType;
  }

  public Boolean getSpectating() {
    return spectating;
  }

  public void setSpectating(Boolean spectating) {
    if(spectating == null)
      spectating = true;
    this.spectating = spectating;
  }

  public DTOClient toDTO() {
    final DTOClient client = new DTOClient(name);
    client.cardCount = cardCount;
    client.playerType = playerType;
    client.spectating = spectating;
    return client;
  }

  /* Sets only from the server changeble values of the clients information */
  public void setClientInfo(DTOClient dtoClient) {
    setCardCount(dtoClient.cardCount);
    setPlayerType(dtoClient.playerType);
    setSpectating(dtoClient.spectating);
  }
}
