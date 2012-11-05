package client.gui.widget.card;

import client.gui.frame.gamePanel.CombatCardPanel;
import client.gui.frame.gamePanel.GamePanel;
import utilities.gui.Compute;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 05.11.12
 * Time: 00:52
 */
public class DefenseCardMoveListener extends CardMoveListener {
  private CombatCardPanel curtainPanel;
  private List<CombatCardPanel> combatPanels;

  /* Constructors */
  protected DefenseCardMoveListener(GamePanel parent, List<CombatCardPanel> combatPanels) {
    super(parent);
    this.combatPanels = combatPanels;
  }

  /* Methods */
  private void setCurtainPanel(CombatCardPanel panel) {
    if(curtainPanel != null)
      curtainPanel.getAttackerCard().setPaintCurtain(false);

    if(panel != null) {
      if(curtainPanel != panel)
        curtainPanel = panel;
      curtainPanel.getAttackerCard().setPaintCurtain(true);
    } else curtainPanel = null;
  }

  private Boolean moveIsValid(GameCardWidget widget, CombatCardPanel panel) {
    return panel != null;

//    try {
//      return GameClient.getClient().sendAction(SetUpFrame.getInstance().getClientInfo(),
//          Converter.toDTO(widget.getCardInfo()));
//    } catch (RemoteException e) {
//      e.printStackTrace();
//    }
//
//    return false;
  }

  private void addCardToPanel(GameCardWidget widget) {
    parent.removeCard(widget);
    parent.repaint();
    curtainPanel.setDefenderCard(widget);
  }

  public void mouseReleased(MouseEvent e) {
    GameCardWidget widget = (GameCardWidget) e.getComponent();
    if(moveIsValid(widget, curtainPanel)) {
      addCardToPanel(widget);
      widget.setCursor(Cursor.getDefaultCursor());
    } else {
      widget.setLocation(widget.getLastLocation());
      parent.setComponentZOrder(widget, widget.getLastZOrderIndex());
    }

    super.mouseReleased(e);
  }

  public void componentMoved(ComponentEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    final CombatCardPanel nearestPanel =
        (CombatCardPanel) Compute.getMostTouchedComponent(combatPanels, widget);

    /* Determine nearest curtain */
    if(getGrabbingPoint() != null && nearestPanel != null) {
      if(!nearestPanel.hasDefenderCard())
        setCurtainPanel(nearestPanel);
      else setCurtainPanel(null);
    } else setCurtainPanel(null);
  }
  /* Getter and Setter */
}
