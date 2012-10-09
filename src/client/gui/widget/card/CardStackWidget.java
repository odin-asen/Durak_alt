package client.gui.widget.card;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * User: Timm Herrmann
 * Date: 06.10.12
 * Time: 03:57
 */
public class CardStackWidget extends JComponent {
  public static final int ORIENTATION_HORIZONTAL = 0;
  public static final int ORIENTATION_VERTICAL = 1;
  public static final float ALIGNMENT_CARDHEIGHT = 0.5f;
  public static final float RATIO_WIDTH_TO_HEIGHT = 0.69f;
  public static final float RATIO_RIGHT_MARGIN = 0.9f;

  private ImageIcon cardBack;
  private ImageIcon trumpCard;
  private int cardCount;
  private AffineTransform affineTransform;
  private int widthLimit;

  public CardStackWidget(ImageIcon cardBack, ImageIcon trumpCard, int orientation,
                         int widthLimit) {
    this.widthLimit = widthLimit;
    this.cardBack = cardBack;
    this.trumpCard = trumpCard;
    initAffineTransform(orientation);
    this.cardCount = 0;
    this.setBackground(Color.BLACK);
  }

  private void initAffineTransform(int orientation) {
    this.affineTransform = new AffineTransform();
    if(orientation == ORIENTATION_HORIZONTAL)
      this.affineTransform.rotate(Math.PI/2);
  }

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    final int cardHeight;
    if((int) (getHeight()*ALIGNMENT_CARDHEIGHT)>widthLimit)
      cardHeight = widthLimit;
    else
      cardHeight = (int) (getHeight()*ALIGNMENT_CARDHEIGHT);
    final int cardWidth = (int) (cardHeight*RATIO_WIDTH_TO_HEIGHT);
    final int cardX = (int) (getWidth()*RATIO_RIGHT_MARGIN) - cardHeight;
    final int cardY = getHeight()/2-cardHeight/2;

    g2D.drawImage(trumpCard.getImage(), cardX, cardY + cardHeight/2-cardWidth/2,
        cardWidth, cardHeight, this);
    for(int i = 0; i < cardCount; i++)
      g2D.drawImage(cardBack.getImage(), cardX + cardHeight-cardWidth+i*cardWidth/700, cardY, cardWidth, cardHeight,this);
  }

  public void pushCard() {
    cardCount++;
  }

  public void popCard() {
    cardCount--;
  }

  public int getCardCount() {
    return cardCount;
  }
}
