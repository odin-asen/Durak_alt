package dto.observer;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 21:58
 * <p>
 * This enumerations can be used for GUI updates in GUIs that are implementing
 * the {@link java.util.Observer} interface.
 * </p>
 */
public enum GUIObserverConstants {
  CONNECTED, DISCONNECTED, CONNECTION_FAIL, SERVER_START, SERVER_STOP, SERVER_FAIL,
  CLIENT_CONNECTED, CLIENT_DISCONNECTED
}
