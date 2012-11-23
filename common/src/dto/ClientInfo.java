package dto;

import utilities.constants.PlayerConstants;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 12.10.12
 * Time: 17:12
 */
public class ClientInfo implements Serializable {
  public Short loginNumber;
  public String name;
  public int cardCount;
  public boolean spectating;
  public PlayerConstants.PlayerType playerType;

  /* Constructors */
  public ClientInfo(String name, Short loginNumber) {
    this.name = name;
    this.cardCount = 0;
    this.loginNumber = loginNumber;
    this.playerType = PlayerConstants.PlayerType.DEFAULT;
    this.spectating = false;
  }

  /* Methods */
  public String toString() {
    return name +" - "+ loginNumber;
  }

  public boolean isEqual(ClientInfo info) {
    return this.loginNumber.equals(info.loginNumber);
  }

  public void setClientInfo(ClientInfo info) {
    cardCount = info.cardCount;
    name = info.name;
    playerType = info.playerType;
    loginNumber = info.loginNumber;
    spectating = info.spectating;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  public void printInfo(OutputStream output) {
    final PrintStream out = new PrintStream(output, true);
    out.println("Name: " + name);
    out.println("Login number: "+loginNumber);
    out.println("Player playerType: "+ playerType);
    out.println("Card count: "+cardCount);
    out.println("Spectator: "+spectating);
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
