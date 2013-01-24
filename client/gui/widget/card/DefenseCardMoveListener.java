package client.gui.widget.card;

import client.business.Client;
import client.business.client.GameClient;
import client.gui.frame.gamePanel.CardContainer;
import client.gui.frame.gamePanel.CombatCardPanel;
import common.dto.DTOClient;
import common.utilities.Converter;
import common.utilities.LoggingUtility;
import common.utilities.gui.Compute;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 05.11.12
 * Time: 00:52
 *
 * This CardMoveListener extension is made for defender types. A card can be dragged and
 * successfully released only over a CombatCardPanel that is contained in the surpassed list
 * at the constructor. The most touched CombatCardPanel without a defense card will be highlighted.
 */
public class DefenseCardMoveListener extends CardMoveListener {
  private static final Logger LOGGER = LoggingUtility.getLogger(
      DefenseCardMoveListener.class.getName());
  private CombatCardPanel currentCurtain;
  private List<CombatCardPanel> cardPanels;
  private CardContainer<GameCardWidget> cardContainer;

  /* Constructors */
  protected DefenseCardMoveListener(List<CombatCardPanel> cardPanels,
                                    CardContainer<GameCardWidget> cardContainer) {
    super();
    this.cardPanels = cardPanels;
    this.cardContainer = cardContainer;
  }

  /* Methods */
  private void setCurrentCurtain(CombatCardPanel widget) {
    if(currentCurtain != null)
      currentCurtain.paintCurtain(false);

    if(widget != null) {
      if(!widget.equals(currentCurtain))
        currentCurtain = widget;
      currentCurtain.paintCurtain(true);
    } else currentCurtain = null;
  }

  private void removeClientCard(GameCardWidget widget) {
    cardContainer.removeCard(widget);
    if(widget.getParent() != null)
      widget.getParent().repaint();
  }

  public void mousePressed(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    final JComponent parent = (JComponent) widget.getParent();
    final int lastZOrder = (parent != null) ? parent.getComponentZOrder(widget) : 0;
    widget.setLastLocation(widget.getLocation());
    widget.setLastZOrderIndex(lastZOrder);

    super.mousePressed(e);
  }

  public void mouseReleased(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    if(moveIsValid(widget)) {
      removeClientCard(widget);
    } else {
      setWidgetToLastPlace(widget);
    }

    super.mouseReleased(e);
  }

  private void setWidgetToLastPlace(GameCardWidget widget) {
    widget.setLocation(widget.getLastLocation());
    if(widget.getParent() != null)
      widget.getParent().setComponentZOrder(widget, widget.getLastZOrderIndex());
  }

  /**
   * Compares the move with the current curtain panel. If the curtain panel is null, the method
   * returns false.
   * @param defenseCard Widget of this defense move.
   * @param currentPanel Current panel the defenseCard stands over.
   * @return Returns true, if the move is valid or the current panel is null, else false.
   */
  private boolean moveIsValid(GameCardWidget defenseCard) {
    if(currentCurtain == null)
      return false;

    final Client client = Client.getOwnInstance();
    final DTOClient dtoClient = client.toDTO();
    return GameClient.getClient().sendAction(dtoClient,
        Converter.toDTO(currentCurtain.getAttackerCard().getCardInfo()),
        Converter.toDTO(defenseCard.getCardInfo()));
  }

  public void componentMoved(ComponentEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    final CombatCardPanel nearestWidget =
        (CombatCardPanel) Compute.getMostTouchedComponent(cardPanels, widget);

    /* Determine nearest curtain */
    if(getGrabbingPoint() != null && nearestWidget != null) {
      setCurrentCurtain(!nearestWidget.isComplete() ? nearestWidget : null);
    } else setCurrentCurtain(null);
  }
  /* Getter and Setter */
}
