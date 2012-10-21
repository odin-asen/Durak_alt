package client.gui.frame;

import client.gui.widget.card.OpponentHandWidget;
import dto.ClientInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:12
 */
public class OpponentsPanel extends JPanel {
  private static final Logger LOGGER = Logger.getLogger(OpponentsPanel.class.getName());

  /* Constructors */
  public OpponentsPanel() {
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
  }

  /* Methods */
  public void addOpponent(ClientInfo opponent) {
    OpponentHandWidget oHWidget = new OpponentHandWidget(ClientGUIConstants.CARD_BACK, opponent);
    this.add(oHWidget);
  }

  public void removeOpponent(ClientInfo opponent) {
    final OpponentHandWidget widget = findOpponentHandWidget(opponent);
    if(widget != null)
      this.remove(widget);
  }

  private OpponentHandWidget findOpponentHandWidget(ClientInfo info) {
    for (Component component : getComponents()) {
      final OpponentHandWidget widget = (OpponentHandWidget) component;
      if(widget.getOpponent().equalsID(info))
        return widget;
    }

    return null;
  }

  public void updateOpponents(List<ClientInfo> opponents) {
    for (ClientInfo opponent : opponents) {
      final OpponentHandWidget widget = findOpponentHandWidget(opponent);
      if(widget != null)
        widget.getOpponent().setCardCount(opponent.getCardCount());
      else LOGGER.log(Level.INFO, opponent+" konnte nicht gefunden werden!");
    }
  }
}
