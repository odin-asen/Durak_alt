package client.gui.widget.card;

import dto.DTOCard;
import game.GameCard;
import resources.ResourceGetter;
import utilities.Converter;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import static utilities.constants.GameCardConstants.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:24
 *
 * This class provides the graphical handling of the card, such as drag-and-drop.
 */
public class GameCardWidget extends JComponent implements Observer{
  public static final float WIDTH_TO_HEIGHT = 0.69f;
  public static final float ALIGNMENT_CARD_HEIGHT = 0.3f;
  //TODO Karten auf Position fixieren und nur auf andere Karten ziehbar machen oder aufs
  //TODO Spielfeld
  private Image cardImage;
  private GameCard cardInfo;
  private Boolean paintCurtain;
  private GameCardListener gameCardListener;
  private Boolean movable;

  /* Constructors */
  public GameCardWidget(DTOCard dtoCard) {
    this.cardInfo = Converter.fromDTO(dtoCard);
    this.paintCurtain = false;
    this.gameCardListener = new GameCardListener();
    this.movable = false;

    this.setToolTipText(dtoCard.getColourAndValue());
    this.cardImage = ResourceGetter.getCardImage(dtoCard.cardColour,
        dtoCard.cardValue,dtoCard.getColourAndValue()).getImage();
  }

  /* Methods */
  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    drawCard(g2D, getSize());

    if(paintCurtain) {
      paintCurtain(g2D, getSize());
    }
  }

  private void drawCard(Graphics2D g2D, Dimension cardDim) {
    this.setSize(cardDim);
    g2D.drawImage(cardImage, 0, 0, cardDim.width, cardDim.height, this);
  }

  private void paintCurtain(Graphics2D g2D, Dimension cardDim) {
    final Color oldColor = g2D.getColor();
    g2D.setColor(new Color(0, 0, 255, 109));
    g2D.fillRect(0, 0, cardDim.width, cardDim.height);
    g2D.setColor(oldColor);
  }

  public void setGameCardListener(GameCardListener listener) {
    this.removeMouseListener(gameCardListener);

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

  public void update(Observable o, Object arg) {
    String parameter = arg.toString();

    if(parameter.equals(BECAME_MOVABLE)) {
      this.addMouseMotionListener(gameCardListener);
    } else if(parameter.equals(BECAME_NOT_MOVABLE)) {
      this.removeMouseMotionListener(gameCardListener);
    }
  }

  /**
   * Changes the widgets bounds, so that it is inside the specified
   * area.
   * @param area Specified area.
   */
  public void moveInArea(Rectangle area, Float distanceX, Float distanceY) {
    final Rectangle card = new Rectangle(this.getBounds());
    final int borderDistanceX = (int) (area.width*distanceX);
    final int borderDistanceY = (int) (area.height*distanceY);

    boolean insideLeft = false;
    boolean insideRight = false;
    boolean insideBottom = false;
    boolean insideTop = false;

    if(card.x >= (area.x + borderDistanceX))
      insideLeft = true;
    if((card.x + card.width) <= ((area.x + area.width) - borderDistanceX))
      insideRight = true;
    if((card.y + card.height) <= ((area.y + area.height) - borderDistanceY))
      insideBottom = true;
    if(card.y >= (area.y + borderDistanceY))
      insideTop = true;

    if(!insideLeft)
      this.setLocation(area.x+borderDistanceX,card.y);
    if(!insideRight)
      this.setLocation(area.x+area.width-card.width-borderDistanceX,card.y);
    if(!insideBottom)
      this.setLocation(area.x+card.x,area.y+area.height-card.height-borderDistanceY);
    if(!insideTop)
      this.setLocation(card.x,area.y+borderDistanceY);
  }

  /* Getter and Setter */
  public GameCard getCardInfo() {
    return cardInfo;
  }

  public void setMovable(Boolean movable) {
    if(movable ^ this.movable) {
      this.movable = movable;

      if(isMovable())
        this.addMouseMotionListener(gameCardListener);
      else
        this.removeMouseMotionListener(gameCardListener);
    }
  }

  public Boolean isMovable() {
    return movable;
  }
}
