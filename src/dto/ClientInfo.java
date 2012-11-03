package dto;

import utilities.constants.PlayerConstants;

import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 12.10.12
 * Time: 17:12
 */
public class ClientInfo implements Serializable {
  private Short loginNumber;
  private String name;
  private int cardCount;
  private PlayerConstants.PlayerType type;

  /* Constructors */
  public ClientInfo(String name, Short loginNumber) {
    this.name = name;
    this.cardCount = 0;
    this.loginNumber = loginNumber;
    this.type = PlayerConstants.PlayerType.DEFAULT;
  }

  /* Methods */
  public String toString() {
    return name +" - "+ loginNumber;
  }

  public boolean isEqual(ClientInfo info) {
    return this.loginNumber.equals(info.loginNumber);
  }

  /* Getter and Setter */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCardCount() {
    return cardCount;
  }

  public void setCardCount(int cardCount) {
    this.cardCount = cardCount;
  }

  public PlayerConstants.PlayerType getPlayerType() {
    return type;
  }

  public void setPlayerType(PlayerConstants.PlayerType type) {
    this.type = type;
  }

  public void setLoginNumber(Short loginNumber) {
    this.loginNumber = loginNumber;
  }

  public Short getLoginNumber() {
    return loginNumber;
  }
}
