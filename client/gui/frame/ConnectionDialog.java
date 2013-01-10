package client.gui.frame;

import client.business.ConnectionInfo;
import client.business.Client;
import client.business.client.GameClient;
import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 05.01.13
 * Time: 22:15
 */
public class ConnectionDialog extends JDialog {
  private static final Logger LOGGER =
      LoggingUtility.getLogger(ConnectionDialog.class.getName());

  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String ACTION_COMMAND_CONNECT = "connect"; //NON-NLS
  private static final String ACTION_COMMAND_CANCEL = "cancel";  //NON-NLS
  private static final String ACTION_COMMAND_CLOSE_SAVE = "closeSave";  //NON-NLS
  private static final String ACTION_COMMAND_CLOSE = "close"; //NON-NLS
  private static final int STRUT_HEIGHT = 5;
  private static final int STRUT_WIDTH = 5;

  /* Some parameters have a component that will be used to edit this parameter and
   * another component if the parameter should be not editable. E.g. for the
   * server address, the editable component is a combo box and the non editable is a
   * label. */
  private JComboBox<String> serverAddressCombo;
  private JLabel serverAddressLabel;
  private JTextField serverPortField;
  private JLabel serverPortLabel;
  private JTextField passwordField;
  private JTextField nameField;
  private JLabel nameLabel;
  private JCheckBox spectatorCheckBox;
  private JLabel spectatorLabel;
  private JPanel buttonPanel;

  private boolean editable;

  /* Constructors */

  public ConnectionDialog(boolean editable) {
    this.editable = editable;

    /* initialise gui stuff */
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
    getContentPane().add(getServerInfoPanel());
    getContentPane().add(Box.createGlue());
    getContentPane().add(getClientInfoPanel());
    getContentPane().add(Box.createGlue());
    getContentPane().add(getButtonPanel());
    resetFields();

    /* initialise frame */
    final FramePosition position = FramePosition.createFramePositions(
        getContentPane().getPreferredSize().width,
        getContentPane().getPreferredSize().height);

    this.setBounds(position.getRectangle());

    this.setTitle(I18nSupport.getValue(CLIENT_BUNDLE, "frame.title.setup"));
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.pack();
  }

  public static void main(String[] args) {
    new ConnectionDialog(true).setVisible(true);
    new ConnectionDialog(false).setVisible(true);
  }


  /* Methods */

  private void fillConnectionInfo() {
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    connectionInfo.setServerAddress(serverAddressCombo.getSelectedItem().toString());
    connectionInfo.setServerPort(Integer.parseInt(serverPortField.getText()));
    connectionInfo.setPassword(passwordField.getText());
  }

  private void fillClientInfo() {
    final Client client = Client.getOwnInstance();
    client.setName(nameField.getText());
    client.setSpectating(spectatorCheckBox.isSelected());
  }

  /* Getter and Setter */

