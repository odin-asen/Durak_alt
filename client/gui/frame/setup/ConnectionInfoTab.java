package client.gui.frame.setup;

import client.business.ConnectionInfo;
import client.gui.frame.ClientGUIConstants;
import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;
import common.utilities.Miscellaneous;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.gui.Constraints;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionInfoTab extends JPanel {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final Logger LOGGER = LoggingUtility.getLogger(ConnectionInfoTab.class.getName());

  private JComboBox<String> serverAddressCombo;
  private JComboBox<String> clientAddressCombo;
  private JTextField serverPortField;
  private JTextField clientPortField;
  private JPasswordField passwordField;
  private JTextField plainTextField;
  private JCheckBox passwordCheckBox;
  private ConnectionInfo connectionInfo;
  private JLabel serverAddressLabel;
  private JLabel clientAddressLabel;
  private JLabel serverPortLabel;
  private JLabel clientPortLabel;
  private JLabel passwordLabel;

  ConnectionInfoTab() {
    final int labelIPadX = 5;
    GridBagConstraints constraints;

    this.setLayout(new GridBagLayout());

    try {
      initComponents();
      /* server settings */
      constraints = Constraints.getDefaultFieldConstraintLeft(0, 0, 2, 1);
      constraints.ipadx = labelIPadX;
      add(serverAddressLabel, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 0, 2, 1);
      add(serverAddressCombo, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(0, 1, 2, 1);
      constraints.ipadx = labelIPadX;
      add(serverPortLabel, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 1, 2, 1);
      add(serverPortField, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(0, 2, 2, 1);
      constraints.ipadx = labelIPadX;
      add(passwordLabel, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 2, 2, 1);
      add(passwordField, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 2, 2, 1);
      add(plainTextField, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 3, 2, 1);
      add(passwordCheckBox, constraints);

      /* client settings */
      constraints = Constraints.getDefaultFieldConstraintLeft(0, 4, 2, 1);
      constraints.ipadx = labelIPadX;
      add(clientAddressLabel, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 4, 2, 1);
      add(clientAddressCombo, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(0, 5, 2, 1);
      constraints.ipadx = labelIPadX;
      add(clientPortLabel, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 5, 2, 1);
      add(clientPortField, constraints);
    } catch (InstantiationException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    } catch (IllegalAccessException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    connectionInfo = new ConnectionInfo();
    fillConnectionInfo();
  }

  void fillConnectionInfo() {
    connectionInfo.setServerAddress(serverAddressCombo.getSelectedItem().toString());
    connectionInfo.setServerPort(Integer.parseInt(serverPortField.getText()));
    connectionInfo.setPassword(getPassword());
    connectionInfo.setClientAddress(clientAddressCombo.getSelectedItem().toString());
    connectionInfo.setClientPort(Integer.parseInt(clientPortField.getText()));
  }

  private void setPassword(String password) {
    plainTextField.setText(password);
    passwordField.setText(password);
  }

  String getPassword() {
    if (passwordCheckBox.isSelected())
      return plainTextField.getText();
    else
      return String.copyValueOf(passwordField.getPassword());
  }

  void initComponents() throws InstantiationException, IllegalAccessException {
    final Vector<String> comboBoxContent;

    initServerAddressField();
    initServerPortField();
    initPasswordField();
    initPasswordCheckBox();
    initClientAddressField();
    initClientPortField();
  }

  private void initClientAddressField() {
    final Vector<String> comboBoxContent = new Vector<String>(3);
    clientAddressLabel = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE,
        "label.text.client.address"));
    clientAddressLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        serverAddressLabel.getPreferredSize().height));

    clientAddressCombo = WidgetCreator.makeComboBox(comboBoxContent,3,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "combo.box.tooltip.client.address"));
    try {
      final InetAddress address = Miscellaneous.getHostInetAddress(Inet4Address.class);
      clientAddressCombo.addItem(address.getHostAddress());
    } catch (Exception e) {
      clientAddressCombo.addItem(InetAddress.getLoopbackAddress().getHostAddress());
    }

    clientAddressCombo.addActionListener(new IPComboBoxListener(
        clientAddressCombo,comboBoxContent));
  }

  private void initClientPortField() {
    clientPortLabel = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.client.port"));
    clientPortLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, serverPortLabel.getPreferredSize().height));

    clientPortField = WidgetCreator.makeIntegerTextField(GameConfigurationConstants.DEFAULT_PORT_STRING,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE, "field.tooltip.client.port"));
  }

  private void initPasswordCheckBox() {
    passwordCheckBox = new JCheckBox(I18nSupport.getValue(CLIENT_BUNDLE, "check.box.show.password"));
    passwordCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordCheckBox.getPreferredSize().height));

    passwordCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (((AbstractButton) e.getSource()).isSelected()) {
          plainTextField.setText(String.copyValueOf(passwordField.getPassword()));
          passwordField.setVisible(false);
          plainTextField.setVisible(true);
        } else {
          passwordField.setText(plainTextField.getText());
          plainTextField.setVisible(false);
          passwordField.setVisible(true);
        }
      }
    });
  }

  private void initPasswordField() throws IllegalAccessException, InstantiationException {
    passwordLabel = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.password"));
    passwordLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordLabel.getPreferredSize().height));

    passwordField = (JPasswordField) WidgetCreator.makeTextField(JPasswordField.class,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE, "check.box.tooltip.show.password"));

    plainTextField = WidgetCreator.makeTextField(JTextField.class,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE,"check.box.tooltip.show.password"));
  }

  private void initServerPortField() {
    serverPortLabel = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.server.port"));
    serverPortLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, serverPortLabel.getPreferredSize().height));

    serverPortField = WidgetCreator.makeIntegerTextField(GameConfigurationConstants.DEFAULT_PORT_STRING,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE, "field.tooltip.server.port"));
  }

  private void initServerAddressField() {
    final Vector<String> comboBoxContent;
    serverAddressLabel = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.server.address"));
    serverAddressLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, serverAddressLabel.getPreferredSize().height));

    comboBoxContent = new Vector<String>();
    serverAddressCombo = WidgetCreator.makeComboBox(comboBoxContent, 3,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "combo.box.tooltip.server.address"));

    try {
      final InetAddress address = Miscellaneous.getHostInetAddress(Inet4Address.class);
      serverAddressCombo.addItem(address.getHostAddress());
    } catch (Exception e) {
      serverAddressCombo.addItem(InetAddress.getLoopbackAddress().getHostAddress());
    }

    serverAddressCombo.addActionListener(new IPComboBoxListener(
        serverAddressCombo,comboBoxContent));
  }

  ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  public void resetInput() {
    serverAddressCombo.setSelectedItem(connectionInfo.getServerAddress());
    setPassword(connectionInfo.getPassword());
    serverPortField.setText(connectionInfo.getServerPort().toString());
    clientAddressCombo.setSelectedItem(connectionInfo.getClientAddress());
    clientPortField.setText(connectionInfo.getClientPort().toString());
  }
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