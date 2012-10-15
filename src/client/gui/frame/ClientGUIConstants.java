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
  public static final Color GAME_TABLE_COLOUR = new Color(13, 153, 0);
  public static final ImageIcon CARD_BACK = ResourceGetter.getBackCard();

  /* ClientFrame constants */
  public static final float MAIN_FRAME_SCREEN_SIZE = 0.8f;

  public static final String APPLICATION_NAME = "Durak, das PC Spiel";
  public static final String TITLE_SEPARATOR = " - ";
  public static final String VERSION = "Version 0.0";
  public static final int OPPONENT_PANEL_HEIGHT = 70;
  public static final int CARDSTACK_PANEL_WIDTH = 250;
  public static final int CARDSTACK_PANEL_VERTICAL_INSET = 80;

  /* SetUpFrame constants */
  public static final float SET_UP_FRAME_SCREEN_SIZE_HEIGHT = 0.7f;
  public static final float SET_UP_FRAME_SCREEN_SIZE_WIDTH = 0.3f;

  /* Action commands for Buttons and MenuItems */
  public static final String ACTION_COMMAND_CONNECTION = "connection";
  public static final String ACTION_COMMAND_SETUP = "setup";
  public static final String ACTION_COMMAND_CLOSE = "close";
}
