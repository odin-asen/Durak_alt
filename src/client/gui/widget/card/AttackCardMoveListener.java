package client.gui.widget.card;

import client.gui.frame.gamePanel.CombatCardPanel;
import client.gui.frame.gamePanel.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

/**
 * User: Timm Herrmann
 * Date: 05.11.12
 * Time: 00:52
 */
public class AttackCardMoveListener extends CardMoveListener {
  /* Constructors */
  protected AttackCardMoveListener(GamePanel parent) {
    super(parent);
  }

  /* Methods */
  private void addCardToPanel(GameCardWidget widget) {
    parent.removeCard(widget);
    parent.repaint();

    final CombatCardPanel panel = new CombatCardPanel();
    panel.setAttackerCard(widget);
    parent.addInGameCards(panel);
  }

  public void mouseReleased(MouseEvent e) {
    GameCardWidget widget = (GameCardWidget) e.getComponent();
    if(moveIsValid(widget)) {
      addCardToPanel(widget);
      widget.setCursor(Cursor.getDefaultCursor());
    } else {
      widget.setLocation(widget.getLastLocation());
      parent.setComponentZOrder(widget, widget.getLastZOrderIndex());
    }

    super.mouseReleased(e);
  }

  private Boolean moveIsValid(GameCardWidget widget) {
    return true;
//    try {
//      return GameClient.getClient().sendAction(SetUpFrame.getInstance().getClientInfo(),
//          Converter.toDTO(widget.getCardInfo()));
//    } catch (RemoteException e) {
//      e.printStackTrace();
//    }
//
//    return false;
  }

  public void componentMoved(ComponentEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    final Rectangle intersection = SwingUtilities.computeIntersection(
        widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(),
        parent.getInGameArea());

    if(intersection.x != 0 || intersection.y != 0 ||
       intersection.width != 0 || intersection.height != 0) {
      parent.paintInGameCurtain(true);
    } else parent.paintInGameCurtain(false);
  }
  /* Getter and Setter */
}
