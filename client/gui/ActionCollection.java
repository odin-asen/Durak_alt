package client.gui;

import client.business.Client;
import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.business.client.GameClientException;
import client.gui.frame.ClientFrame;
import client.gui.frame.ClientGUIConstants;
import client.gui.frame.ConnectionDialog;
import client.gui.frame.chat.ChatFrame;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.resources.ResourceList;
import common.utilities.LoggingUtility;
import common.utilities.gui.DurakPopup;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 11.01.13
 * Time: 03:11
 */
public class ActionCollection {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS

  /************************/
  /**** Static Actions ****/
  /************************/
  public static final Action CONNECTION_DIALOG_EDITABLE = new OpenConnectionDialog(true);
  public static final Action OPEN_SETUP_DIALOG = new OpenSetupAction();
  public static final Action OPEN_CHAT_DIALOG = new OpenChatAction();
  public static final Action CONNECTION_DIALOG_STATISTIC = new OpenConnectionDialog(false);
  public static final Action CONNECT = new ConnectionAction(true);
  public static final Action DISCONNECT = new ConnectionAction(false);

  private static class OpenConnectionDialog extends AbstractAction {
    private boolean editable;

    private OpenConnectionDialog(boolean editable) {
      this.editable = editable;
      String iconString = ResourceList.IMAGE_TOOLBAR_NETWORK_INFO;
      int virtualKey = KeyEvent.VK_I;
      if(editable) {
        iconString = ResourceList.IMAGE_TOOLBAR_NETWORK_EDIT;
        virtualKey = KeyEvent.VK_V;
      }
      WidgetCreator.initialiseAction(this, null, null, virtualKey, null,
          I18nSupport.getValue(CLIENT_BUNDLE, "action.name.connection.information"),
          null, ResourceGetter.getImage(iconString));
    }

    public void actionPerformed(ActionEvent e) {
      final ConnectionDialog dialog =
          new ConnectionDialog(!GameClient.getClient().isConnected());
      dialog.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
      dialog.setVisible(true);
    }
  }

  private static class ConnectionAction extends AbstractAction {
    private static final Logger LOGGER =
        LoggingUtility.getLogger(ConnectionAction.class.getName());
    private boolean connect;

    private ConnectionAction(boolean connect) {
      this.connect = connect;

      if(connect)
        WidgetCreator.initialiseAction(this, null, null, null, null,
            I18nSupport.getValue(CLIENT_BUNDLE, "action.name.connect"),
            I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.connect"),
            ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_NETWORK));
      else WidgetCreator.initialiseAction(this, null, null, null, null,
          I18nSupport.getValue(CLIENT_BUNDLE, "action.name.disconnect"),
          I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.disconnect"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_NETWORK_CLOSE));
    }

    public void actionPerformed(ActionEvent e) {
      if(connect) {
        connectClient();
      } else {
        ClientFrame.getInstance().resetAll(
            I18nSupport.getValue(MSGS_BUNDLE, "status.has.been.disconnected"), false);
      }
    }

    private void connectClient() {
      final ClientFrame mainFrame = ClientFrame.getInstance();
      final GameClient gameClient = GameClient.getClient();
      final ConnectionInfo connection = ConnectionInfo.getOwnInstance();

      try {
        if(gameClient.reconnect(connection.getServerAddress(), connection.getServerPort(),
            Client.getOwnInstance().toDTO(), connection.getPassword())) {
          final String message = I18nSupport.getValue(MSGS_BUNDLE, "status.connected");
          mainFrame.addChatMessage(message,true);
          mainFrame.setStatus(message, true, "["+gameClient.getSocketAddress()+"]");
        }
      } catch (GameClientException e) {
        final String message = e.getMessage();
        LOGGER.info("Connect action failed: "+message);
        WidgetCreator.createPopup(ClientGUIConstants.USER_MESSAGE_ERROR_COLOUR,
            message, mainFrame.getBounds(), DurakPopup.LOCATION_UP_LEFT, 3).setVisible(true);
        mainFrame.setStatus(message, false, "");
      }
    }
  }

  private static class OpenSetupAction extends AbstractAction {
    private OpenSetupAction() {
      WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_E, null,
          "", I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.open.setup"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_PINION));
    }

    public void actionPerformed(ActionEvent e) {
      //TODO Dialog wegen Einstellungen für die Oberfläche, Datenspeicherung usw öffnen
    }
  }

  private static class OpenChatAction extends AbstractAction {
    private OpenChatAction() {
      WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_C, null,
          "", I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.open.chat.frame"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_CHAT));
    }

    public void actionPerformed(ActionEvent e) {
      ChatFrame frame = ChatFrame.getFrame();
      if(!frame.isVisible())
        frame.setVisible(true);
      else frame.setVisible(false);
    }
  }
}