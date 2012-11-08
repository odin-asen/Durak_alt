package dto;

import utilities.constants.PlayerConstants;

import java.io.Serializable;
import java.util.List;

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

  public void setClientInfo(ClientInfo info) {
    this.cardCount = info.cardCount;
    this.name = info.name;
    this.type = info.type;
    this.loginNumber = info.loginNumber;
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

  public void printInfo() {
    System.out.println("Name: "+name);
    System.out.println("Login number: "+loginNumber);
    System.out.println("Player type: "+type);
    System.out.println("Card count: "+cardCount);
  }

  /**
   * Returns true, if a ClientInfo object in {@code list} and this
   * objects isEqual call returns also true. If no matching object
   * can be found, this method returns false.
   * @param list List with ClientInfo objects.
   * @return Returns true or false, if a matching client could be found.
   */
  public Boolean containsIsEqual(List<ClientInfo> list) {
    for (ClientInfo clientInfo : list) {
      if(this.isEqual(clientInfo))
        return true;
    }

    return false;
  }
}
