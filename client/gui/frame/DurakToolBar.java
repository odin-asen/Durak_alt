package client.gui.frame;

import client.business.client.GameClient;
import client.gui.ActionFactory;
import client.gui.frame.chat.ChatFrame;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.resources.ResourceList;
import common.utilities.LoggingUtility;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
  
  private static final String ACTION_COMMAND_CLOSE = "close";  //NON-NLS
  private static final String ACTION_COMMAND_CONNECTION_SETTINGS = "connection"; //NON-NLS
  private static final String ACTION_COMMAND_SETUP = "setup";  //NON-NLS
  private static final String ACTION_COMMAND_CHAT = "chat";  //NON-NLS

  private JButton connectionButton;
  private JPopupMenu connectionPopup;
  private JMenuItem connectionMenuItem;

  public DurakToolBar() {
    setMargin(new Insets(5, 5, 5, 5));
    setRollover(true);

    connectionPopup = getConnectionPopup();
    addButtons();
  }

  private JPopupMenu getConnectionPopup() {
    if(connectionPopup != null)
      return connectionPopup;

    connectionPopup = new JPopupMenu();
    connectionMenuItem = new JMenuItem(ActionFactory.getConnectAction());
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
    add(Box.createGlue());
    addSeparator();
    add(new JButton(new CloseApplicationAction()));
  }
                       //TODO image.description aus properties entfernen, da unnötig
  public void setConnection(boolean connected) {
    if(connected) {
      connectionMenuItem.setAction(new OpenDialogAction(false));
      connectionButton.setAction(ActionFactory.getDisconnectAction());
      connectionButton.setText("");
    } else {
      connectionMenuItem.setAction(ActionFactory.getConnectAction());
      connectionButton.setAction(new OpenDialogAction(true));
      connectionButton.setText("");
    }
  }

  //TODO alle ClientInfo.toString().equals(ClientInfo.toString()) durch Miscalaneous.CLIENT_COMPARATOR.compare ersetzen

  /***************************/
  /***** Toolbar actions *****/
  /***************************/

  private class OpenSetupAction extends AbstractAction {
    private OpenSetupAction() {
      ActionFactory.initialiseAction(this, null, null, KeyEvent.VK_E, ACTION_COMMAND_SETUP,
          "", I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.open.setup"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_PINION, ""));
    }

    public void actionPerformed(ActionEvent e) {
      //TODO Dialog wegen Einstellungen für die Oberfläche, Datenspeicherung usw öffnen
    }
  }

  private class OpenChatAction extends AbstractAction {
    private OpenChatAction() {
      ActionFactory.initialiseAction(this, null, null, KeyEvent.VK_C, ACTION_COMMAND_CHAT,
          "", I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.open.chat.frame"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_CHAT, ""));
    }

    public void actionPerformed(ActionEvent e) {
      ChatFrame frame = ChatFrame.getFrame();
      if(!frame.isVisible())
        frame.setVisible(true);
      else frame.setVisible(false);
    }
  }

  private class CloseApplicationAction extends AbstractAction {
    private CloseApplicationAction() {
      ActionFactory.initialiseAction(this, null, null, KeyEvent.VK_Q, ACTION_COMMAND_CLOSE,
          "", I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.close.application"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_CLOSE, ""));
    }

    public void actionPerformed(ActionEvent e) {
      ActionFactory.doAction(e.getSource(), ActionFactory.getDisconnectAction());
      ClientFrame.getInstance().setVisible(false);
      ClientFrame.getInstance().dispose();
      System.exit(0);
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
      ActionFactory.initialiseAction(this,null,null, virtualKey,
          ACTION_COMMAND_CONNECTION_SETTINGS,
          I18nSupport.getValue(CLIENT_BUNDLE, "action.name.connection.information"),
          null, ResourceGetter.getImage(iconString, ""));
    }

    public void actionPerformed(ActionEvent e) {
      final ConnectionDialog dialog =
          new ConnectionDialog(!GameClient.getClient().isConnected());
      dialog.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
      dialog.setVisible(true);
    }
  }
}
