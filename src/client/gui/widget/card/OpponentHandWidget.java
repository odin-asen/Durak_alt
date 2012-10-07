package client.gui.widget.card;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 05.10.12
 * Time: 22:36
 */
public class OpponentHandWidget extends Canvas {
  public static final int CARDWIDTH = 34;
  public static final int CARDHEIGHT = 50;
  private ImageIcon cardBack;
  private int cardCount;
  private String opponentName;

  public OpponentHandWidget(ImageIcon cardBack, String opponentName) {
    this.cardBack = cardBack;
    this.cardCount = 0;
    this.opponentName = opponentName;
    this.setBounds(10,10,CARDWIDTH*6,CARDHEIGHT+20);
  }

  public void paint(Graphics g) {
    for(int i = 0; i < cardCount; i++)
      g.drawImage(cardBack.getImage(), i*CARDWIDTH/3, 0, CARDWIDTH, CARDHEIGHT,this);
    g.setColor(Color.BLACK);
    g.drawString(opponentName, (CARDWIDTH*2)/3,CARDHEIGHT/2);
  }

  public void setCardCount(int count) {
    cardCount = count;
  }
}
