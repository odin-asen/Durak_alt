package client.gui.frame;

import javax.swing.*;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 03:16
 * <p/>
 * This interface describes the requirements for a panel that will be displayed as a centre panel
 * in the {@link ClientFrame} class.
 */
public interface DurakCentrePanel {
  /**
   * Updates the client list. If clients is null, all clients will be removed from the list.
   *
   * @param clients All clients to show on the list.
   * @param T       Indicates the class type of the clients.
   */
  <T> void updateClients(List<T> clients);

  /**
   * Enables the game buttons depending on wheter the round was finished or not.
   *
   * @param roundFinished Indicates wheter the round was finished or not.
   */
  void enableGameButtons(boolean roundFinished);

  /**
   * Resets all widgets to a state that is necessary in the game for a new round.
   */
  void setNewRound();

  /**
   * Returns a widget that contains all buttons that are necessary for a game, e.g.
   * take cards or finish round.
   *
   * @param <T> A class that extends JPanel or a JPanel itself.
   * @return The panel for the game buttons.
   */
  <T extends JComponent> T getGameButtonsContainer();

  /**
   * Returns a widget where the opponents will be displayed.
   *
   * @param <T> A class that extends JPanel or a JPanel itself.
   * @return The panel for the opponent widgets.
   */
  <T extends JComponent> T getOpponentsContainer();

  /**
   * Returns the panel where the game cards will be displayed. This includes
   * the cards on the hand as well as the cards on the table.
   *
   * @param <T> A class that extends JPanel or a JPanel itself.
   * @return The panel for the game process display.
   */
  <T extends JComponent> T getGameProcessContainer();

  /**
   * Returns the panel for the statusbar to show information about the game,
   * the client, time, etc...
   *
   * @param <T> A class that extends JPanel or a JPanel itself.
   * @return The panel for the status.
   */
  <T extends JComponent> T getStatusBarContainer();

  /**
   * Returns the panel where all to the same server connected clients will be
   * shown.
   *
   * @return The panel that contains the client list.
   */
  <T extends JComponent> T getClientListContainer();

  /**
   * Returns the panel that shows the card stack of the game.
   *
   * @param <T> A class that extends JPanel or a JPanel itself.
   * @return The panel that shows the card stack of the game.
   */
  <T extends JComponent> T getCardStackContainer();
}