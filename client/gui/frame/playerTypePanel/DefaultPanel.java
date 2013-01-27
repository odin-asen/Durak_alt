package client.gui.frame.playerTypePanel;

import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 01:59
 */
public class DefaultPanel extends AbstractDurakGamePanel {
  /* Constructors */

  public DefaultPanel() {
    setLayout(new BorderLayout());

    init();

    add(getOpponentsContainer(), BorderLayout.PAGE_START);
    add(getCardStackContainer(), BorderLayout.LINE_START);
    add(getGameProcessContainer(), BorderLayout.CENTER);
  }

  /* Methods */

  public void enableGameButtons(boolean roundFinished, boolean attackerFinished) {
    /* Nothing to do */
  }

  /**
   * Resets the layout and displays to a default state for a client with the
   * PlayerType PlayerType.DEFAULT. The reset concerns the resets for the game, so that
   * the client list and the opponent widgets, etc... will be untouched.
   */
  public void setNewRound() {
    getGameProcessContainer().setIngameCards(null, null);
  }

  /* Getter and Setter */
}
