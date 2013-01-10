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
  CLIENT_LIST,    /* Notifies the server's gui that the client list has changed */
  GAME_FINISHED,  /* Notifies the server's gui that the game has finished */
  REMOVE_CLIENTS  /* Notifies the server's gui that all clients are deleted */
}
