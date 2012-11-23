package resources;

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
  String TOOLBAR_DIR = "toolbar/";

  String IMAGE_TOOLBAR_CHAT = TOOLBAR_DIR + "Chat.png";
  String IMAGE_TOOLBAR_CLOSE = TOOLBAR_DIR + "Close.png";
  String IMAGE_TOOLBAR_GAME_START = TOOLBAR_DIR + "Games.png";
  String IMAGE_TOOLBAR_NETWORK = TOOLBAR_DIR + "Network.png";
  String IMAGE_TOOLBAR_NETWORK_CLOSE = TOOLBAR_DIR + "Network_Close.png";
  String IMAGE_TOOLBAR_PINION = TOOLBAR_DIR + "Pinion.png";
  String IMAGE_TOOLBAR_PLAY = TOOLBAR_DIR + "Play.png";
  String IMAGE_TOOLBAR_STOP_PLAYER = TOOLBAR_DIR + "Stop Player.png";

  String IMAGE_RED_CROSS = "No.png";
  String IMAGE_GREEN_HOOK = "Yes.png";
  String IMAGE_STATUS_OK = "StatusOK.png";
  String IMAGE_STATUS_ERROR = "StatusError.png";
  String IMAGE_CROWN = "Winner.png";
  String IMAGE_DEFENDER = "Defender.png";
  String IMAGE_STAR_GREEN = "StarGreen.png";
  String IMAGE_STAR_YELLOW = "StarYellow.png";
  String IMAGE_STAR_RED = "StarRed.png";
  String IMAGE_STATUS_CONNECTED = "Connected.png";
  String IMAGE_STATUS_DISCONNECTED = "Disconnected.png";

  String CARD_BACK = "back.png";
  String CARD_COLOUR_CLUBS = "clubs.png";
  String CARD_COLOUR_DIAMONDS = "diamonds.png";
  String CARD_COLOUR_HEARTS = "hearts.png";
  String CARD_COLOUR_SPADES = "spades.png";
}
