package dto.message;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 21:58
 * <p>
 * This enumerations can be used for GUI updates in GUIs that are implementing
 * the {@link java.util.Observer} interface.
 * </p>
 */
public enum GUIObserverType {
  CLIENT_CONNECTED,
  CLIENT_DISCONNECTED,
  CONNECTED,
  CONNECTION_FAIL,
  DISCONNECTED,
  INITIALISE_CARDS,
  INITIALISE_OPPONENTS,
  INITIALISE_STACK,
  SERVER_FAIL,
  SERVER_START,
  SERVER_STOP
}
