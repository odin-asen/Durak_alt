package client.gui.frame.playerTypePanel;

import client.business.Client;
import client.business.client.GameClient;
import common.dto.DTOClient;
import common.i18n.I18nSupport;
import common.simon.action.FinishAction;
import common.utilities.constants.PlayerConstants;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static common.utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 04:48
 */
public class AttackerPanel extends DurakCentrePanelImpl {
  private boolean firstAttacker;

  private JPanel stackClientsPanel;
  private JButton roundDoneButton;

  /* Constructors */

  public AttackerPanel(boolean firstAttacker) {
    this.firstAttacker = firstAttacker;

    setLayout(new BorderLayout());

    init();

    add(getOpponentsContainer(), BorderLayout.PAGE_START);
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
    //    takeCardsButton = WidgetCreator.makeButton(null,
//        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.take.cards"),
//        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.take.cards"), null,
//        new ActionListener() {
//          public void actionPerformed(ActionEvent e) {
//            if (nextRoundRequest(true)) {
//              takeCardsButton.setEnabled(false);
//              roundDoneButton.setEnabled(false);
//              update.updateGamePanel(new ArrayList<DTOCard>(), new ArrayList<DTOCard>(), null);
//            }
//          }
//        });
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
//    takeCardsButton.setEnabled(false);
    roundDoneButton.setEnabled(false);

    panel.setLayout(new GridLayout());
    panel.add(roundDoneButton);
  }

//  private Boolean nextRoundRequest(/*Boolean takeCards*/) {
//    if (takeCards == null)
//      return false;

//    final DTOClient dtoClient = Client.getOwnInstance().toDTO();
//    FinishAction.FinishType type = FinishAction.FinishType.GO_TO_NEXT_ROUND;
    /*if (dtoClient.playerType.equals(PlayerConstants.PlayerType.DEFENDER)) {
      if (takeCards)
        type = FinishAction.FinishType.TAKE_CARDS;
      return GameClient.getClient().finishRound(dtoClient, type);
    } else if (dtoClient.playerType.equals(PlayerConstants.PlayerType.FIRST_ATTACKER) ||
        dtoClient.playerType.equals(PlayerConstants.PlayerType.SECOND_ATTACKER)) {*/
//      if (GameClient.getClient().finishRound(dtoClient, type)) {
//        /* The player is not allowed to do a card move */
//        gamePanel.setListenerType(PlayerConstants.PlayerType.DEFAULT);
//        return true;
//      }
//    }

//    return false;
//  }

  public void enableGameButtons(boolean roundFinished) {
//    Boolean /*take,*/ round = false;
    final PlayerConstants.PlayerType playerType = Client.getOwnInstance().getPlayerType();
    final Boolean cardsOnTable = getGameProcessContainer().hasInGameCards();
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
//    takeCardsButton.setEnabled(take && cardsOnTable);
    roundDoneButton.setEnabled(!roundFinished && cardsOnTable);
  }

  /**
   * Resets the layout and displays to a default state for a client with the
   * PlayerType PlayerType.FIRST_ATTACKER or PlayerType.SECOND_ATTACKER.
   */
  public void resetAll() {
    getOpponentsContainer().removeAllOpponents();
    getCardStackContainer().deleteCards();
    getGameProcessContainer().placeInGameCards(null, null);
    getGameProcessContainer().placeClientCards(null);
    updateClients(null);
    getStatusBarContainer().setConnected(false);
    getStatusBarContainer().setText("");
    setPlayerType(firstAttacker);
  }

  /* Getter and Setter */

  public void setPlayerType(boolean firstAttacker) {
    this.firstAttacker = firstAttacker;
    if(firstAttacker) {
      getGameProcessContainer().setListenerType(PlayerType.FIRST_ATTACKER);
      getStatusBarContainer().setPlayerType(PlayerType.DEFAULT);
    } else {
      getGameProcessContainer().setListenerType(PlayerType.SECOND_ATTACKER);
      getStatusBarContainer().setPlayerType(PlayerType.SECOND_ATTACKER);
    }
  }

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
