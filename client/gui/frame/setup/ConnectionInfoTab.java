package client.gui.frame.setup;

import client.business.ConnectionInfo;
import client.gui.frame.ClientGUIConstants;
import common.i18n.I18nSupport;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.gui.Constraints;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionInfoTab extends JPanel {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final Logger LOGGER = Logger.getLogger(ConnectionInfoTab.class.getName());

  private JComboBox<String> addressCombo;
  private JTextField portField;
  private JPasswordField passwordField;
  private JTextField plainTextField;
  private JCheckBox passwordCheckBox;
  private ConnectionInfo connectionInfo;
  private JLabel addressLabel;
  private JLabel portLabel;
  private JLabel passwordLabel;

  ConnectionInfoTab() {
    final int labelIPadX = 5;
    GridBagConstraints constraints;

    this.setLayout(new GridBagLayout());

    try {
      initComponents();
      constraints = Constraints.getDefaultFieldConstraintLeft(0, 0, 2, 1);
      constraints.ipadx = labelIPadX;
      add(addressLabel, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 0, 2, 1);
      add(addressCombo, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(0, 1, 2, 1);
      constraints.ipadx = labelIPadX;
      add(portLabel, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 1, 2, 1);
      add(portField, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(0, 2, 2, 1);
      constraints.ipadx = labelIPadX;
      add(passwordLabel, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 2, 2, 1);
      add(passwordField, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 2, 2, 1);
      add(plainTextField, constraints);
      constraints = Constraints.getDefaultFieldConstraintLeft(3, 3, 2, 1);
      add(passwordCheckBox, constraints);
    } catch (InstantiationException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    } catch (IllegalAccessException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    connectionInfo = new ConnectionInfo();
    fillConnectionInfo();
  }

  void fillConnectionInfo() {
    connectionInfo.setIpAddress(addressCombo.getSelectedItem().toString());
    connectionInfo.setPort(Integer.parseInt(portField.getText()));
    connectionInfo.setPassword(getPassword());
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

    addressLabel = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.server.address"));
    addressLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, addressLabel.getPreferredSize().height));

    comboBoxContent = new Vector<String>();
    addressCombo = WidgetCreator.makeComboBox(comboBoxContent,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE,"combo.box.tooltip.address"));

    try {
      addressCombo.addItem(InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    addressCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          final InetAddress address = InetAddress.getByName(addressCombo.getSelectedItem().toString());
          final String selected = address.getHostName();
          if (!comboBoxContent.contains(selected)) {
            addressCombo.addItem(selected);
            addressCombo.setSelectedItem(selected);
          }
        } catch (UnknownHostException e1) {
          LOGGER.log(Level.INFO, e1.getMessage());
          addressCombo.setSelectedIndex(0);
        }
      }
    });

    portLabel = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.port"));
    portLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, portLabel.getPreferredSize().height));

    portField = WidgetCreator.makeIntegerTextField(GameConfigurationConstants.DEFAULT_PORT_STRING,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE,"field.tooltip.port"));

    passwordLabel = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.password"));
    passwordLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordLabel.getPreferredSize().height));

    passwordField = (JPasswordField) WidgetCreator.makeTextField(JPasswordField.class,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE,"check.box.tooltip.show.password"));

    plainTextField = WidgetCreator.makeTextField(JTextField.class,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE,"check.box.tooltip.show.password"));

    passwordCheckBox = new JCheckBox(I18nSupport.getValue(CLIENT_BUNDLE,"check.box.show.password"));
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

  ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  public void resetInput() {
    addressCombo.setSelectedItem(connectionInfo.getIpAddress());
    setPassword(connectionInfo.getPassword());
    portField.setText(connectionInfo.getPort().toString());
  }
}