package client.gui.widget.card;

import client.business.Client;
import client.business.client.GameClient;
import client.gui.frame.gamePanel.CardContainer;
import client.gui.frame.gamePanel.CurtainWidget;
import common.dto.DTOCard;
import common.dto.DTOClient;
import common.utilities.Converter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
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
  private CardContainer<GameCardWidget> cardContainer;
  private Rectangle attackArea;
  private CurtainWidget attackFieldWidget;

  /* Constructors */

  /**
   * Creates a move listener for attackers. It provides the dragging of a card to the GamePanel
   * objects ingame field. Hand cards can be pointed or marked to indicate that they will be
   * dragged together to the field.
   * @param relativePointedHeight The relative height of a card that this card will be raise if
   *                              a mouse click is done. E.g. if the value is 0.5, the card will
   *                              be raised by the half of its height.
   */
  protected AttackCardMoveListener(CardContainer<GameCardWidget> cardContainer,
                                   Rectangle attackArea, CurtainWidget attackFieldWidget,
                                   Float relativePointedHeight) {
    super();
    this.cardContainer = cardContainer;
    this.attackArea = attackArea;
    this.attackFieldWidget = attackFieldWidget;
    this.relativePointHeight = relativePointedHeight;
    pointedWidgets = new PointedCardContainer();
  }

  /* Methods */

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
      final JComponent parent = (JComponent) widget.getParent();
      final int lastZOrder = (parent != null) ? parent.getComponentZOrder(widget) : 0;
      widget.setLastZOrderIndex(lastZOrder);
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
      if(moveIsValid(widget)) {
        removeClientCards(widget);
      } else {
        setWidgetToLastPlace(widget);
      }

      pointedWidgets.dePointAll();
    }

    if(attackFieldWidget != null)
      attackFieldWidget.paintCurtain(false);

    super.mouseReleased(e);
  }

  private void setWidgetToLastPlace(GameCardWidget widget) {
    widget.setLocation(widget.getLastLocation());
    if(widget.getParent() != null)
      widget.getParent().setComponentZOrder(widget, widget.getLastZOrderIndex());
  }

  private void removeClientCards(GameCardWidget currentWidget) {
    if(pointedWidgets.widgets.contains(currentWidget))
      pointedWidgets.widgets.remove(currentWidget);
    cardContainer.removeCard(currentWidget);
    currentWidget.setCursor(Cursor.getDefaultCursor());
    for (GameCardWidget pointed : pointedWidgets.widgets) {
      cardContainer.removeCard(pointed);
      pointed.setCursor(Cursor.getDefaultCursor());
    }
    if(currentWidget.getParent() != null)
      currentWidget.getParent().repaint();
  }

  /**
   * @param widget Main widget of this attack move.
   * @param currentPanel Current panel the widget stands over.
   * @return Returns an empty string if the move is valid. If the reason is clear, when
   * the move is not valid, the string has a content. If the reason is not clear, the string
   * is null.
   */
  private boolean moveIsValid(GameCardWidget widget) {
    boolean result;
    if(!isWidgetInArea(widget, attackArea))
      return false;

    if(!pointedWidgets.widgets.contains(widget))
      pointedWidgets.widgets.add(widget);

    final List<DTOCard> cards = new ArrayList<DTOCard>(pointedWidgets.widgets.size());
    for (GameCardWidget cardWidget : pointedWidgets.widgets) {
      cards.add(Converter.toDTO(cardWidget.getCardInfo()));
    }

    final DTOClient dtoClient = Client.getOwnInstance().toDTO();
    result = GameClient.getClient().sendAction(dtoClient, cards, null);

    return result;
  }

  public void componentMoved(ComponentEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    attackFieldWidget.paintCurtain(isWidgetInArea(widget, attackArea));
  }

  public void componentResized(ComponentEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    pointedWidgets.widgets.remove(widget);
  }

  private Boolean isWidgetInArea(GameCardWidget widget, Rectangle area) {
    final Rectangle intersection = new Rectangle(area);
    SwingUtilities.computeIntersection(widget.getX(), widget.getY(), widget.getWidth(),
        widget.getHeight(), intersection);
    return intersection.x != 0 || intersection.y != 0 ||
        intersection.width != 0 || intersection.height != 0;
  }

  /* Getter and Setter */
  public void setArea(Rectangle area) {
    attackArea = area;
  }
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
      widgets.clear();
    }
  }
}
