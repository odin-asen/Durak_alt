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
public class DefenderPanel extends DurakCentrePanelImpl {
  private JPanel opponentsButtonsPanel;
  private JPanel stackClientsPanel;
  private JButton takeCardsButton;
  private JButton roundDoneButton;

  /* Constructors */

  public DefenderPanel() {
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
    takeCardsButton = WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.take.cards"),
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.take.cards"), null,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (GameClient.getClient().finishRound(Client.getOwnInstance().toDTO(),
                FinishAction.FinishType.TAKE_CARDS)) {
              setNewRound();
            }
          }
        });
    roundDoneButton = WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.finish.round"),
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.finish.round"), null,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (GameClient.getClient().finishRound(Client.getOwnInstance().toDTO(),
                FinishAction.FinishType.GO_TO_NEXT_ROUND)) {
              setNewRound();
            }
          }
        });
    takeCardsButton.setEnabled(false);
    roundDoneButton.setEnabled(false);

    panel.setLayout(new GridLayout(0,1));
    panel.add(takeCardsButton);
    panel.add(roundDoneButton);
  }

  public void enableGameButtons(boolean roundFinished) {
    final Boolean cardsOnTable = getGameProcessContainer().hasInGameCards();
    final Boolean round = roundFinished && getGameProcessContainer().inGameCardsAreCovered();
    if (round)
      ClientFrame.getInstance().showInformationPopup(
          I18nSupport.getValue(MSGS_BUNDLE, "next.round.available"));
    takeCardsButton.setEnabled(cardsOnTable);
    roundDoneButton.setEnabled(!roundFinished && cardsOnTable);
  }

  /**
   * Resets the layout and displays to a default state for a client with the
   * PlayerType PlayerType.DEFENDER. The reset concernes the resets for the game, so that
   * the client list and the opponent widgets, etc... will be untouched.
   */
  public void setNewRound() {
    getOpponentsContainer().removeAllOpponents();
    getCardStackContainer().deleteCards();
    getGameProcessContainer().setIngameCards(null, null);
    getGameProcessContainer().setListenerType(PlayerConstants.PlayerType.DEFENDER);
    getStatusBarContainer().setPlayerType(PlayerConstants.PlayerType.DEFENDER);
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

