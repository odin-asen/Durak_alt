package dto;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 22:56
 *
 * This interface implies an action that can be made within a game.
 * An action can be something like attack with a card or defense or etc.
 */
public interface GameAction {
  public void doAction();
}
