package client.gui.widget.card;

import client.business.client.GameClient;
import client.gui.frame.ClientFrame;
import client.gui.frame.gamePanel.GamePanel;
import client.gui.frame.setup.SetupFrame;
import common.dto.ClientInfo;
import common.dto.DTOCard;
import common.i18n.I18nSupport;
import common.utilities.Converter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 05.11.12
 * Time: 00:52
 */
public class AttackCardMoveListener extends CardMoveListener {
  private static final String BUNDLE_NAME = "user.messages"; //NON-NLS
  private final PointedCardContainer pointedWidgets;
  private Float relativePointHeight;

  /* Constructors */
  protected AttackCardMoveListener(GamePanel parent, Float relativePointedHeight) {
    super(parent);
    this.relativePointHeight = relativePointedHeight;
    pointedWidgets = new PointedCardContainer();
  }

  /* Methods */
//  private void addCardToPanel(GameCardWidget widget) {
//    parent.removeCard(widget);
//    parent.repaint();
//
//    final CombatCardPanel panel = new CombatCardPanel();
//    panel.setAttackerCard(widget);
//    parent.addInGameCards(panel);
//  }

  public void mouseDragged(MouseEvent e) {
    super.mouseDragged(e);

    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    if(!pointedWidgets.widgets.isEmpty()) {
      movePointedWidgets(widget);
    }
  }

  public void mousePressed(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();

    if(!pointedWidgets.widgets.contains(widget)) {
      widget.setLastLocation(widget.getLocation());
      widget.setLastZOrderIndex(parent.getComponentZOrder(widget));
    }

    pointedWidgets.reDePointCard(widget);

    super.mousePressed(e);
  }

  private void movePointedWidgets(GameCardWidget cardWidget) {
    if(pointedWidgets.widgets.contains(cardWidget))
      pointedWidgets.widgets.remove(cardWidget);
    int nextX = cardWidget.getX();
    for (GameCardWidget widget : pointedWidgets.widgets) {
      nextX = nextX -cardWidget.getWidth();
      widget.setLocation(nextX,cardWidget.getY());
    }
  }

  public void mouseReleased(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    if(dragged) {
      final String inValidString = moveIsValid(widget);
      if(inValidString != null) {
        if(!inValidString.isEmpty()) {
          ClientFrame.showRuleException(parent, inValidString);
          setWidgetToLastPlace(widget);
        } else removeClientCards(widget);
      } else {
        setWidgetToLastPlace(widget);
      }

      pointedWidgets.dePointAll();
    }

    super.mouseReleased(e);
  }

  private void setWidgetToLastPlace(GameCardWidget widget) {
    widget.setLocation(widget.getLastLocation());
    parent.setComponentZOrder(widget, widget.getLastZOrderIndex());
  }

  private void removeClientCards(GameCardWidget currentWidget) {
    if(pointedWidgets.widgets.contains(currentWidget))
      pointedWidgets.widgets.remove(currentWidget);

    parent.removeCard(currentWidget);
    currentWidget.setCursor(Cursor.getDefaultCursor());
    for (GameCardWidget pointed : pointedWidgets.widgets) {
      parent.removeCard(pointed);
      pointed.setCursor(Cursor.getDefaultCursor());
    }
    parent.repaint();
  }

  /**
   * @param widget Main widget of this attack move.
   * @param currentPanel Current panel the widget stands over.
   * @return Returns an empty string if the move is valid. If the reason is clear, when
   * the move is not valid, the string has a content. If the reason is not clear, the string
   * is null.
   */
  private String moveIsValid(GameCardWidget widget) {
    String result;
    if(!isWidgetInArea(widget, parent.getInGameArea()))
      return null;

    try {
      if(!pointedWidgets.widgets.contains(widget))
        pointedWidgets.widgets.add(widget);

      final DTOCard[] cards = new DTOCard[pointedWidgets.widgets.size()];
      for (int index = 0, cardsLength = cards.length; index < cardsLength; index++) {
        cards[index] = Converter.toDTO(pointedWidgets.widgets.get(index).getCardInfo());
      }

      final ClientInfo clientInfo = SetupFrame.getInstance().getClientInfo();
      GameClient.getClient().sendAction(clientInfo, cards);
      result = GameClient.getClient().getActionDeniedReason(clientInfo);
    } catch (RemoteException e) {
      e.printStackTrace();
      result = I18nSupport.getValue(BUNDLE_NAME,"network.error");
    }

    return result;
  }

  public void componentMoved(ComponentEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    parent.paintInGameCurtain(isWidgetInArea(widget, parent.getInGameArea()));
  }

  public void componentResized(ComponentEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    pointedWidgets.widgets.remove(widget);
  }

  private Boolean isWidgetInArea(GameCardWidget widget, Rectangle area) {
    final Rectangle intersection = SwingUtilities.computeIntersection(
        widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(),
        area);

    return intersection.x != 0 || intersection.y != 0 ||
        intersection.width != 0 || intersection.height != 0;
  }

  /* Getter and Setter */
  /* Inner classes */
  private class PointedCardContainer {
    protected List<GameCardWidget> widgets;

    private PointedCardContainer() {
      widgets = new ArrayList<GameCardWidget>();
    }

    private void reDePointCard(GameCardWidget widget) {
      final int pointedHeight = (int) (widget.getHeight()*relativePointHeight);

      if(widgets.contains(widget)) {
        widget.setLocation(widget.getLastLocation());
        widgets.remove(widget);
      } else {
        widget.setLocation(widget.getX(), widget.getY()-pointedHeight);
        widgets.add(widget);
      }
    }

    private void dePointAll() {
      for (GameCardWidget widget : widgets) {
        widget.setLocation(widget.getLastLocation());
      }
      widgets.removeAll(widgets);
    }
  }
}
