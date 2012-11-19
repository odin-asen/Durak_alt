package client.gui.frame.chat;

import client.business.client.GameClient;
import client.gui.frame.setup.SetUpFrame;
import dto.ClientInfo;
import utilities.gui.FramePosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static client.gui.frame.ClientGUIConstants.*;

/**
 * User: Timm Herrmann
 * Date: 03.11.12
 * Time: 01:02
 */
public class ChatFrame extends JDialog {
  private static ChatFrame chatFrame;

  private JToolBar toolBar;
  private JPanel centrePanel;
  private JPanel buttonPanel;
  private JButton sendButton;
  private ButtonListener buttonListener;

  private JTextArea chatReadArea;
  private JScrollPane scrollPaneRead;

  private JTextArea chatWriteArea;
  private JScrollPane scrollPaneWrite;

  private ChatMessageHandler chatMessageHandler;

  /* Constructors */
  public ChatFrame() {
    final FramePosition position = FramePosition.createFensterPositionen(
        CHAT_FRAME_SCREEN_SIZE_WIDTH, CHAT_FRAME_SCREEN_SIZE_HEIGHT);
    buttonListener = new ButtonListener();
    chatMessageHandler = new ChatMessageHandler();
    initComponents();

    this.setBounds(position.getRectangle());
    this.setTitle(CHAT_TITLE);
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  public static ChatFrame getFrame() {
    if(chatFrame == null) {
      chatFrame = new ChatFrame();
    }

    return chatFrame;
  }

  /* Methods */
  private void initComponents() {
    getContentPane().setLayout(new BorderLayout());
//    getContentPane().add(getToolBar(), BorderLayout.PAGE_START);
    getContentPane().add(getCentrePanel(), BorderLayout.CENTER);
  }

  public void addMessage(String text) {
    chatMessageHandler.addMessage(text);
  }

  /* Getter and Setter */
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
    scrollPaneWrite.setPreferredSize(new Dimension(scrollPaneWrite.getPreferredSize().width,
        CHAT_WRITE_AREA_HEIGHT));
    scrollPaneWrite.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        scrollPaneWrite.getPreferredSize().height));
    scrollPaneWrite.setName(SCROLL_WRITE_NAME);

    return scrollPaneWrite;
  }

  private JPanel getCentrePanel() {
    if(centrePanel != null)
      return centrePanel;

    centrePanel = new JPanel();
    centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.PAGE_AXIS));
    centrePanel.add(getScrollPaneRead());
    centrePanel.add(getScrollPaneWrite());
    centrePanel.add(getButtonPanel());

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
    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        sendButton.getPreferredSize().height+10));
    buttonPanel.setName(BUTTON_PANEL_NAME);

    return buttonPanel;
  }

  private JToolBar getToolBar() {
    if(toolBar != null)
      return toolBar;

    toolBar = new JToolBar();
    toolBar.setFloatable(true);
    toolBar.setBorderPainted(false);
    return toolBar;
  }

  /* Inner Classes */
  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(BUTTON_NAME_SEND)) {
        new Thread(new Runnable() {
          public void run() {
            String text = chatWriteArea.getText();
            chatMessageHandler.sendChatMessage(text);
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

  private class ChatMessageHandler {
    public void addMessage(String text) {
      chatReadArea.append(text + '\n');
      scrollPaneRead.getVerticalScrollBar().setValue(chatFrame.getScrollPaneRead().getVerticalScrollBar().getMaximum());
    }

    public void sendChatMessage(String text) {
      if (!text.isEmpty()) {
        ClientInfo info = SetUpFrame.getInstance().getClientInfo();
        try {
          GameClient.getClient().getChatHandler().sendMessage(info, text);
          chatWriteArea.setText("");
        } catch (Exception e) {
          JOptionPane.showMessageDialog(
              chatFrame, "Die Nachricht konnte nicht gesendet werden!" +
              "\nM\u00f6glicherweise besteht keine Verbindung zum Server!",
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }
}
