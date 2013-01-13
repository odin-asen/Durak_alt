package client.gui.frame.playerTypePanel;

import client.business.Client;
import client.business.client.GameClient;
import common.i18n.I18nSupport;
import common.simon.action.FinishAction;
import common.utilities.constants.PlayerConstants;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static client.gui.frame.ClientGUIConstants.CARD_STACK_PANEL_WIDTH;
import static client.gui.frame.ClientGUIConstants.OPPONENT_PANEL_HEIGHT;
import static common.utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 04:48
 */
public class AttackerPanel extends DurakCentrePanelImpl {
  private boolean firstAttacker;

  private JPanel opponentsButtonsPanel;
  private JPanel stackClientsPanel;
  private JButton roundDoneButton;

  /* Constructors */

  public AttackerPanel(boolean firstAttacker) {
    this.firstAttacker = firstAttacker;

    setLayout(new BorderLayout());

    init();

    add(getOpponentsButtonsPanel(), BorderLayout.PAGE_START);
    add(getStackClientsPanel(), BorderLayout.LINE_START);
    add(getGameProcessContainer(), BorderLayout.CENTER);
    add(getStatusBarContainer(), BorderLayout.PAGE_END);
  }

  /* Methods */

  public void init() {
    super.init();

    /* Game Proccess Container */
    getGameProcessContainer().setHandCardsVisible(true);

    /* Game Button Panel */
    JPanel panel = getGameButtonsContainer();
    roundDoneButton = WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.finish.round"),
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.finish.round"), null,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (GameClient.getClient().finishRound(Client.getOwnInstance().toDTO(),
                FinishAction.FinishType.GO_TO_NEXT_ROUND)) {
              /* The player is not allowed to do a card move */
              getGameProcessContainer().setListenerType(PlayerConstants.PlayerType.DEFAULT);
              roundDoneButton.setEnabled(false);
            }
          }
        });
    roundDoneButton.setEnabled(false);

    panel.setLayout(new GridLayout());
    panel.add(roundDoneButton);
  }

  public void enableGameButtons(boolean roundFinished) {
    roundDoneButton.setEnabled(!roundFinished && getGameProcessContainer().hasInGameCards());
  }

  /**
   * Resets the layout and displays to a default state for a client with the
   * PlayerType PlayerType.FIRST_ATTACKER or PlayerType.SECOND_ATTACKER. The reset
   * concernes the resets for the game, so that the client list and the opponent
   * widgets, etc...will be untouched.
   */
  public void setNewRound() {
    getOpponentsContainer().removeAllOpponents();
    getCardStackContainer().deleteCards();
    getGameProcessContainer().setIngameCards(null, null);
    setPlayerType(firstAttacker);
  }

  /* Getter and Setter */

  private PlayerType getPlayerType() {
    return firstAttacker ? PlayerType.FIRST_ATTACKER : PlayerType.SECOND_ATTACKER;
  }

  public void setPlayerType(boolean firstAttacker) {
    this.firstAttacker = firstAttacker;
    getGameProcessContainer().setListenerType(getPlayerType());
    getStatusBarContainer().setPlayerType(getPlayerType());
  }

  private JPanel getOpponentsButtonsPanel() {
    if (opponentsButtonsPanel != null)
      return opponentsButtonsPanel;

    opponentsButtonsPanel = new JPanel();

    final JPanel buttonPanel = getGameButtonsContainer();
    buttonPanel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, OPPONENT_PANEL_HEIGHT));
    buttonPanel.setMaximumSize(new Dimension(CARD_STACK_PANEL_WIDTH, Integer.MAX_VALUE));

    opponentsButtonsPanel.setLayout(new BoxLayout(opponentsButtonsPanel, BoxLayout.LINE_AXIS));
    opponentsButtonsPanel.add(buttonPanel);
    opponentsButtonsPanel.add(getOpponentsContainer());

    return opponentsButtonsPanel;
  }

  private JPanel getStackClientsPanel() {
    if (stackClientsPanel != null)
      return stackClientsPanel;

    stackClientsPanel = new JPanel();

    stackClientsPanel.setLayout(new BoxLayout(stackClientsPanel, BoxLayout.PAGE_AXIS));
    stackClientsPanel.add(getCardStackContainer());
    stackClientsPanel.add(getClientListContainer());

    return stackClientsPanel;
  }
}
