package client.gui.frame;

import client.business.Client;
import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.business.client.GameClientException;
import client.gui.frame.chat.ChatFrame;
import common.dto.DTOClient;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.resources.ResourceList;
import common.utilities.LoggingUtility;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 02.10.12
 * Time: 20:44
 */
public class DurakToolBar extends JToolBar {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String MESSAGE_BUNDLE = "user.messages"; //NON-NLS

  private static final Logger LOGGER = LoggingUtility.getLogger(DurakToolBar.class.getName());
  
  private static final String ACTION_COMMAND_CONNECTION_SETTINGS = "connection"; //NON-NLS
  private static final String ACTION_COMMAND_SETUP = "setup";  //NON-NLS
  private static final String ACTION_COMMAND_CHAT = "chat";  //NON-NLS

  private JButton connectionButton;
  private JPopupMenu connectionPopup;
  private JMenuItem connectionMenuItem;
  private Action connectAction;
  private Action disconnectAction;

  public DurakToolBar() {
    setMargin(new Insets(5, 5, 5, 5));
    setRollover(true);

    connectAction = new ConnectionAction(true);
    disconnectAction = new ConnectionAction(false);
    connectionPopup = getConnectionPopup();
    addButtons();
  }

  private JPopupMenu getConnectionPopup() {
    if(connectionPopup != null)
      return connectionPopup;

    connectionPopup = new JPopupMenu();
    connectionMenuItem = new JMenuItem(connectAction);
    connectionPopup.add(connectionMenuItem);

    return connectionPopup;
  }

  private void addButtons() {
    /* connection button with popup menu */
    connectionButton = new JButton(new OpenDialogAction(true));
    connectionButton.setText("");
    connectionButton.addMouseListener(new MouseAdapter() {
      boolean pressed = false;
      public void mousePressed(MouseEvent e) {
        pressed = true;
      }
      public void mouseReleased(MouseEvent e) {
        if (pressed && SwingUtilities.isRightMouseButton(e))
          getConnectionPopup().show(e.getComponent(), e.getX(), e.getY());
        pressed = false;
      }
      public void mouseExited(MouseEvent e) {
        pressed = false;
      }
      public void mouseEntered(MouseEvent e) {
        pressed = true;
      }
    });

    add(connectionButton);
    addSeparator();
    add(new JButton(new OpenSetupAction()));
    addSeparator();
    add(new JButton(new OpenChatAction()));
  }

  public void setConnection(boolean connected) {
    if(connected) {
      connectionMenuItem.setAction(new OpenDialogAction(false));
      connectionButton.setAction(disconnectAction);
      connectionButton.setText("");
    } else {
      connectionMenuItem.setAction(connectAction);
      connectionButton.setAction(new OpenDialogAction(true));
      connectionButton.setText("");
    }
  }


  /***************************/
  /***** Toolbar actions *****/
  /***************************/

  private class OpenSetupAction extends AbstractAction {
    private OpenSetupAction() {
      WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_E, ACTION_COMMAND_SETUP,
          "", I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.open.setup"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_PINION));
    }

    public void actionPerformed(ActionEvent e) {
      //TODO Dialog wegen Einstellungen für die Oberfläche, Datenspeicherung usw öffnen
    }
  }

  private class OpenChatAction extends AbstractAction {
    private OpenChatAction() {
      WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_C, ACTION_COMMAND_CHAT,
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

  private class OpenDialogAction extends AbstractAction {
    private boolean editable;

    private OpenDialogAction(boolean editable) {
      this.editable = editable;
      String iconString = ResourceList.IMAGE_TOOLBAR_NETWORK_INFO;
      int virtualKey = KeyEvent.VK_I;
      if(editable) {
        iconString = ResourceList.IMAGE_TOOLBAR_NETWORK_EDIT;
        virtualKey = KeyEvent.VK_V;
      }
      WidgetCreator.initialiseAction(this,null,null, virtualKey,
          ACTION_COMMAND_CONNECTION_SETTINGS,
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
      WidgetCreator.initialiseAction(this, null, null, null, AC_CONNECT,
          I18nSupport.getValue(CLIENT_BUNDLE, "action.name.connect"),
          I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.connect"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_NETWORK));
    else WidgetCreator.initialiseAction(this, null, null, null, AC_DISCONNECT,
        I18nSupport.getValue(CLIENT_BUNDLE, "action.name.disconnect"),
        I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.disconnect"),
        ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_NETWORK_CLOSE));
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals(AC_CONNECT)) {
      connectClient();
    } else if(e.getActionCommand().equals(AC_DISCONNECT)) {
      mainFrame.resetAll(I18nSupport.getValue(MSGS_BUNDLE, "status.has.been.disconnected"), false);
    }
  }

  private void connectClient() {
    final GameClient gameClient = GameClient.getClient();
    final ConnectionInfo connection = ConnectionInfo.getOwnInstance();

    try {
      if(gameClient.reconnect(connection.getServerAddress(), connection.getServerPort(),
          Client.getOwnInstance().toDTO(), connection.getPassword()))
        mainFrame.setStatus(I18nSupport.getValue(MSGS_BUNDLE, "status.connected"), true,
            "["+gameClient.getSocketAddress()+"]");
    } catch (GameClientException e) {
      LOGGER.info("Connect action failed: "+e.getMessage());
      mainFrame.setStatus(e.getMessage(), false, "");
    }
  }
}
