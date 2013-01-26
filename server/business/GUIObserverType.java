package server.business;

/**
 * User: Timm Herrmann
 * Date: 09.01.13
 * Time: 17:06
 * <p>
 * This enumerations can be used for the server's GUI updates.
 * </p>
 */
public enum GUIObserverType {
  /** Notifies the server's gui that the client list has changed */
  CLIENT_LIST,
  /** Notifies the server's gui that the game has finished */
  GAME_FINISHED
}
