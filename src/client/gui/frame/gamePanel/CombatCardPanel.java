package client.gui.frame.gamePanel;

import client.gui.frame.ClientGUIConstants;
import client.gui.widget.card.GameCardWidget;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 30.10.12
 * Time: 20:43
 * This panel can contain two GameCardWidgets. It should be used to display an
 * attacker card and a defender card.
 */
public class CombatCardPanel extends JPanel {
  private GameCardWidget attackerCard;
  private GameCardWidget defenderCard;

  private static final Float DISTANCE_DEFENDER_X = 0.2f;
  private static final Float DISTANCE_DEFENDER_Y = 0.2f;

  private Dimension cardDimension;

  /* Constructors */
  public CombatCardPanel() {
    cardDimension = new Dimension();
    this.setLayout(null);
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
  }

  public void paint(Graphics g) {
    super.paint(g);
    placeCards();
  }

  /* Methods */
  public void placeCards() {
    final Point attackerPoint = computeAttackerPosition();
    final Point defenderPoint = computeDefenderPosition();
    final Dimension cardDim = computeCardDimension();

    if(defenderCard != null) {
      GameCardWidget widget = defenderCard;
      widget.setBounds(new Rectangle(defenderPoint, cardDim));
      widget.setGameCardListener(null);
      widget.setMovable(false);
      this.setComponentZOrder(widget,0);
    }

    if(attackerCard != null) {
      GameCardWidget widget = attackerCard;
      widget.setBounds(new Rectangle(attackerPoint, cardDim));
      widget.setGameCardListener(null);
      widget.setMovable(false);
    }
  }

  private Dimension computeCardDimension() {
    int height = (int) (getHeight()*(1.0f));
    height = (int) (height*(1.0f-DISTANCE_DEFENDER_Y));
    int width = (int) (height*GameCardWidget.WIDTH_TO_HEIGHT);
    if(width*(1.0f+DISTANCE_DEFENDER_X) >= getWidth()) {
      width = (int) (getWidth()*(1.0f-DISTANCE_DEFENDER_X));
      height = (int) (width/ GameCardWidget.WIDTH_TO_HEIGHT);
    }
    cardDimension = new Dimension(width,height);

    return cardDimension;
  }

  public Point computeAttackerPosition() {
    int x = (int) (getWidth()/2-(cardDimension.width*(1.0f+DISTANCE_DEFENDER_X))/2);
    int y = (int) (getHeight()/2-(cardDimension.height*(1.0f+DISTANCE_DEFENDER_Y))/2);
    return new Point(x,y);
  }

  public Point computeDefenderPosition() {
    final Point attackerPoint = computeAttackerPosition();
    return new Point(attackerPoint.x+(int) (DISTANCE_DEFENDER_X * cardDimension.width),
        attackerPoint.y+(int) (DISTANCE_DEFENDER_Y * cardDimension.height));
  }

  private GameCardWidget addCard(GameCardWidget oldWidget, GameCardWidget newWidget) {
    if (oldWidget != null)
      remove(oldWidget);
    if(newWidget != null)
      add(newWidget);

    return newWidget;
  }

  private String createToolTipText() {
    if(attackerCard != null) {
      if(defenderCard == null)
        return attackerCard.getToolTipText()+" ist zu schlagen";
      else return defenderCard.getToolTipText()+" schl\u00e4gt "+attackerCard.getToolTipText();
    } else return "";
  }
  /* Getter and Setter */
  public GameCardWidget getAttackerCard() {
    return attackerCard;
  }

  public void setAttackerCard(GameCardWidget attackerCard) {
    this.attackerCard = addCard(this.attackerCard, attackerCard);
    setToolTipText(createToolTipText());
  }

  public GameCardWidget getDefenderCard() {
    return defenderCard;
  }

  public void setDefenderCard(GameCardWidget defenderCard) {
    this.defenderCard = addCard(this.defenderCard, defenderCard);
    setToolTipText(createToolTipText());
  }
}
