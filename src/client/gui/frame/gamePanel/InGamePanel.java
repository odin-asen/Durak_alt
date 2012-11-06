package client.gui.frame.gamePanel;

import client.gui.frame.ClientGUIConstants;
import client.gui.widget.card.GameCardWidget;
import dto.DTOCard;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InGamePanel extends JPanel {
  private List<CombatCardPanel> cardPanels;
  private Integer[] grids;

  private Boolean paintCurtain;

  /* Constructors */
  public InGamePanel() {
    paintCurtain = false;
    grids = new Integer[]{1,1};
    cardPanels = new ArrayList<CombatCardPanel>();
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
    this.setLayout(new GridLayout(grids[0], grids[1]));
  }

  /* Methods */
  /**
   * Places every card of {@code attackCards} to the panel and lays every card of
   * {@code defenderCards} with the same index a little shifted over the attacker card.
   *
   * @param attackCards   Cards of the attacker.
   * @param defenderCards Cards of the defender.
   */
  public void placeCards(List<DTOCard> attackCards, List<DTOCard> defenderCards) {
    clearField();

    refreshGrids();
    for (DTOCard card : attackCards) {
      final CombatCardPanel panel = new CombatCardPanel();
      panel.setAttackerCard(new GameCardWidget(card));
      addInGameCards(panel);
    }

    for (int index = 0; index < defenderCards.size() && index < attackCards.size(); index++) {
      final CombatCardPanel cardPanel = cardPanels.iterator().next();
      cardPanel.setDefenderCard(new GameCardWidget(defenderCards.get(index)));
      cardPanel.placeCards();
    }
  }

  public void clearField() {
    removeAll();
    cardPanels.removeAll(cardPanels);
    repaint();
  }

  public void addInGameCards(CombatCardPanel panel) {
    if (panel != null) {
      cardPanels.add(panel);
      add(panel);
      repaint();
    }
  }

  private void refreshGrids() {
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

    g.setColor(Color.BLACK);
    g.drawRect(0,0,getWidth()-1,getHeight()-1);
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

  /* Getter and Setter */
  public void setPaintCurtain(Boolean paint) {
    if(paintCurtain != paint) {
      this.paintCurtain = paint;
      this.repaint();
    }
  }

  public List<CombatCardPanel> getCardPanels() {
    return cardPanels;
  }
}