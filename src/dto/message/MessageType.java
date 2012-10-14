package dto.message;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:00
 *
 * This enum contains constants that will be used on client and server side.
 */
public enum MessageType {
  GAME_ACTION,
  INITIAL_CARDS,
  LOGIN,
  QUIT_GAME_SIGNAL,
  START_GAME_SIGNAL,
  SERVER_PING,
  SERVER_UPDATE,
  WAIT_FOR_PLAYER
}
