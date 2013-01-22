package client.gui.frame.playerTypePanel;

import client.business.Client;
import client.business.client.GameClient;
import client.gui.frame.ClientFrame;
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

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 05:31
 */
public class DefenderPanel extends AbstractDurakGamePanel {
  private JPanel opponentsButtonsPanel;
  private JButton takeCardsButton;
  private JButton roundDoneButton;

  /* Constructors */

  public DefenderPanel() {
    setLayout(new BorderLayout());

    init();

    add(getOpponentsButtonsPanel(), BorderLayout.PAGE_START);
    add(getCardStackContainer(), BorderLayout.LINE_START);
    add(getGameProcessContainer(), BorderLayout.CENTER);
  }

  /* Methods */

  public void init() {
    super.init();

    /* Game Proccess Container */
    getGameProcessContainer().setHandCardsVisible(true);

    /* Game Button Panel */
    JPanel panel = getGameButtonsContainer();
    takeCardsButton = WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.take.cards"),
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.take.cards"), null,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            GameClient.getClient().finishRound(Client.getOwnInstance().toDTO(),
                FinishAction.FinishType.TAKE_CARDS);
          }
        });
    roundDoneButton = WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.finish.round"),
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.finish.round"), null,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            GameClient.getClient().finishRound(Client.getOwnInstance().toDTO(),
                FinishAction.FinishType.GO_TO_NEXT_ROUND);
          }
        });
    enableGameButtons(false, false);

    panel.setLayout(new GridLayout(0,1));
    panel.add(takeCardsButton);
    panel.add(roundDoneButton);
  }

  public void enableGameButtons(boolean roundFinished, boolean attackerFinished) {
    final boolean cardsOnTable = getGameProcessContainer().hasInGameCards();
    final boolean allCardsCovered = getGameProcessContainer().inGameCardsAreCovered();
    final boolean roundCanBeFinished = !roundFinished && attackerFinished && allCardsCovered;
    if (roundCanBeFinished)
      ClientFrame.getInstance().showGamePopup(
          I18nSupport.getValue(MSGS_BUNDLE, "next.round.available"));
    takeCardsButton.setEnabled(!roundFinished && cardsOnTable);
    roundDoneButton.setEnabled(roundCanBeFinished);
  }

  /**
   * Resets the layout and displays to a default state for a client with the
   * PlayerType PlayerType.DEFENDER. The reset concernes the resets for the game, so that
   * the client list and the opponent widgets, etc... will be untouched.
   */
  public void setNewRound() {
    getGameProcessContainer().setIngameCards(null, null);
    getGameProcessContainer().setListenerType(PlayerConstants.PlayerType.DEFENDER);
    enableGameButtons(false, false);
  }

  /* Getter and Setter */

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
}

