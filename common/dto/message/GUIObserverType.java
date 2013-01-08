package common.dto.message;

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
  REFRESH_CLIENT_LIST,  /* Updates the servers client list to the server frame */
  REMOVE_CLIENTS,          /* Notfies the user that an error in the server occured */
}
