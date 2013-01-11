package client.gui.frame;

import client.gui.ActionCollection;
import common.utilities.LoggingUtility;

import javax.swing.*;
import java.awt.*;
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
    connectionMenuItem = new JMenuItem(ActionCollection.CONNECT);
    connectionPopup.add(connectionMenuItem);

    return connectionPopup;
  }

  private void addButtons() {
    /* connection button with popup menu */
    connectionButton = new JButton(ActionCollection.CONNECTION_DIALOG_EDITABLE);
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
    add(new JButton(ActionCollection.OPEN_SETUP_DIALOG));
    addSeparator();
    add(new JButton(ActionCollection.OPEN_CHAT_DIALOG));
  }

  public void setConnection(boolean connected) {
    if(connected) {
      connectionMenuItem.setAction(ActionCollection.CONNECTION_DIALOG_STATISTIC);
      connectionButton.setAction(ActionCollection.DISCONNECT);
      connectionButton.setText("");
    } else {
      connectionMenuItem.setAction(ActionCollection.CONNECT);
      connectionButton.setAction(ActionCollection.CONNECTION_DIALOG_EDITABLE);
      connectionButton.setText("");
    }
  }
}