  private JPanel getServerInfoPanel() {
    final JPanel mainPanel = new JPanel();

    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_BUNDLE,"border.title.server.settings")));

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getServerAddressPanel());
    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getServerPortPanel());
    if(editable) {
      mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
      mainPanel.add(getPasswordPanel());
    }

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    return mainPanel;
  }

  private JPanel getPasswordPanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.password"));
    label.setMaximumSize(label.getPreferredSize());
    try {
      passwordField = WidgetCreator.makeTextField(JPasswordField.class,
          ClientGUIConstants.PREFERRED_FIELD_WIDTH,
          I18nSupport.getValue(CLIENT_BUNDLE, "check.box.tooltip.show.password"));
      panel.setLayout(new GridLayout(1, 2));
      panel.add(label);
      panel.add(passwordField);
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    } catch (Exception e) {
      LOGGER.severe("Error creating password panel.\nMessage: "+e.getMessage());
    }
    return panel;
  }

  private JPanel getServerPortPanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.port"));
    label.setMaximumSize(label.getPreferredSize());
    serverPortField = WidgetCreator.makeIntegerTextField("",
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "field.tooltip.server.port"));
    serverPortLabel = new JLabel("");

    panel.setLayout(new GridLayout(1, 2));
    panel.add(label);
    if(editable)
      panel.add(serverPortField);
    else panel.add(serverPortLabel);

    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private JPanel getServerAddressPanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.address"));
    label.setMaximumSize(label.getPreferredSize());
    final Vector<String> comboBoxContent = new Vector<String>();
    serverAddressCombo = WidgetCreator.makeComboBox(comboBoxContent, 3,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "combo.box.tooltip.server.address"));
    serverAddressCombo.setMaximumSize(serverAddressCombo.getPreferredSize());
    serverAddressCombo.addActionListener(new IPComboBoxListener(serverAddressCombo, comboBoxContent));
    serverAddressLabel = new JLabel("");

    panel.setLayout(new GridLayout(1,2));
    panel.add(label);
    if(editable)
      panel.add(serverAddressCombo);
    else panel.add(serverAddressLabel);

    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private JPanel getClientInfoPanel() {
    final JPanel mainPanel = new JPanel();

    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_BUNDLE, "border.title.client.settings")));

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getClientNamePanel());
    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getSpectatorPanel());

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    return mainPanel;
  }

  private JPanel getSpectatorPanel() {
    final JPanel panel = new JPanel();
    spectatorCheckBox = new JCheckBox(
        I18nSupport.getValue(CLIENT_BUNDLE,"check.box.spectator"));
    spectatorCheckBox.setSelected(false);
    spectatorLabel = new JLabel("");

    panel.setLayout(new GridLayout(1,2));
    if(editable) {
      panel.add(Box.createHorizontalStrut(STRUT_WIDTH));
      panel.add(spectatorCheckBox);
    } else {
      panel.add(new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.status")));
      panel.add(spectatorLabel);
    }
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  private JPanel getClientNamePanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.player.name"));
    label.setMaximumSize(label.getPreferredSize());
    nameField = new JTextField(Client.getOwnInstance().getName());
    nameLabel = new JLabel("");

    panel.setLayout(new GridLayout(1, 2));
    panel.add(label);
    if(editable)
      panel.add(nameField);
    else panel.add(nameLabel);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  private JPanel getButtonPanel() {
    if(buttonPanel != null)
      return buttonPanel;

    buttonPanel = new JPanel();

    buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
    if(editable) {
      buttonPanel.add(createConnectionButton());
      buttonPanel.add(WidgetCreator.makeButton(null,
          I18nSupport.getValue(CLIENT_BUNDLE, "button.text.close.save"),
          I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.close.save"), null,
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              saveAndClose();
            }
          }));
      buttonPanel.add(WidgetCreator.makeButton(null,
          I18nSupport.getValue(CLIENT_BUNDLE, "button.text.cancel"),
          I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.cancel"), null,
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              resetFields();
              setVisible(false);
              dispose();
            }
          }));
    } else {
      buttonPanel.add(WidgetCreator.makeButton(null,
          I18nSupport.getValue(CLIENT_BUNDLE, "button.text.close"), null, null,
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              setVisible(false);
              dispose();
            }
          }));
    }

    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));

    return buttonPanel;
  }

  private JButton createConnectionButton() {
    final JButton button = new JButton();
    /* It seems that the action listener that will be added last, will be executed first.
       * Therefore the fill-methods listener will be added last. */
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(GameClient.getClient().isConnected()) {
          setVisible(false);
          dispose();
        }
      }
    });
    button.setAction(new ConnectionAction(true));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fillClientInfo();
        fillConnectionInfo();
      }
    });

    return button;
  }

  private void saveAndClose() {
    fillClientInfo();
    fillConnectionInfo();
    setVisible(false);
    dispose();
  }

  public void resetFields() {
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    final Client client = Client.getOwnInstance();

    if(editable) {
      /* server fields */
      serverAddressCombo.setSelectedItem(connectionInfo.getServerAddress());
      serverPortField.setText(connectionInfo.getServerPort().toString());
      passwordField.setText(connectionInfo.getPassword());
      /* client fields */
      nameField.setText(client.getName());
      spectatorCheckBox.setSelected(client.getSpectating());
    } else {
      /* server fields */
      serverAddressLabel.setText(connectionInfo.getServerAddress());
      serverPortLabel.setText(connectionInfo.getServerPort().toString());
      /* client fields */
      nameLabel.setText(client.getName());
      String key = "label.text.spectator."+client.getSpectating().toString(); //NON-NLS
      spectatorLabel.setText(I18nSupport.getValue(CLIENT_BUNDLE, key));
    }
  }

  /* Inner Classes */
}

class IPComboBoxListener implements ActionListener {
  private JComboBox<String> comboBox;
  private Vector<String> comboBoxContent;
  IPComboBoxListener(JComboBox<String> comboBox, Vector<String> comboBoxContent) {
    this.comboBox = comboBox;
    this.comboBoxContent = comboBoxContent;
  }

  public void actionPerformed(ActionEvent e) {
    final String selected = comboBox.getSelectedItem().toString();
    try {
      InetAddress address = InetAddress.getByName(selected);
      final String hostAddress = address.getHostAddress();
      if (!comboBoxContent.contains(hostAddress)) {
        comboBox.addItem(hostAddress);
        comboBox.setSelectedItem(hostAddress);
      } else comboBox.setSelectedIndex(comboBoxContent.indexOf(hostAddress));
    } catch (UnknownHostException e1) {
      comboBox.setSelectedIndex(comboBox.getItemCount()-1);
    }
  }
}