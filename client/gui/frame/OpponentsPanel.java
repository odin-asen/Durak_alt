package client.gui.frame;

import client.gui.widget.card.OpponentHandWidget;
import common.dto.DTOClient;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static client.gui.frame.ClientGUIConstants.CARD_BACK;
import static client.gui.frame.ClientGUIConstants.OPPONENT_FONT;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:12
 */
public class OpponentsPanel extends JPanel {
  private List<DTOClient> opponents;
  private List<OpponentHandWidget> widgets;

  /* Constructors */

  public OpponentsPanel() {
    opponents = new ArrayList<DTOClient>(6);
    widgets = new ArrayList<OpponentHandWidget>(6);
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
  }

  /* Methods */

  private void addWidget(DTOClient opponent) {
    final OpponentHandWidget hw = new OpponentHandWidget(OPPONENT_FONT, CARD_BACK, opponent);
    widgets.add(hw);
    add(hw);
  }

  public void updateOpponents() {
    for (int index = 0; index < opponents.size(); index++) {
      final DTOClient opponent = opponents.get(index);
      if(widgets.size() > index)
        widgets.get(index).setOpponent(opponent);
      else addWidget(opponent);
    }
    if(cleanUpWidgets())
      validate();
    else revalidate();
    repaint();
  }

  private boolean cleanUpWidgets() {
    boolean invalid = false;
    for (int index = opponents.size(); index < widgets.size(); index++) {
      remove(widgets.get(index));
      widgets.remove(index);
      invalid = true;
    }
    return invalid;
  }

  public void removeAllOpponents() {
    opponents.clear();
    widgets.clear();
    removeAll();
    validate();
  }

  /* Getter and Setter */

  public void setOpponents(List<DTOClient> opponents) {
    if(opponents != null) {
      this.opponents = opponents;
      updateOpponents();
    }
  }
}
