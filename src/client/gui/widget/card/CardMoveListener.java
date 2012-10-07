package client.gui.widget.card;

import client.gui.widget.card.GameCardWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Timm Herrmann
 * Date: 06.10.12
 * Time: 02:14
 */
public class CardMoveListener implements ComponentListener{
  private Set<GameCardWidget> widgets;
  private GameCardWidget curtainWidget;

  public CardMoveListener(Set<GameCardWidget> widgets) {
    this.widgets = widgets;
    for (GameCardWidget widget : widgets) {
      widget.addComponentListener(this);
    }
  }

  private GameCardWidget getMostTouchedWidget(GameCardWidget widget) {
    GameCardWidget resultWidget = null;
    Set<GameCardWidget> touchedWidgets = getAllTouchedWidgets(widget);
    Rectangle currentBiggest = new Rectangle(0,0,0,0);
    Rectangle newBiggest;

    final Rectangle toucher = widget.getBounds();
    for (GameCardWidget touchedWidget : touchedWidgets) {
      final Rectangle touched = touchedWidget.getBounds();
      newBiggest = SwingUtilities.computeIntersection(toucher.x,toucher.y,
          toucher.width,toucher.height,touched);
      if((currentBiggest.width*currentBiggest.height)<(newBiggest.width*newBiggest.height)) {
        currentBiggest = newBiggest;
        resultWidget = touchedWidget;
      }
    }
    return resultWidget;
  }

  private Set<GameCardWidget> getAllTouchedWidgets(GameCardWidget widget) {
    Set<GameCardWidget> touchedWidgets = new HashSet<GameCardWidget>();
    final Rectangle toucher = widget.getBounds();
    for (GameCardWidget gameCardWidget : widgets) {
      final Rectangle touched = gameCardWidget.getBounds();
      if((gameCardWidget != widget) && (toucher.intersects(touched)))
        touchedWidgets.add(gameCardWidget);
    }
    return touchedWidgets;
  }

  private void setCurtainWidget(GameCardWidget nearestWidget) {
    if(curtainWidget != nearestWidget) {
      if(curtainWidget != null)
        curtainWidget.setPaintCurtain(false);
      curtainWidget = nearestWidget;
    }
    curtainWidget.setPaintCurtain(true);
  }

  public void componentResized(ComponentEvent e) {
  }

  public void componentMoved(ComponentEvent e) {
    GameCardWidget widget = (GameCardWidget) e.getComponent();
    GameCardWidget nearestWidget = getMostTouchedWidget(widget);

    if(nearestWidget == null) {
      if(curtainWidget != null) {
        curtainWidget.setPaintCurtain(false);
        curtainWidget = null;
      }
    } else {
      setCurtainWidget(nearestWidget);
    }
  }

  public void componentShown(ComponentEvent e) {
  }

  public void componentHidden(ComponentEvent e) {
  }
}
