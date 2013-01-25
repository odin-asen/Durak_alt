package client.gui;

import client.business.Client;
import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.business.client.GameClientException;
import client.gui.frame.ClientFrame;
import client.gui.frame.ConnectionDialog;
import client.gui.frame.SettingsDialog;
import client.gui.frame.chat.ChatFrame;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.utilities.LoggingUtility;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import static common.i18n.BundleStrings.CLIENT_GUI;
import static common.i18n.BundleStrings.USER_MESSAGES;

/**
 * User: Timm Herrmann
 * Date: 11.01.13
 * Time: 03:11
 */
public class ActionCollection {
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
    private OpenConnectionDialog(boolean editable) {
      String iconString = "toolbar.network.info";
      int virtualKey = KeyEvent.VK_I;
      if(editable) {
        iconString = "toolbar.network.edit";
        virtualKey = KeyEvent.VK_V;
      }
      WidgetCreator.initialiseAction(this, null, null, virtualKey, null,
          I18nSupport.getValue(CLIENT_GUI, "action.name.connection.information"),
          null, ResourceGetter.getToolbarIcon(iconString));
    }

    public void actionPerformed(ActionEvent e) {
      final ConnectionDialog dialog = new ConnectionDialog(!GameClient.getClient().isConnected());
      dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
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
            I18nSupport.getValue(CLIENT_GUI, "action.name.connect"),
            I18nSupport.getValue(CLIENT_GUI, "action.tooltip.connect"),
            ResourceGetter.getToolbarIcon("toolbar.network"));
      else WidgetCreator.initialiseAction(this, null, null, null, null,
          I18nSupport.getValue(CLIENT_GUI, "action.name.disconnect"),
          I18nSupport.getValue(CLIENT_GUI, "action.tooltip.disconnect"),
          ResourceGetter.getToolbarIcon("toolbar.network.close"));
    }

    public void actionPerformed(ActionEvent e) {
      if(connect) {
        connectClient();
      } else {
        ClientFrame.getInstance().resetAll(
            I18nSupport.getValue(USER_MESSAGES, "status.has.been.disconnected"), false);
      }
    }

    private void connectClient() {
      final ClientFrame mainFrame = ClientFrame.getInstance();
      final GameClient gameClient = GameClient.getClient();
      final ConnectionInfo connection = ConnectionInfo.getOwnInstance();

      try {
        if(gameClient.reconnect(connection.getServerAddress(), connection.getServerPort(),
            Client.getOwnInstance().toDTO(), connection.getPassword())) {
          final String message = I18nSupport.getValue(USER_MESSAGES, "status.connected");
          mainFrame.addChatMessage(message,true);
          mainFrame.setStatus(message, true, "["+gameClient.getSocketAddress()+"]");
        }
      } catch (GameClientException e) {
        final String message = e.getMessage();
        LOGGER.info("Connect action failed: "+message);
        mainFrame.showErrorPopup(message);
        mainFrame.setStatus(message, false, "");
      }
    }
  }

  private static class OpenSetupAction extends AbstractAction {
    private OpenSetupAction() {
      WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_E, null,
          "", I18nSupport.getValue(CLIENT_GUI, "action.tooltip.open.setup"),
          ResourceGetter.getToolbarIcon("toolbar.pinion"));
    }

    public void actionPerformed(ActionEvent e) {
      final SettingsDialog dialog = new SettingsDialog();
      dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      dialog.setVisible(true);
    }
  }

  private static class OpenChatAction extends AbstractAction {
    private OpenChatAction() {
      WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_C, null,
          "", I18nSupport.getValue(CLIENT_GUI, "action.tooltip.open.chat.frame"),
          ResourceGetter.getToolbarIcon("toolbar.chat"));
    }

    public void actionPerformed(ActionEvent e) {
      final ChatFrame frame = ChatFrame.getFrame();
      frame.setAlwaysOnTop(!frame.isVisible());
      frame.setVisible(!frame.isVisible());
    }
  }
}
