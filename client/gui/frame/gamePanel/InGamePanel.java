package client.gui.frame.gamePanel;

import client.gui.frame.ClientGUIConstants;
import client.gui.widget.card.GameCardWidget;
import common.game.GameCard;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InGamePanel extends JPanel implements CurtainWidget {
  private Integer[] grids;

  private boolean paintCurtain;
  private List<CombatCardPanel> cardPanels;
  private List<GameCard> attackCards;
  private List<GameCard> defenseCards;

  /* Constructors */
  public InGamePanel() {
    paintCurtain = false;
    grids = new Integer[]{1,1};
    cardPanels = new ArrayList<CombatCardPanel>();
    attackCards = new ArrayList<GameCard>(6);
    defenseCards = new ArrayList<GameCard>(6);
    setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
    setLayout(new GridLayout(grids[0], grids[1]));
    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
  }

  /* Methods */

  /**
   * Places the attacker and defender cards depending on the surpassed lists and fits them
   * to the given area.
   */
  public void setCards() {
    for (int index = 0; index < attackCards.size(); index++) {
      final CombatCardPanel panel;
      if(cardPanels.size() > index) {
        panel = cardPanels.get(index);
      } else {
        panel = new CombatCardPanel();
        addIngameCards(panel);
      }
      setAttackCard(panel, attackCards, index);
      setDefenseCard(panel, defenseCards, index);
    }
    refreshGrids();
  }

  private void setAttackCard(CombatCardPanel panel, List<GameCard> cardList, int index) {
    if(cardList != null && cardList.size() > index) {
      if(panel.getAttackerCard() != null)
        panel.getAttackerCard().setCard(cardList.get(index));
      else panel.setAttackerCard(new GameCardWidget(cardList.get(index)));
    }
  }

  private void setDefenseCard(CombatCardPanel panel, List<GameCard> cardList, int index) {
    if(cardList != null && cardList.size() > index) {
      if(panel.getDefenderCard() != null)
        panel.getDefenderCard().setCard(cardList.get(index));
      else panel.setDefenderCard(new GameCardWidget(cardList.get(index)));
    }
  }

  public void clearField() {
    removeAll();
    cardPanels.clear();
    validate();
    repaint();
  }

  private void addIngameCards(CombatCardPanel panel) {
    if (panel != null) {
      cardPanels.add(panel);
      add(panel);
      repaint();
    }
  }

  public void refreshGrids() {
    final int componentCount = getComponentCount();
    final Integer[] gridValues = computePanelGrid(componentCount, GameCardWidget.WIDTH_TO_HEIGHT);
    if(gridValues[0].compareTo(grids[0]) != 0 ||
       gridValues[1].compareTo(grids[1]) != 0) {
      grids = gridValues;
      setLayout(new GridLayout(gridValues[0], gridValues[1]));
    }
  }

  /**
   * Returns the number of rows and columns for the GridLayout of the parent Panel.
   * @param parent Panel where the GridLayout should be used.
   * @param panelCount Number of panels to be displayed at the {@code parent}
   * @param widthHeightRatio Ration between the width and the height of a panel
   *                         in the {@link InGamePanel#inGamePanel}
   * @return An array of the class Integer with the length of two where the first value is
   * the number of columns for the GridLayout and the second value is the number of rows.
   */
  private Integer[] computePanelGrid(Integer panelCount, Float widthHeightRatio) {
    if(panelCount < 1 || getHeight() < panelCount || getWidth() < panelCount)
      return new Integer[]{1,1};

    /* Compute the height space that the panels have to fill */
    int width = getWidth()/panelCount;
    int height = (int) (width/widthHeightRatio);
    if((height) > getHeight())
      height = getHeight();

    /* Compute rows and columns */
    Integer rows = getHeight()/height;
    Integer columns = panelCount/rows+1;

    while((rows-1)*columns >= panelCount)
      rows--;

    return new Integer[]{rows, columns};
  }

  public void paint(Graphics g) {
    super.paint(g);

    refreshGrids();
    if(paintCurtain)
      paintCurtain((Graphics2D) g, getSize());
  }

  private void paintCurtain(Graphics2D g2D, Dimension cardDim) {
    final Color oldColor = g2D.getColor();
    g2D.setColor(ClientGUIConstants.CURTAIN_COLOUR);
    g2D.fillRect(0, 0, cardDim.width, cardDim.height);
    g2D.setColor(oldColor);
  }

  public boolean allCardCovered() {
    if(cardPanels.size() <= 0)
      return false;

    for (CombatCardPanel panel : cardPanels) {
      if(!panel.isComplete())
        return false;
    }
    return true;
  }

  /* Getter and Setter */

  public void paintCurtain(boolean paint) {
    if(paintCurtain != paint) {
      paintCurtain = paint;
      repaint();
    }
  }

  public boolean setAttackCards(List<GameCard> cards) {
    if(!attackCards.equals(cards) && cards != null) {
      attackCards = cards;
      return true;
    } else return false;
  }

  public boolean setDefenseCards(List<GameCard> cards) {
    if(!defenseCards.equals(cards) && cards != null) {
      defenseCards = cards;
      return true;
    } else return false;
  }

  public List<CombatCardPanel> getCardPanels() {
    return cardPanels;
  }
}