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
   * PlayerType PlayerType.DEFAULT.
   */
  public void resetAll() {
    getOpponentsContainer().removeAllOpponents();
    getCardStackContainer().deleteCards();
    getGameProcessContainer().placeInGameCards(null, null);
    getGameProcessContainer().placeClientCards(null);
    updateClients(null);
    getStatusBarContainer().setConnected(false);
    getStatusBarContainer().setPlayerType(PlayerConstants.PlayerType.DEFAULT);
    getStatusBarContainer().setText("");
  }

  /* Getter and Setter */

//  private JPanel getOpponentButtonPanel() {
//    final JPanel panel = new JPanel();
//    final JPanel buttonPanel = getButtonPanel();
//
//    buttonPanel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, OPPONENT_PANEL_HEIGHT));
//    buttonPanel.setMaximumSize(new Dimension(CARD_STACK_PANEL_WIDTH, Integer.MAX_VALUE));
//    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
//    panel.add(buttonPanel);
//    panel.add(opponentsPanel);
//
//    return panel;
//  }

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
