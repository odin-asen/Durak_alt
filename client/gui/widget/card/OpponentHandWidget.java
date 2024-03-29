package client.gui.widget.card;

import common.dto.DTOClient;
import common.i18n.BundleStrings;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.utilities.constants.PlayerConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * User: Timm Herrmann
 * Date: 05.10.12
 * Time: 22:36
 */
public class OpponentHandWidget extends JComponent {
  public static final int CARD_WIDTH = 34;
  public static final int CARD_HEIGHT = 50;
  public static final float LEFT_BORDER_DISTANCE = 0.33f;

  private ImageIcon cardBack;
  private ImageIcon statusIcon;
  private Font nameFont;

  private DTOClient opponent;

  public OpponentHandWidget(Font nameFont, ImageIcon cardBack, DTOClient opponent) {
    this.nameFont = nameFont;
    this.cardBack = cardBack;
    setOpponent(opponent);
    setBackground(Color.BLACK);
  }

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    drawCards(g2D);
    drawStatus(g2D);
    drawName(g2D);
  }

  private void drawCards(Graphics2D g2D) {
    final int drawWidth = (int) ((opponent.cardCount - 1) * CARD_WIDTH * LEFT_BORDER_DISTANCE) + CARD_WIDTH;
    final int xDrawOffset = getWidth() / 2 - drawWidth / 2;

    for (int i = 0; i < opponent.cardCount; i++)
      g2D.drawImage(cardBack.getImage(), (int) (i * CARD_WIDTH * LEFT_BORDER_DISTANCE) + xDrawOffset,
          getHeight() / 2 - CARD_HEIGHT / 2, CARD_WIDTH, CARD_HEIGHT, this);
  }

  private void drawName(Graphics2D g2D) {
    final Rectangle2D stringRect = nameFont.getStringBounds(opponent.name, g2D.getFontRenderContext());
    g2D.setFont(nameFont);
    g2D.setColor(Color.BLACK);
    g2D.drawString(opponent.name,
        getWidth() / 2 - (int) Math.abs(stringRect.getWidth())/2,
        getHeight() / 2);
  }

  private void drawStatus(Graphics2D g2D) {
    if (statusIcon != null) {
      final int xOffset = getWidth()/2-statusIcon.getIconWidth()/2;
      g2D.drawImage(statusIcon.getImage(), xOffset, 0, this);
    }
  }

  /* Getter and Setter */

  @SuppressWarnings("UnusedDeclaration")
  public DTOClient getOpponent() {
    return opponent;
  }

  public void setOpponent(DTOClient opponent) {
    final ImageIcon icon;
    final String string;
    if(opponent != null) {
      final int height = opponent.playerType.equals(PlayerConstants.PlayerType.LOSER)
          ? getSize().height : 0;
      icon = ResourceGetter.getPlayerTypeIcon(opponent.playerType, height);
      string = opponent.playerType.getDescription();
      this.opponent = opponent;
    } else {
      icon = null;
      string = null;
    }
    setStatusIcon(icon, string);
  }

  private void setStatusIcon(ImageIcon statusIcon, String statusDescription) {
    this.statusIcon = statusIcon;
    final String tooltipText;
    if(opponent != null)
      tooltipText = I18nSupport.getValue(BundleStrings.GUI_COMPONENT,
        "tooltip.player.type.0.name.0.cards.0", statusDescription, opponent.name,
        opponent.cardCount);
    else tooltipText = null;
    this.setToolTipText(tooltipText);
  }
}
