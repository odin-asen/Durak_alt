package client.gui;

import resources.ResourceGetter;
import resources.ResourceGetterException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * User: Timm Herrmann
 * Date: 02.10.12
 * Time: 20:44
 */
public class DurakToolBar extends JToolBar {
  public static final String ACTION_COMMAND_CONNECTION = "connection";
  public static final String ACTION_COMMAND_SETUP = "setup";
  public static final String ACTION_COMMAND_CLOSE = "close";

  private JFrame parent;
  private JButton connectionButton;
  private JButton setUpButton;
  private JButton closeButton;

  public DurakToolBar(JFrame parent) {
    this.parent = parent;
    connectionButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_NETWORK, "Verbindung zu Server aufbauen",
        ACTION_COMMAND_CONNECTION, "Verbindung", KeyEvent.VK_V);
    setUpButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_PINION, "\u00d6ffnet Fenster f\u00fcr Einstellungen",
        ACTION_COMMAND_SETUP, "Einstellungen", KeyEvent.VK_E);
    closeButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_CLOSE,"Schlie\u00dft die Anwendung",
        ACTION_COMMAND_CLOSE,"Schlie\u00dfen", KeyEvent.VK_Q);

    this.setMargin(new Insets(5,5,5,5));
    this.setRollover(true);

    this.add(connectionButton);
    this.addSeparator();
    this.add(setUpButton);
    this.add(Box.createHorizontalGlue());
    this.addSeparator();
    this.add(closeButton);
  }

  private JButton makeToolBarButton(String pictureName, String toolTipText,
                                    String actionCommand, String alternativeText,
                                    int virtualKey) {
    JButton button = new JButton();
    button.setToolTipText(toolTipText);
    button.setActionCommand(actionCommand);
    button.setMnemonic(virtualKey);
    button.addActionListener(new ToolBarComponentAL());
    try {
      button.setIcon(ResourceGetter.loadImage(pictureName, alternativeText));
    } catch (ResourceGetterException e) {
      e.printStackTrace();
    }
    if(button.getIcon() == null)
      button.setText(alternativeText);

    return button;
  }

  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        parent.setVisible(false);
        parent.dispose();
        System.exit(0);
      } else if(ACTION_COMMAND_CONNECTION.equals(e.getActionCommand())) {

      } else if(ACTION_COMMAND_SETUP.equals(e.getActionCommand())) {
        SetUpFrame frame = SetUpFrame.getInstance();
        if(!frame.isVisible())
          frame.setVisible(true);
      }
    }
  }
}
