package client.gui;

import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.business.client.GameClientException;
import client.gui.frame.ClientFrame;
import client.gui.frame.ConnectionDialog;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.resources.ResourceList;
import common.utilities.LoggingUtility;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 06.01.13
 * Time: 02:18
 *
 * This class is a factory that provides different Action objects. These objects are used
 * for different controls that provide the same funcionality.
 */
public class ActionFactory {
  public static Action getConnectAction() {
    return new ConnectionAction(true);
  }

  public static Action getDisconnectAction() {
    return new ConnectionAction(false);
  }

  public static void doAction(Object source, Action action) {
    final ActionEvent event = new ActionEvent(source, 0,
        (String) action.getValue(Action.ACTION_COMMAND_KEY));
    action.actionPerformed(event);
  }
}

class ConnectionAction extends AbstractAction {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS
  private static final Logger LOGGER =
      LoggingUtility.getLogger(ConnectionAction.class.getName());

  private static final String AC_CONNECT = "connect"; //NON-NLS
  private static final String AC_DISCONNECT = "disconnect"; //NON-NLS
  private ClientFrame mainFrame;

  ConnectionAction(boolean connect) {
    mainFrame = ClientFrame.getInstance();
    if(connect)
      putConnectValues();
    else putDisconnectValues();
  }

  private void putConnectValues() {
    putValue(ACCELERATOR_KEY, null);
    putValue(LONG_DESCRIPTION, null);
    putValue(MNEMONIC_KEY, null);
    putValue(ACTION_COMMAND_KEY, AC_CONNECT); //NON-NLS
    putValue(NAME, I18nSupport.getValue(CLIENT_BUNDLE, "action.name.connect"));
    putValue(SHORT_DESCRIPTION,
        I18nSupport.getValue(CLIENT_BUNDLE, "action.short.description.connect"));
    putValue(SMALL_ICON, ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_NETWORK,
        I18nSupport.getValue(CLIENT_BUNDLE, "image.description.connect")));
  }

  private void putDisconnectValues() {
    putValue(ACCELERATOR_KEY, null);
    putValue(LONG_DESCRIPTION, null);
    putValue(MNEMONIC_KEY, null);
    putValue(ACTION_COMMAND_KEY, AC_DISCONNECT); //NON-NLS
    putValue(NAME, I18nSupport.getValue(CLIENT_BUNDLE, "action.name.disconnect"));
    putValue(SHORT_DESCRIPTION,
        I18nSupport.getValue(CLIENT_BUNDLE, "action.short.description.disconnect"));
    putValue(SMALL_ICON, ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_NETWORK_CLOSE,
        I18nSupport.getValue(CLIENT_BUNDLE, "image.description.disconnect")));
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals(AC_CONNECT)) {
      connectClient();
    } else if(e.getActionCommand().equals(AC_DISCONNECT)) {
      disconnectClient();
    }
  }

  private void disconnectClient() {
    final GameClient client = GameClient.getClient();
    final ConnectionDialog dialog = ConnectionDialog.getInstance();

    client.disconnect(dialog.getClientInfo());
    mainFrame.setStatus(I18nSupport.getValue(MSGS_BUNDLE, "status.has.been.disconnected"),
        false, "");
    mainFrame.clearClients();
  }

  private void connectClient() {
    final GameClient client = GameClient.getClient();
    final ConnectionDialog dialog = ConnectionDialog.getInstance();

    if(client.isConnected())
      client.disconnect(dialog.getClientInfo());

    try {
      final ConnectionInfo connection = dialog.getConnectionInfo();
      client.setConnection(connection);

      dialog.getClientInfo().ipAddress = connection.getClientAddress();
      dialog.getClientInfo().port = connection.getClientPort();

      final String socketString =
          "["+connection.getServerAddress()+":"+ connection.getServerPort()+"]";
      client.connect(dialog.getClientInfo(), connection.getPassword());
      mainFrame.setStatus(I18nSupport.getValue(MSGS_BUNDLE, "status.connected"),
          true, socketString);
    } catch (GameClientException e) {
      mainFrame.setStatus(e.getMessage(), false, "");
    } catch (Exception e) {
      LOGGER.severe(e.getMessage());
      mainFrame.setStatus(I18nSupport.getValue(MSGS_BUNDLE, "status.connection.failed"),
          false, "");
    }
  }
}
