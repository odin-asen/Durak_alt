package client.gui.widget.card;

import dto.ClientInfo;

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

  private ClientInfo opponent;

  public OpponentHandWidget(Font nameFont, ImageIcon cardBack, ClientInfo opponent) {
    this.nameFont = nameFont;
    this.cardBack = cardBack;
    this.opponent = opponent;
    this.setPreferredSize(new Dimension(CARD_WIDTH * 6, CARD_HEIGHT + 40));
    this.setBackground(Color.BLACK);
    setStatusIcon(null, null);
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
  public ClientInfo getOpponent() {
    return opponent;
  }

  public void setOpponent(ClientInfo opponent) {
    this.opponent = opponent;
  }

  public void setStatusIcon(ImageIcon statusIcon, String statusDescription) {
    this.statusIcon = statusIcon;
    this.setToolTipText(statusDescription);
  }
}
