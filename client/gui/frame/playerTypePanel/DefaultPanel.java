package client.gui.frame.playerTypePanel;

import common.utilities.constants.PlayerConstants;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 01:59
 */
public class DefaultPanel extends DurakCentrePanelImpl {
  private JPanel stackClientsPanel;

  /* Constructors */

  public DefaultPanel() {
    setLayout(new BorderLayout());

    init();

    add(getOpponentsContainer(), BorderLayout.PAGE_START);
    add(getStackClientsPanel(), BorderLayout.LINE_START);
    add(getGameProcessContainer(), BorderLayout.CENTER);
    add(getStatusBarContainer(), BorderLayout.PAGE_END);
  }

  /* Methods */

  public void enableGameButtons(boolean roundFinished) {
    /* Nothing to do */
  }

  /**
   * Resets the layout and displays to a default state for a client with the
   * PlayerType PlayerType.DEFAULT. The reset concernes the resets for the game, so that
   * the client list and the opponent widgets, etc... will be untouched.
   */
  public void setNewRound() {
    getGameProcessContainer().setIngameCards(null, null);
    getStatusBarContainer().setPlayerType(PlayerConstants.PlayerType.DEFAULT);
  }

  /* Getter and Setter */

  private JPanel getStackClientsPanel() {
    if(stackClientsPanel != null)
      return stackClientsPanel;

    stackClientsPanel = new JPanel();

    stackClientsPanel.setLayout(new BoxLayout(stackClientsPanel, BoxLayout.PAGE_AXIS));
    stackClientsPanel.add(getCardStackContainer());
    stackClientsPanel.add(getClientListContainer());

    return stackClientsPanel;
  }
}
