package client.gui.frame.playerTypePanel;

import javax.swing.*;
import java.awt.*;

import static client.gui.frame.ClientGUIConstants.CARD_STACK_PANEL_WIDTH;
import static client.gui.frame.ClientGUIConstants.OPPONENT_PANEL_HEIGHT;

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

    initParentPanels();

    add(getOpponentsContainer(), BorderLayout.PAGE_START);
    add(getStackClientsPanel(), BorderLayout.LINE_START);
    add(getGameProcessContainer(), BorderLayout.CENTER);
    add(getStatusBarContainer(), BorderLayout.PAGE_END);
  }

  /* Methods */

  private void initParentPanels() {
    /* Cards Stack Panel */
    JPanel panel = getCardStackContainer();

    panel.setLayout(new BorderLayout());
    panel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, panel.getPreferredSize().height));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    /* Opponents Panel */
    panel = getOpponentsContainer();
    panel.setPreferredSize(new Dimension(0, OPPONENT_PANEL_HEIGHT));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    /* Client List Panel */
    panel = getClientListContainer();
    panel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, 100));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
  }

//  private Boolean nextRoundRequest(Boolean takeCards) {
//    if(takeCards == null)
//      return false;
//
//    final DTOClient dtoClient = Client.getOwnInstance().toDTO();
//    FinishAction.FinishType type = FinishAction.FinishType.GO_TO_NEXT_ROUND;
//    if(dtoClient.playerType.equals(PlayerConstants.PlayerType.DEFENDER)) {
//      if(takeCards)
//        type = FinishAction.FinishType.TAKE_CARDS;
//      return GameClient.getClient().finishRound(dtoClient, type);
//    } else if(dtoClient.playerType.equals(PlayerConstants.PlayerType.FIRST_ATTACKER) ||
//        dtoClient.playerType.equals(PlayerConstants.PlayerType.SECOND_ATTACKER)) {
//      if(GameClient.getClient().finishRound(dtoClient, type)) {
//        /* The player is not allowed to do a card move */
//        gamePanel.setListenerType(PlayerConstants.PlayerType.DEFAULT);
//        return true;
//      }
//    }
//
//    return false;
//  }

  public void enableGameButtons(boolean roundFinished) {
    /* Nothing to do */
//    final Boolean take, round;
//    final PlayerConstants.PlayerType playerType = Client.getOwnInstance().getPlayerType();
//    final Boolean cardsOnTable = gamePanel.hasInGameCards();
//    if(playerType.equals(PlayerConstants.PlayerType.FIRST_ATTACKER) ||
//        playerType.equals(PlayerConstants.PlayerType.SECOND_ATTACKER)) {
//      take = false;
//      round = !roundFinished;
//    } else if (playerType.equals(PlayerConstants.PlayerType.DEFENDER)) {
//      take = true;
//      round = roundFinished && gamePanel.inGameCardsAreCovered();
//      if(round)
//        showInformationPopup(I18nSupport.getValue(MSGS_BUNDLE, "next.round.available"));
//    } else {
//      take = false;
//      round = false;
//    }
//    //TODO unterschiedliche oberflächen und auch messagehandler für beobachter und spieler machen, damit so ein scheiß wie in dieser methode nicht gemacht werden muss
//    takeCardsButton.setEnabled(take && cardsOnTable);
//    roundDoneButton.setEnabled(round && cardsOnTable);
  }

  public void resetAll() {
    //To change body of implemented methods use File | Settings | File Templates.
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
