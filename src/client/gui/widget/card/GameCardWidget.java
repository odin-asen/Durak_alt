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
  public static final float WIDTH_TO_HEIGHT_RATIO = 0.69f;
  public static final float ALIGNMENT_CARD_HEIGHT = 0.3f;

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

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    final Dimension cardDim = computeCardDimension(getParent().getHeight());

    drawCard(g2D, cardDim);

    if(paintCurtain) {
      paintCurtain(g2D, cardDim);
    }
  }

  public static Dimension computeCardDimension(Integer parentPanelHeight) {
    final int height = (int) (parentPanelHeight*ALIGNMENT_CARD_HEIGHT);
    final int width = (int) (height*WIDTH_TO_HEIGHT_RATIO);

    return new Dimension(width,height);
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

  public void update(Observable o, Object arg) {
    String parameter = arg.toString();

    if(parameter.equals(BECAME_MOVABLE)) {
      this.addMouseMotionListener(gameCardListener);
    } else if(parameter.equals(BECAME_NOT_MOVABLE)) {
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
