package client.gui.frame;

import client.gui.widget.card.OpponentHandWidget;
import common.dto.DTOClient;
import common.resources.ResourceGetter;
import common.utilities.Miscellaneous;
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
  /* Methods */

  public void addOpponent(DTOClient opponent) {
    OpponentHandWidget oHWidget = new OpponentHandWidget(
        ClientGUIConstants.OPPONENT_FONT, ClientGUIConstants.CARD_BACK, opponent);
    this.add(oHWidget);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void removeOpponent(DTOClient opponent) {
    final OpponentHandWidget widget = findOpponentHandWidget(opponent);
    if(widget != null)
      this.remove(widget);
  }

  private OpponentHandWidget findOpponentHandWidget(DTOClient info) {
    for (Component component : getComponents()) {
      final OpponentHandWidget widget = (OpponentHandWidget) component;
      final DTOClient opponent = widget.getOpponent();
      if(Miscellaneous.CLIENT_COMPARATOR.compare(opponent,info) == 0)
        return widget;
    }

    return null;
  }

  public void updateOpponents(List<DTOClient> opponents) {
    for (DTOClient opponent : opponents) {
      final OpponentHandWidget widget = findOpponentHandWidget(opponent);
      if(widget != null) {
        widget.getOpponent().cardCount = opponent.cardCount;
        setOpponentStatusIcon(widget, opponent.playerType);
      }
    }
    repaint();
  }

  public void setOpponentStatusIcon(OpponentHandWidget widget, PlayerConstants.PlayerType type) {
    final Integer height;
    if(PlayerConstants.PlayerType.LOSER.equals(type))
      height = getPreferredSize().height;
    else height = null;
    final ImageIcon statusIcon = ResourceGetter.getPlayerTypeIcon(type, height);
    widget.setStatusIcon(statusIcon, type.getDescription());
  }

  public void removeAllOpponents() {
    this.removeAll();
    this.validate();
    this.repaint();
  }
}
