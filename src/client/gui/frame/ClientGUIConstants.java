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
  ImageIcon CARD_BACK = ResourceGetter.getBackCard();

  /* Action commands for Buttons and MenuItems */
  String ACTION_COMMAND_CONNECTION = "connection";
  String ACTION_COMMAND_SETUP = "setup";
  String ACTION_COMMAND_CLOSE = "close";
  String ACTION_COMMAND_OKAY = "okay";
  String ACTION_COMMAND_APPLY = "apply";
  String ACTION_COMMAND_CANCEL = "cancel";

  /* ClientFrame constants */
  float MAIN_FRAME_SCREEN_SIZE = 0.8f;

  String APPLICATION_NAME = "Durak, das PC Spiel";
  String TITLE_SEPARATOR = " - ";
  String VERSION = "Version 0.0";
  int OPPONENT_PANEL_HEIGHT = 70;
  int CARD_STACK_PANEL_WIDTH = 250;
  int CARD_STACK_PANEL_VERTICAL_INSET = 80;

  /* SetUpFrame constants */
  float SET_UP_FRAME_SCREEN_SIZE_HEIGHT = 0.3f;
  float SET_UP_FRAME_SCREEN_SIZE_WIDTH = 0.25f;
  int PREFERRED_FIELD_WIDTH = 150;

  String SET_UP_TITLE = "Clienteinstellungen";
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
}
