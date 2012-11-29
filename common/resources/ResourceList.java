package common.resources;

/**
 * User: Timm Herrmann
 * Date: 29.10.12
 * Time: 17:17
 */
@SuppressWarnings("HardCodedStringLiteral")
public interface ResourceList {
  String RESOURCES_ROOT = "";
  String PICTURES_ROOT = RESOURCES_ROOT + "icons/";
  String CARDS_ROOT = PICTURES_ROOT + "cards/";
  String SOUNDS_ROOT = RESOURCES_ROOT + "sounds/";
  String TOOLBAR_ROOT = PICTURES_ROOT + "toolbar/";
  String APPLICATION_ROOT = PICTURES_ROOT + "application/";
  String STATUS_ROOT = PICTURES_ROOT + "status/";

  String APPLICATION_BASE_PATH = APPLICATION_ROOT + "DurakIcon";

  String IMAGE_TOOLBAR_CHAT = TOOLBAR_ROOT + "Chat.png";
  String IMAGE_TOOLBAR_CLOSE = TOOLBAR_ROOT + "Close.png";
  String IMAGE_TOOLBAR_GAME_START = TOOLBAR_ROOT + "Games.png";
  String IMAGE_TOOLBAR_NETWORK = TOOLBAR_ROOT + "Network.png";
  String IMAGE_TOOLBAR_NETWORK_CLOSE = TOOLBAR_ROOT + "Network_Close.png";
  String IMAGE_TOOLBAR_PINION = TOOLBAR_ROOT + "Pinion.png";
  String IMAGE_TOOLBAR_PLAY = TOOLBAR_ROOT + "Play.png";
  String IMAGE_TOOLBAR_STOP_PLAYER = TOOLBAR_ROOT + "Stop Player.png";

  String IMAGE_RED_CROSS = STATUS_ROOT + "No.png";
  String IMAGE_GREEN_HOOK = STATUS_ROOT + "Yes.png";
  String IMAGE_STATUS_OK = STATUS_ROOT + "StatusOK.png";
  String IMAGE_STATUS_ERROR = STATUS_ROOT + "StatusError.png";
  String IMAGE_CROWN = STATUS_ROOT + "Winner.png";
  String IMAGE_DEFENDER = STATUS_ROOT + "Defender.png";
  String IMAGE_STAR_GREEN = STATUS_ROOT + "StarGreen.png";
  String IMAGE_STAR_YELLOW = STATUS_ROOT + "StarYellow.png";
  String IMAGE_STAR_RED = STATUS_ROOT + "StarRed.png";
  String IMAGE_STATUS_CONNECTED = STATUS_ROOT + "Connected.png";
  String IMAGE_STATUS_DISCONNECTED = STATUS_ROOT + "Disconnected.png";

  String CARD_BACK = "back.png";
  String CARD_COLOUR_CLUBS = "clubs.png";
  String CARD_COLOUR_DIAMONDS = "diamonds.png";
  String CARD_COLOUR_HEARTS = "hearts.png";
  String CARD_COLOUR_SPADES = "spades.png";
}
