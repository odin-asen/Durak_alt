package common.simon;

/**
 * User: Timm Herrmann
 * Date: 07.01.13
 * Time: 00:05
 *
 * This interface indicates that the implementing class can be called back.
 * The interface might be implemented by a client programme.
 */
public interface Callable {
  /**
   * This method might be used by a server to notify a client.
   * @param parameter Notification for the client.
   */
  public void callback(Object parameter);
}
