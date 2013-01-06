package client.business.client;

import common.dto.DTOClient;
import common.utilities.constants.PlayerConstants;

/**
 * User: Timm Herrmann
 * Date: 06.01.13
 * Time: 15:54
 *
 * This class is a singleton class to be sure that each program can only create one
 * client object.
 */
public class Client {
  private static Client ownClient;
  private static final String DEFAULT_IP_ADDRESS = "127.0.0.1";

  private String name;
  private Integer cardCount;
  private Boolean spectating;
  private PlayerConstants.PlayerType playerType;
  private String ipAddress;
  private Integer port;

  /* Constructors */

  private Client() {
    setName("");
    setCardCount(0);
    setPlayerType(PlayerConstants.PlayerType.DEFAULT);
    setSpectating(false);
    setIpAddress(DEFAULT_IP_ADDRESS);
    setPort(0);
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
    ipAddress = info.ipAddress;
    port = info.port;
  }

  /* Getter and Setter */

  public Integer getCardCount() {
    return cardCount;
  }

  public void setCardCount(int cardCount) {
    if(cardCount < 0)
      cardCount = 0;
    this.cardCount = cardCount;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    if(ipAddress == null)
      ipAddress = DEFAULT_IP_ADDRESS;
    this.ipAddress = ipAddress;
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

  public Integer getPort() {
    return port;
  }

  public void setPort(int port) {
    if(port < 0)
      port = 0;
    this.port = port;
  }

  public Boolean getSpectating() {
    return spectating;
  }

  public void setSpectating(boolean spectating) {
    this.spectating = spectating;
  }
}
