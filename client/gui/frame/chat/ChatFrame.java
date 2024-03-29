package client.gui.frame.chat;

import client.business.client.GameClient;
import common.i18n.I18nSupport;
import common.utilities.gui.FramePosition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static client.gui.frame.ClientGUIConstants.*;
import static common.i18n.BundleStrings.GUI_COMPONENT;
import static common.i18n.BundleStrings.GUI_TITLE;

/**
 * User: Timm Herrmann
 * Date: 03.11.12
 * Time: 01:02
 */
public class ChatFrame extends JDialog {
  private static final String ACTION_COMMAND_SEND = "send"; //NON-NLS
  private static ChatFrame chatFrame;

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
    final FramePosition position = FramePosition.createFramePositions(
        CHAT_FRAME_SCREEN_SIZE_WIDTH, CHAT_FRAME_SCREEN_SIZE_HEIGHT);
    buttonListener = new ButtonListener();
    chatMessageHandler = new ChatMessageHandler();
    initComponents();

    setBounds(position.getRectangle());
    setTitle(I18nSupport.getValue(GUI_TITLE, "chat"));
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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

    sendButton = new JButton(I18nSupport.getValue(GUI_COMPONENT, "text.send"));
    sendButton.setActionCommand(ACTION_COMMAND_SEND);
    sendButton.addActionListener(buttonListener);

    buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
    buttonPanel.add(sendButton);
    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        sendButton.getPreferredSize().height + 10));

    return buttonPanel;
  }

  /* Inner Classes */
  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(ACTION_COMMAND_SEND)) {
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
    private static final char NEWLINE = '\n';

    public void addMessage(String text) {
      chatReadArea.append(text + NEWLINE);
      scrollPaneRead.getVerticalScrollBar().setValue(chatFrame.getScrollPaneRead().getVerticalScrollBar().getMaximum());
    }

    public void sendChatMessage(String text) {
      if (!text.isEmpty()) {
        final GameClient client = GameClient.getClient();
        if(client.isConnected()) {
          client.sendChatMessage(text);
          chatWriteArea.setText("");
        } else {
          JOptionPane.showMessageDialog(chatFrame,
              I18nSupport.getValue(GUI_COMPONENT, "text.error.chat.message.not.send"),
              I18nSupport.getValue(GUI_TITLE, "error"), JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }
}
