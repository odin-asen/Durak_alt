package client.gui.frame;

import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.business.client.GameClientException;
import client.gui.ActionFactory;
import client.gui.frame.chat.ChatFrame;
import common.i18n.I18nSupport;
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
  private ActionListener toolbarListener;

  public DurakToolBar() {
    setMargin(new Insets(5, 5, 5, 5));
    setRollover(true);

    toolbarListener = new ToolBarComponentAL();
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
    connectionButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_NETWORK,
        I18nSupport.getValue(CLIENT_BUNDLE, "action.short.description.connect"),
        ACTION_COMMAND_CONNECTION_SETTINGS, I18nSupport.getValue(CLIENT_BUNDLE,
        "image.description.connect"), toolbarListener, KeyEvent.VK_V);
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

    /* other buttons */
    JButton setUpButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_PINION,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.open.setup"),
        ACTION_COMMAND_SETUP, I18nSupport.getValue(CLIENT_BUNDLE,"image.description.setup"),
        toolbarListener, KeyEvent.VK_E);
    JButton chatButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CHAT,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.open.close.chat.frame"),
        ACTION_COMMAND_CHAT, I18nSupport.getValue(CLIENT_BUNDLE,"image.description.chat"),
        toolbarListener, KeyEvent.VK_C);
    JButton closeButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CLOSE,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.close.application"),
        ACTION_COMMAND_CLOSE, I18nSupport.getValue(CLIENT_BUNDLE,"image.description.close"),
        toolbarListener, KeyEvent.VK_Q);

    add(connectionButton);
    addSeparator();
    add(setUpButton);
    addSeparator();
    add(chatButton);
    add(Box.createGlue());
    addSeparator();
    add(closeButton);
  }

  public void setConnection(boolean connected) {
    if(connected) {
      connectionMenuItem.setText(I18nSupport.getValue(CLIENT_BUNDLE,
          "menu.popup.show.connection"));
      connectionMenuItem.setActionCommand(ACTION_COMMAND_CONNECTION_SETTINGS);
      connectionButton.setAction(ActionFactory.getDisconnectAction());
    } else {
      connectionMenuItem.setAction(ActionFactory.getConnectAction());
      WidgetCreator.changeButton(connectionButton, ResourceList.IMAGE_TOOLBAR_NETWORK,
          ACTION_COMMAND_CONNECTION_SETTINGS, null,
          I18nSupport.getValue(CLIENT_BUNDLE,"image.description.connect"));
    }
  }

  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        close(e.getSource());
      } else if(ACTION_COMMAND_SETUP.equals(e.getActionCommand())) {
        //TODO Dialog wegen Einstellungen für die Oberfläche, Datenspeicherung usw öffnen
      } else if(ACTION_COMMAND_CHAT.equals(e.getActionCommand())) {
        ChatFrame frame = ChatFrame.getFrame();
        if(!frame.isVisible())
          frame.setVisible(true);
        else frame.setVisible(false);
      } else if(ACTION_COMMAND_CONNECTION_SETTINGS.equals(e.getActionCommand())) {
        ConnectionDialog dialog = ConnectionDialog.getInstance(!GameClient.getClient().isConnected());
        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setVisible(true);
      }
    }
  //TODO alle ClientInfo.toString().equals(ClientInfo.toString()) durch Miscalaneous.CLIENT_COMPARATOR.compare ersetzen
    private void close(Object source) {
      ActionFactory.doAction(source, ActionFactory.getDisconnectAction());
      ClientFrame.getInstance().setVisible(false);
      ClientFrame.getInstance().dispose();
      System.exit(0);
    }
  }
}
