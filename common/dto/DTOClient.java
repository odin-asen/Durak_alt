package common.dto;

import common.utilities.constants.GameConfigurationConstants;
import common.utilities.constants.PlayerConstants;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 12.10.12
 * Time: 17:12
 *
 * The ClientInfo class represents a client with some informations like the name, the
 * number of cards, if it is spectating a match or the network information
 * (ip address, port). The toString()-method uses the ip address and port as unique string
 * representation, hence there should be only one device in a network that runs a server
 * connected to a server once a time.
 */
public class DTOClient implements Serializable {
  public String name;
  public int cardCount;
  public boolean spectating;
  public PlayerConstants.PlayerType playerType;

  /* Constructors */
  public DTOClient(String name) {
    this.name = name;
    this.cardCount = 0;
    this.playerType = PlayerConstants.PlayerType.DEFAULT;
    this.spectating = false;
  }

  /* Methods */

  public void setClientInfo(DTOClient info) {
    cardCount = info.cardCount;
    name = info.name;
    playerType = info.playerType;
    spectating = info.spectating;
  }

  @SuppressWarnings("ALL")
  public void printInfo(OutputStream output) {
    final PrintStream out = new PrintStream(output, true);
    out.println("name: " + name);
    out.println("playerType: " + playerType);
    out.println("cardCount: " +cardCount);
    out.println("spectating: " +spectating);
  }
}
