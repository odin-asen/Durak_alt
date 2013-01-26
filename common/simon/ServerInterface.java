package common.simon;

import common.dto.DTOClient;
import common.simon.action.GameAction;

/**
 * User: Timm Herrmann
 * Date: 06.01.13
 * Time: 23:55
 */
public interface ServerInterface {
  /**
   * Logs the specified client to the server. A password may be required.
   * @param callable Remote object that should be a valid SIMON implementation.
   * @param client Client information.
   * @param password Server password.
   * @return Returns true, if the client is logged in, else false.
   */
  public boolean login(Callable callable, DTOClient client, String password);

  /**
   * Logs the specified client off the server.
   * @param callable Remote object that should be an implemented SIMON object.
   */
  public void logoff(Callable callable);

  /**
   * Sends a message to all other clients.
   * @param callable Remote object that sends the message. Should be a valid SIMON implementation.
   * @param message Message text.
   */
  public void sendChatMessage(Callable callable, String message);

  /**
   * Executes the action with the specified cards.
   * @param callable Remote object that should be an implemented SIMON object.
   * @param action Contains information of the action, e.g. which type, which cards, etc...
   * @return True, action was made, else false.
   */
  public boolean doAction(Callable callable, GameAction action);

  /**
   * Updates the client information in the server.
   * @param callable Remote object that should be an implemented SIMON object.
   * @param dtoClient Client information.
   */
  public void updateClient(Callable callable, DTOClient dtoClient);
}
