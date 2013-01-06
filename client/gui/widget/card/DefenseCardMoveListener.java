package client.gui.widget.card;

import client.business.client.Client;
import client.business.client.GameClient;
import client.gui.frame.ClientFrame;
import client.gui.frame.gamePanel.CombatCardPanel;
import client.gui.frame.gamePanel.GamePanel;
import common.dto.DTOClient;
import common.i18n.I18nSupport;
import common.utilities.Converter;
import common.utilities.LoggingUtility;
import common.utilities.gui.Compute;

import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.ClientInfoStatus;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 05.11.12
 * Time: 00:52
 */
public class DefenseCardMoveListener extends CardMoveListener {
  private static final String BUNDLE_NAME = "user.messages"; //NON-NLS
  private static final Logger LOGGER = LoggingUtility.getLogger(DefenseCardMoveListener.class.getName());
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

  private void removeClientCard(GameCardWidget widget) {
    parent.removeCard(widget);
    parent.repaint();
  }

  public void mousePressed(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    widget.setLastLocation(widget.getLocation());
    widget.setLastZOrderIndex(parent.getComponentZOrder(widget));

    super.mousePressed(e);
  }

  public void mouseReleased(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    final String invalidString = moveIsValid(widget, curtainPanel);
    if(invalidString != null) {
      if(invalidString.isEmpty()) {
        removeClientCard(widget);
      } else {
        ClientFrame.showRuleException(parent, invalidString);
        setWidgetToLastPlace(widget);
      }
    } else {
      setWidgetToLastPlace(widget);
    }

    super.mouseReleased(e);
  }

  private void setWidgetToLastPlace(GameCardWidget widget) {
    widget.setLocation(widget.getLastLocation());
    parent.setComponentZOrder(widget, widget.getLastZOrderIndex());
  }

  /**
   * @param widget Widget of this defense move.
   * @param currentPanel Current panel the widget stands over.
   * @return Returns an empty string if the move is valid. If the reason is clear, when
   * the move is not valid, the string has a content. If the reason is not clear, the string
   * is null.
   */
  private String moveIsValid(GameCardWidget widget, CombatCardPanel currentPanel) {
    String result;
    if(currentPanel == null)
      return null;

    try {
      final Client client = Client.getOwnInstance();
      final DTOClient dtoClient = client.toDTO();
      GameClient.getClient().sendAction(dtoClient, Converter.toDTO(widget.getCardInfo()),
         Converter.toDTO(currentPanel.getAttackerCard().getCardInfo()));
      result = GameClient.getClient().getActionDeniedReason(dtoClient);
    } catch (RemoteException e) {
      result = I18nSupport.getValue(BUNDLE_NAME,"network.error");
      LoggingUtility.printStackTrace(LOGGER, Level.SEVERE, e.getStackTrace());
    }

    return result;
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
