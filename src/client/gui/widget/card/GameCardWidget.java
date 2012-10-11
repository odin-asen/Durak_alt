package client.gui.widget.card;

import client.business.GameCard;
import utilities.Converter;
import utilities.constants.GameCardConstants;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:24
 *
 * This class provides the graphical handling of the card, such as drag-and-drop.
 */
public class GameCardWidget extends JComponent implements Observer{
  public static final float WIDTH_TO_HEIGHT_RATIO = 0.69f;
  public static final float ALIGNMENT_CARDHEIGHT = 0.3f;

  private Image cardImage;
  private GameCard cardInfo;
  private boolean paintCurtain;
  private GameCardListener gameCardListener;

  public GameCardWidget(Image cardImage) {
    this.cardImage = cardImage;
    this.cardInfo = new GameCard();
    this.cardInfo.setCardColour(GameCardConstants.CARD_COLOUR_CLUBS);
    this.cardInfo.setCardValue(GameCardConstants.CARD_VALUE_ACE);
    this.cardInfo.addObserver(this);
    this.paintCurtain = false;
    this.gameCardListener = new GameCardListener();
    cardInfo.setMovable(false);

    String text = Converter.getCardColourName(cardInfo.getCardColour());
    if(!text.isEmpty())
      text = text + " " + Converter.getCardValueName(cardInfo.getCardValue());
    else text = Converter.getCardValueName(cardInfo.getCardValue());
    this.setToolTipText(text);
  }

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    final int height = (int) (this.getParent().getHeight()*ALIGNMENT_CARDHEIGHT);
    final int width = (int) (height*WIDTH_TO_HEIGHT_RATIO);
    this.setSize(width, height);

    g2D.drawImage(cardImage, 0, 0, width, height, this);
    if(paintCurtain) {
      final Color oldColor = g2D.getColor();
      g2D.setColor(new Color(0, 0, 255, 109));
      g2D.fillRect(0, 0, width, height);
      g2D.setColor(oldColor);
    }
  }

  public void addGameCardListener(GameCardListener listener) {
    this.gameCardListener = listener;
    this.addMouseListener(listener);
  }

  public void setPaintCurtain(boolean paint) {
    if(paintCurtain != paint) {
      this.paintCurtain = paint;
      this.repaint();
    } else
      this.paintCurtain = paint;
  }

  public void setMovable(boolean movable) {
    cardInfo.setMovable(movable);
  }

  public boolean isMovable() {
    return cardInfo.isMovable();
  }

  public void update(Observable o, Object arg) {
    String parameter = arg.toString();

    if(parameter.equals(GameCardConstants.BECAME_MOVABLE)) {
      this.addMouseMotionListener(gameCardListener);
    } else if(parameter.equals(GameCardConstants.BECAME_NOT_MOVABLE)) {
      this.removeMouseMotionListener(gameCardListener);
    }
  }

  public void moveInArea(Dimension dimension) {
    final Rectangle card = new Rectangle(this.getBounds());
    boolean insideLeft = false;
    boolean insideRight = false;
    boolean insideBottom = false;
    boolean insideTop = false;

    if(card.x>=0)
      insideLeft = true;
    if((card.x+card.width)<=dimension.width)
      insideRight = true;
    if((card.y+card.height)<=dimension.height)
      insideBottom = true;
    if(card.y>=0)
      insideTop = true;

    if(!insideLeft)
      this.setLocation(0,card.y);
    if(!insideRight)
      this.setLocation(dimension.width-card.width,card.y);
    if(!insideBottom)
      this.setLocation(card.x,dimension.height-card.height);
    if(!insideTop)
      this.setLocation(card.x,0);
  }
}
