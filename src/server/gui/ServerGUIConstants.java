package server.gui;

/**
 * User: Timm Herrmann
 * Date: 11.10.12
 * Time: 17:50
 */
public interface ServerGUIConstants {
  /* ServerFrame constants */
  public static final float MAIN_FRAME_SCREEN_SIZE = 0.3f;

  /* Action commands for Buttons and MenuItems */
  public static final String ACTION_COMMAND_START = "start";
  public static final String ACTION_COMMAND_STOP = "stop";
  public static final String ACTION_COMMAND_CLOSE = "close";

  /* Label texts */
  public static final String LABEL_SERVER_ADDRESS = "Serveradresse:";
  public static final String LABEL_PORT = "Port:";

  /* Tooltip texts */
  public static final String TOOLTIP_START = "Starten den Server f\u00fcr ein Spiel";
  public static final String TOOLTIP_STOP = "Stopt den Server";
  public static final String TOOLTIP_CLOSE = "Schlie\u00dft die Anwendung";

  /* Alternative texts for icons */
  public static final String ALTERNATIVE_CLOSE = "Schlie\u00dfen";
  public static final String ALTERNATIVE_STOP = "Stop";
  public static final String ALTERNATIVE_START = "Start";

  /* Printed messages */
  public static final String SERVER_INACTIVE = "Server ist inaktiv";
}
