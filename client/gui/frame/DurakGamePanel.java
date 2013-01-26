package client.gui.frame;

import javax.swing.*;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 03:16
 * <p/>
 * This interface describes the requirements for a panel that will be displayed as a centre panel
 * in the {@link ClientFrame} class.
 */
public interface DurakGamePanel {
  /**
   * Enables the game buttons depending on whether the round was finished or not.
   *
   * @param roundFinished Indicates whether the round was finished or not.
   * @param attackerFinished Indicates whether all attackers finished or not.
   */
  void enableGameButtons(boolean roundFinished, boolean attackerFinished);

  /**
   * Resets all widgets to a state that is necessary in the game for a new round.
   */
  void setNewRound();

  /**
   * Returns a widget that contains all buttons that are necessary for a game, e.g.
   * take cards or finish round.
   *
   * @return The panel for the game buttons.
   */
  JComponent getGameButtonsContainer();

  /**
   * Returns a widget where the opponents will be displayed.
   *
   * @return The panel for the opponent widgets.
   */
  JComponent getOpponentsContainer();

  /**
   * Returns the panel where the game cards will be displayed. This includes
   * the cards on the hand as well as the cards on the table.
   *
   * @return The panel for the game process display.
   */
  JComponent getGameProcessContainer();

  /**
   * Returns the panel that shows the card stack of the game.
   *
   * @return The panel that shows the card stack of the game.
   */
  JComponent getCardStackContainer();
}