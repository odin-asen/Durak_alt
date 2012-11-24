package client.gui.frame;

import client.gui.widget.card.OpponentHandWidget;
import common.dto.ClientInfo;
import common.resources.ResourceGetter;
import common.utilities.constants.PlayerConstants;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:12
 */
public class OpponentsPanel extends JPanel {
  /* Constructors */
  public OpponentsPanel() {
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
  }

  /* Methods */
  public void addOpponent(ClientInfo opponent) {
    OpponentHandWidget oHWidget = new OpponentHandWidget(
        ClientGUIConstants.OPPONENT_FONT, ClientGUIConstants.CARD_BACK, opponent);
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
      if(widget.getOpponent().isEqual(info))
        return widget;
    }

    return null;
  }

  public void updateOpponents(List<ClientInfo> opponents) {
    for (ClientInfo opponent : opponents) {
      final OpponentHandWidget widget = findOpponentHandWidget(opponent);
      if(widget != null) {
        widget.getOpponent().cardCount = opponent.cardCount;
        setOpponentStatusIcon(widget, opponent.playerType);
      }
    }
    repaint();
  }

  public void setOpponentStatusIcon(OpponentHandWidget widget, PlayerConstants.PlayerType type) {
    final ImageIcon statusIcon = ResourceGetter.getPlayerTypeIcon(type, null);
    widget.setStatusIcon(statusIcon, type.getDescription());
  }

  public void deleteCards() {
    this.removeAll();
    this.validate();
    this.repaint();
  }
}
