package server.business;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 19:33
 *
 * This class represents the gaming process of one round. One round means,
 * that one player is in his turn the defender and the others are the attackers.
 */
public class PlayingRound {
  private List<Player> playerList;
  private List<Player> attackers;
  private Player defender;

  /* Constructors */
  public PlayingRound(List<Player> playerList) {
    this.playerList = playerList;
    this.attackers = new ArrayList<Player>(2);
    defender = null;
  }

  /* Methods */

  /* Getter and Setter */
  public List<Player> getPlayerList() {
    return playerList;
  }

  public void setPlayerList(List<Player> playerList) {
    this.playerList = playerList;
  }

  public List<Player> getAttackers() {
    return attackers;
  }

  public void setAttackers(List<Player> attackers) {
    this.attackers = attackers;
    if(attackers.contains(defender))
      defender = null;
  }

  public Player getDefender() {
    return defender;
  }

  public void setDefender(Player defender) {
    this.defender = defender;
    if (defender != null) {
      this.defender.setDefending(true);
    }

    if(attackers.contains(defender))
      attackers.remove(defender);
  }
}
