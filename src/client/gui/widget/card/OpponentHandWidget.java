package client.gui.widget.card;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * User: Timm Herrmann
 * Date: 05.10.12
 * Time: 22:36
 */
public class OpponentHandWidget extends JComponent {
  public static final int CARDWIDTH = 34;
  public static final int CARDHEIGHT = 50;
  public static final float LEFT_BORDER_DISTANCE = 0.33f;
  public static final int Y_OFFSET = 5;
  private ImageIcon cardBack;
  private int cardCount;
  private String opponentName;

  public OpponentHandWidget(ImageIcon cardBack, String opponentName) {
    this.cardBack = cardBack;
    this.cardCount = 0;
    this.opponentName = opponentName;
    this.setPreferredSize(new Dimension(CARDWIDTH * 6, CARDHEIGHT + 20));
    this.setBackground(Color.BLACK);
  }

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    final Rectangle2D stringRect = g2D.getFont().getStringBounds(opponentName,g2D.getFontRenderContext());
    final int drawWidth = (int) ((cardCount-1)*CARDWIDTH*LEFT_BORDER_DISTANCE)+CARDWIDTH;
    final int xDrawOffset = getWidth()/2 - drawWidth/2;

    for(int i = 0; i < cardCount; i++)
      g2D.drawImage(cardBack.getImage(), (int) (i*CARDWIDTH*LEFT_BORDER_DISTANCE) + xDrawOffset,
          getHeight()/2 - CARDHEIGHT/2 - Y_OFFSET, CARDWIDTH, CARDHEIGHT,this);
    g2D.setColor(Color.BLACK);
    g2D.drawString(opponentName,
        getWidth()/2-(int)stringRect.getCenterX(),
        getHeight()/2-(int)stringRect.getCenterY() - Y_OFFSET);
  }

  public void setCardCount(int count) {
    cardCount = count;
  }
}
