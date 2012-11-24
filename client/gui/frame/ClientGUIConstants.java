package client.gui.frame;

import common.resources.ResourceGetter;

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

  /* ClientFrame constants */
  float MAIN_FRAME_SCREEN_SIZE = 0.8f;
  int OPPONENT_PANEL_HEIGHT = 70;
  int CARD_STACK_PANEL_WIDTH = 250;
  int CARD_STACK_PANEL_VERTICAL_INSET = 80;
  int STATUS_BAR_HEIGHT = 20;

  /* SetUpFrame constants */
  float SETUP_FRAME_SCREEN_SIZE_HEIGHT = 0.3f;
  float SETUP_FRAME_SCREEN_SIZE_WIDTH = 0.25f;
  int PREFERRED_FIELD_WIDTH = 150;

  /* ChatFrame constants */
  float CHAT_FRAME_SCREEN_SIZE_HEIGHT = 0.5f;
  float CHAT_FRAME_SCREEN_SIZE_WIDTH = 0.25f;

  int CHAT_WRITE_AREA_HEIGHT = 70;
}
