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
import static common.i18n.BundleStrings.GUI_COMPONENT;
import static common.utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 04:48
 */
public class AttackerPanel extends AbstractDurakGamePanel {
  private boolean firstAttacker;

  private JPanel opponentsButtonsPanel;
  private JButton roundDoneButton;
  private boolean alreadyPressed;

  /* Constructors */

  public AttackerPanel(boolean firstAttacker) {
    this.firstAttacker = firstAttacker;

    setLayout(new BorderLayout());

    init();

    add(getOpponentsButtonsPanel(), BorderLayout.PAGE_START);
    add(getCardStackContainer(), BorderLayout.LINE_START);
    add(getGameProcessContainer(), BorderLayout.CENTER);
  }

  /* Methods */

  public void init() {
    super.init();

    alreadyPressed = false;

    /* Game Proccess Container */
    getGameProcessContainer().setHandCardsVisible(true);

    /* Game Button Panel */
    JPanel panel = getGameButtonsContainer();
    roundDoneButton = WidgetCreator.makeButton(null,
        I18nSupport.getValue(GUI_COMPONENT, "text.finish.round"),
        I18nSupport.getValue(GUI_COMPONENT, "tooltip.finish.round"), null,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (GameClient.getClient().finishRound(Client.getOwnInstance().toDTO(),
                FinishAction.FinishType.GO_TO_NEXT_ROUND)) {
              /* The player is not allowed to do a card move */
              getGameProcessContainer().setListenerType(PlayerConstants.PlayerType.DEFAULT);
              alreadyPressed = true;
            }
          }
        });
    enableGameButtons(false, false);

    panel.setLayout(new GridLayout());
    panel.add(roundDoneButton);
  }

  public void enableGameButtons(boolean roundFinished, boolean attackerFinished) {
    roundDoneButton.setEnabled(!alreadyPressed && !roundFinished
        && getGameProcessContainer().hasInGameCards() && !attackerFinished);
  }

  /**
   * Resets the layout and displays to a default state for a client with the
   * PlayerType PlayerType.FIRST_ATTACKER or PlayerType.SECOND_ATTACKER. The reset
   * concerns the resets for the game, so that the client list and the opponent
   * widgets, etc...will be untouched.
   */
  public void setNewRound() {
    getGameProcessContainer().setIngameCards(null, null);
    setPlayerType(firstAttacker);
    alreadyPressed = false;
    enableGameButtons(false, false);
  }

  /* Getter and Setter */

  private PlayerType getPlayerType() {
    return firstAttacker ? PlayerType.FIRST_ATTACKER : PlayerType.SECOND_ATTACKER;
  }

  public void setPlayerType(boolean firstAttacker) {
    this.firstAttacker = firstAttacker;
    getGameProcessContainer().setListenerType(getPlayerType());
  }

  private JPanel getOpponentsButtonsPanel() {
    if (opponentsButtonsPanel != null)
      return opponentsButtonsPanel;

    opponentsButtonsPanel = new JPanel();

    final JPanel buttonPanel = getGameButtonsContainer();
    buttonPanel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, OPPONENT_PANEL_HEIGHT));
    buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());

    opponentsButtonsPanel.setLayout(new BorderLayout());
    opponentsButtonsPanel.add(buttonPanel, BorderLayout.LINE_START);
    opponentsButtonsPanel.add(getOpponentsContainer(), BorderLayout.CENTER);

    return opponentsButtonsPanel;
  }
}
