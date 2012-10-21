package dto.message;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:00
 *
 * This enum contains constants that will be used on client and server side.
 */
public enum MessageType {
  CHAT_MESSAGE,
  GAME_ACTION,
  LOGIN,
  SERVER_PING,
  WAIT_FOR_PLAYER
}
