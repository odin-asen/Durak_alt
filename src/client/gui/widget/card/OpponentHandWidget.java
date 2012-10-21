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
  public static final int Y_OFFSET = 5;
  private ImageIcon cardBack;
  private ClientInfo opponent;

  public OpponentHandWidget(ImageIcon cardBack, ClientInfo opponent) {
    this.cardBack = cardBack;
    this.opponent = opponent;
    this.setPreferredSize(new Dimension(CARD_WIDTH * 6, CARD_HEIGHT + 20));
    this.setBackground(Color.BLACK);
  }

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    final Rectangle2D stringRect = g2D.getFont().getStringBounds(opponent.getClientName(),g2D.getFontRenderContext());
    final int drawWidth = (int) ((opponent.getCardCount()-1)* CARD_WIDTH *LEFT_BORDER_DISTANCE)+ CARD_WIDTH;
    final int xDrawOffset = getWidth()/2 - drawWidth/2;

    for(int i = 0; i < opponent.getCardCount(); i++)
      g2D.drawImage(cardBack.getImage(), (int) (i* CARD_WIDTH *LEFT_BORDER_DISTANCE) + xDrawOffset,
          getHeight()/2 - CARD_HEIGHT /2 - Y_OFFSET, CARD_WIDTH, CARD_HEIGHT,this);
    g2D.setColor(Color.BLACK);
    g2D.drawString(opponent.getClientName(),
        getWidth()/2-(int)stringRect.getCenterX(),
        getHeight()/2-(int)stringRect.getCenterY() - Y_OFFSET);
  }

  /* Getter and Setter */
  public ClientInfo getOpponent() {
    return opponent;
  }

  public void setOpponent(ClientInfo opponent) {
    this.opponent = opponent;
  }
}
