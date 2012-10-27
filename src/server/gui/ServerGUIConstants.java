package server.gui;

/**
 * User: Timm Herrmann
 * Date: 11.10.12
 * Time: 17:50
 */
public interface ServerGUIConstants {
  /* ServerFrame constants */
  float MAIN_FRAME_SCREEN_SIZE = 0.35f;

  String APPLICATION_NAME = "Durak Server";
  String TITLE_SEPARATOR = " - ";
  String VERSION = "Version 0.0";

  /* Action commands for Buttons and MenuItems */
  String ACTION_COMMAND_START = "start";
  String ACTION_COMMAND_STOP = "stop";
  String ACTION_COMMAND_CLOSE = "close";

  /* Label texts */
  String LABEL_SERVER_ADDRESS = "Serveradresse:";
  String LABEL_PORT = "Port";

  /* Tooltip texts */
  String TOOLTIP_START = "Starten den Server f\u00fcr ein Spiel";
  String TOOLTIP_STOP = "Stopt den Server";
  String TOOLTIP_CLOSE = "Schlie\u00dft die Anwendung";

  /* Alternative texts for icons */
  String ALTERNATIVE_CLOSE = "Schlie\u00dfen";
  String ALTERNATIVE_STOP = "Stop";
  String ALTERNATIVE_START = "Start";

  /* Printed messages */
  String STATUS_SERVER_INACTIVE = "Server ist inaktiv";
  String STATUS_SERVER_ACTIVE = "Server l\u00e4uft";

  int PREFERRED_FIELD_WIDTH = 80;
  int LIST_WIDTH = 200;
  String TOOLTIP_GAME_START = "Startet das Spiel";
  String ACTION_COMMAND_GAME_START = "gameStart";
  String ALTERNATIVE_GAME_START = "gameStart";
}
