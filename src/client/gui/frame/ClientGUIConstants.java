package client.gui.frame;

import resources.ResourceGetter;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 11.10.12
 * Time: 17:14
 */
public interface ClientGUIConstants {
  /* General constants */
  Color GAME_TABLE_COLOUR = new Color(13, 153, 0);
  Color CURTAIN_COLOUR = new Color(0, 0, 255, 109);
  ImageIcon CARD_BACK = ResourceGetter.getBackCard();
  Font OPPONENT_FONT = new Font(Font.SANS_SERIF, Font.BOLD,17);

  /* Action commands for Buttons and MenuItems */
  String ACTION_COMMAND_APPLY = "apply";
  String ACTION_COMMAND_CANCEL = "cancel";
  String ACTION_COMMAND_CHAT = "chat";
  String ACTION_COMMAND_CLOSE = "close";
  String ACTION_COMMAND_CONNECT = "connect";
  String ACTION_COMMAND_DISCONNECT = "disconnect";
  String ACTION_COMMAND_OKAY = "okay";
  String ACTION_COMMAND_SETUP = "setup";
  String ACTION_COMMAND_TAKE_CARDS = "takeCards";
  String ACTION_COMMAND_ROUND_DONE = "roundDone";

  /* ClientFrame constants */
  float MAIN_FRAME_SCREEN_SIZE = 0.8f;
  int OPPONENT_PANEL_HEIGHT = 70;
  int CARD_STACK_PANEL_WIDTH = 250;
  int CARD_STACK_PANEL_VERTICAL_INSET = 80;

  String APPLICATION_NAME = "Durak, das PC Spiel";
  String TITLE_SEPARATOR = " - ";
  String VERSION = "Version 0.0";
  String BUTTON_TEXT_TAKE_CARDS = "Karten nehmen";
  String BUTTON_TEXT_ROUND_DONE = "Runde beenden";

  /* SetUpFrame constants */
  float SET_UP_FRAME_SCREEN_SIZE_HEIGHT = 0.3f;
  float SET_UP_FRAME_SCREEN_SIZE_WIDTH = 0.25f;
  int PREFERRED_FIELD_WIDTH = 150;

  String SET_UP_TITLE = "Client Einstellungen";
  String TITLE_CONNECTION = "Verbindung";
  String TITLE_INFORMATION = "Client Information";

  String SET_UP_APPLY_TOOLTIP = "Eingabedaten \u00fcbernehmen";
  String SET_UP_OKAY_TOOLTIP = "Eingabedaten best\u00e4tigen und Fenster schlie\u00dfen";
  String SET_UP_CANCEL_TOOLTIP = "Eingabedaten verwerfen und Fenster schlie\u00dfen";
  String SET_UP_CHECKBOX_PLAYING_TOOLTIP = "Meldet den Client automatisch zum Spiel an";
  String SET_UP_PASSWORD_TOOLTIP = "Passwort f\u00fcr den Server";

  String SET_UP_CANCEL_TEXT = "Abbrechen";
  String SET_UP_OKAY_TEXT = "Okay";
  String SET_UP_APPLY_TEXT = "\u00dcbernehmen";
  String SET_UP_CHECKBOX_PLAYING_TEXT = "Spielbereit";
  String SET_UP_CHECKBOX_PASSWORD_TEXT = "Passwort anzeigen";

  /* ChatFrame constants */
  float CHAT_FRAME_SCREEN_SIZE_HEIGHT = 0.5f;
  float CHAT_FRAME_SCREEN_SIZE_WIDTH = 0.25f;

  String CHAT_TITLE = "Chat";

  int CHAT_WRITE_AREA_HEIGHT = 70;
  String BUTTON_NAME_SEND = "Senden";
  String SCROLL_READ_NAME = "Lesen";
  String SCROLL_WRITE_NAME = "Schreiben";
  String BUTTON_PANEL_NAME = "Kn\u00f6pfe";

  /* Status Messages */
  String STATUS_CONNECTION_FAIL = "Verbindung zum Server konnte nicht aufgebaut werden!";
  String STATUS_CONNECTED = "Verbindung zum Server wurde erfolgreich aufgebaut";
  String STATUS_PERMISSION_DENIED = "Das Passwort f\u00fcr den Server ist falsch";

  /* Alternative texts */
  String ALTERNATIVE_CONNECT = "Verbindung";
  String ALTERNATIVE_DISCONNECT = "Trennen";
  String ALTERNATIVE_SETUP = "Einstellungen";
  String ALTERNATIVE_CLOSE = "Schlie\u00dfen";
  String ALTERNATIVE_CHAT = "Chat";

  /* Tooltip Texts */
  String TOOLTIP_CLOSE = "Schlie\u00dft die Anwendung";
  String TOOLTIP_CHAT = "\u00d6ffnet und schlie\u00dft das Chatfenster";
  String TOOLTIP_SETUP = "\u00d6ffnet ein Fenster f\u00fcr Einstellungen";
  String TOOLTIP_CONNECT = "Verbindung zu Server aufbauen";
  String TOOLTIP_DISCONNECT = "Verbindung zum Server trennen";
}
