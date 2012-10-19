package client.gui.frame;

import client.business.GameClient;
import dto.message.MessageObject;
import dto.message.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * User: Timm Herrmann
 * Date: 19.10.12
 * Time: 12:30
 *
 * This class creates the chat environment for the game. The LayoutManager should not
 * be modified after creation otherwise, the pinning function of the window won't work.
 */
public class ChatPanel extends JPanel {
  public static final int CHAT_WRITE_AREA_HEIGHT = 70;
  public static final String BUTTON_NAME_SEND = "Senden";
  public static final int CENTRE_PANEL_WIDTH = 200;
  public static final String SCROLL_READ_NAME = "Lesen";
  public static final String SCROLL_WRITE_NAME = "Schreiben";
  public static final String BUTTON_PANEL_NAME = "Kn\u00f6pfe";

  private JToolBar pinBar;

  private CentrePanel centrePanel;

  private JPanel buttonPanel;
  private ButtonListener buttonListener;
  private JButton sendButton;

  @SuppressWarnings("FieldCanBeLocal")
  private JTextArea chatReadArea;
  private JScrollPane scrollPaneRead;

  private JTextArea chatWriteArea;
  private JScrollPane scrollPaneWrite;

  /* Constructors */
  public ChatPanel() {
    this(BorderLayout.PAGE_START);
    getPinBar().setFloatable(false);
  }

  /**
   * Creates the chat panel with the initial location of the pin bar.
   * @param pinBarLocation Specified location of the pin bar. It should be
   *                       one of the BorderLayout constants:
   *                       <ul>
   *                        <li>{@link java.awt.BorderLayout#PAGE_START}</li>
   *                        <li>{@link java.awt.BorderLayout#PAGE_END}</li>
   *                        <li>{@link java.awt.BorderLayout#LINE_START}</li>
   *                        <li>{@link java.awt.BorderLayout#LINE_END}</li>
   *                       </ul>
   */
  public ChatPanel(String pinBarLocation) {
    buttonListener = new ButtonListener();
    this.setLayout(new BorderLayout());
    this.add(getCentrePanel(), BorderLayout.CENTER);
    this.add(getPinBar(), pinBarLocation);
  }

  /* Methods */
  private JToggleButton makePin(final Component pinComponent, String text, boolean selected) {
    final JToggleButton button = new JToggleButton(text, selected);
    button.setMaximumSize(new Dimension(70, 20));
    button.setToolTipText(text);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pinComponent.setVisible(button.isSelected());
      }
    });

    return button;
  }

  public void addMessage(String text) {
    chatReadArea.append(text);
  }

  /* Getter and Setter */
  private JToolBar getPinBar() {
    if(pinBar != null)
      return pinBar;

    pinBar = new JToolBar();
    pinBar.setFloatable(true);
    pinBar.setBorderPainted(false);
    return pinBar;
  }

  private CentrePanel getCentrePanel() {
    if(centrePanel != null)
      return centrePanel;

    centrePanel = new CentrePanel();
    centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.PAGE_AXIS));
    centrePanel.add(getScrollPaneRead());
    centrePanel.add(getScrollPaneWrite());
    centrePanel.add(getButtonPanel());
    centrePanel.setPreferredSize(new Dimension(CENTRE_PANEL_WIDTH, centrePanel.getPreferredSize().height));

    return centrePanel;
  }

  private JPanel getButtonPanel() {
    if(buttonPanel != null)
      return buttonPanel;

    buttonPanel = new JPanel();

    sendButton = new JButton(BUTTON_NAME_SEND);
    sendButton.setActionCommand(BUTTON_NAME_SEND);
    sendButton.addActionListener(buttonListener);
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(sendButton);
    buttonPanel.setPreferredSize(new Dimension(CENTRE_PANEL_WIDTH, sendButton.getPreferredSize().height+10));
    buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());
    buttonPanel.setName(BUTTON_PANEL_NAME);

    return buttonPanel;
  }

  private JScrollPane getScrollPaneRead() {
    if(scrollPaneRead != null)
      return scrollPaneRead;
    chatWriteArea = new JTextArea();
    chatReadArea = new JTextArea();
    chatReadArea.setEditable(false);
    chatReadArea.setLineWrap(true);
    chatReadArea.setWrapStyleWord(true);
    scrollPaneRead = new JScrollPane();
    scrollPaneRead.setViewportView(chatReadArea);
    scrollPaneRead.setName(SCROLL_READ_NAME);

    return scrollPaneRead;
  }

  private JScrollPane getScrollPaneWrite() {
    if(scrollPaneWrite != null)
      return scrollPaneWrite;

    scrollPaneWrite = new JScrollPane();

    chatWriteArea.addKeyListener(new KeyboardListener());
    chatWriteArea.setLineWrap(true);
    chatWriteArea.setWrapStyleWord(true);

    scrollPaneWrite.setViewportView(chatWriteArea);
    scrollPaneWrite.setPreferredSize(new Dimension(CENTRE_PANEL_WIDTH, CHAT_WRITE_AREA_HEIGHT));
    scrollPaneWrite.setMaximumSize(new Dimension(Integer.MAX_VALUE, scrollPaneWrite.getPreferredSize().height));
    scrollPaneWrite.setName(SCROLL_WRITE_NAME);

    return scrollPaneWrite;
  }

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(BUTTON_NAME_SEND)) {
        new Thread(new Runnable() {
          public void run() {
            String text = chatWriteArea.getText();
            chatWriteArea.setText("");
            GameClient.getClient().send(new MessageObject(MessageType.CHAT_MESSAGE, text));
          }
        }).start();
      }
    }
  }

  private class KeyboardListener implements KeyListener {
    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
      if(e.getModifiers() == KeyEvent.CTRL_MASK) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendButton.doClick();
        }
      }
    }

    public void keyReleased(KeyEvent e) {
    }
  }

  private class CentrePanel extends JPanel {
    public Component add(Component comp) {
      Component component = super.add(comp);
      getPinBar().add(makePin(comp, comp.getName(),true));
      return component;
    }

    public void add(Component comp, Object constraints) {
      super.add(comp, constraints);
      getPinBar().add(makePin(comp, comp.getName(),true));
    }

    public void add(Component comp, Object constraints, int index) {
      super.add(comp, constraints, index);
      getPinBar().add(makePin(comp, comp.getName(),true));
    }

    public Component add(Component comp, int index) {
      Component component = super.add(comp, index);
      getPinBar().add(makePin(comp, comp.getName(),true));
      return component;
    }

    public Component add(String name, Component comp) {
      Component component = super.add(name, comp);
      getPinBar().add(makePin(comp, name,true));
      return component;
    }
  }
}
