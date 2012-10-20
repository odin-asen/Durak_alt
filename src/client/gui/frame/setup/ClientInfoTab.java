package client.gui.frame.setup;

import client.gui.frame.ClientGUIConstants;
import dto.message.ClientInfo;
import utilities.gui.Constraints;
import utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputMethodListener;

public class ClientInfoTab extends JPanel {
  private JTextField nameField;
  private JCheckBox startPlayingCheckBox;

  private ClientInfo clientInfo;
  private JLabel nameLabel;

  private InputMethodListener inputListener;

  ClientInfoTab() {
    final int labelIPadX = 5;

    inputListener = WidgetCreator.createTextChangeNotifyListener(SetUpFrame.class,
        "setChanged", new Class<?>[]{Boolean.class}, false);

    this.setLayout(new GridBagLayout());
    initComponents();
    GridBagConstraints constraints;

    constraints = Constraints.getDefaultFieldConstraintLeft(0, 0, 2, 1);
    constraints.ipadx = labelIPadX;
    this.add(nameLabel, constraints);
    constraints = Constraints.getDefaultFieldConstraintLeft(3, 0, 2, 1);
    this.add(nameField, constraints);
    constraints = Constraints.getDefaultFieldConstraintLeft(3, 3, 2, 1);
    this.add(startPlayingCheckBox, constraints);

    clientInfo = new ClientInfo("");
    fillClientInfo();
  }

  void fillClientInfo() {
    clientInfo.startPlayingFlag = startPlayingCheckBox.isSelected();
    clientInfo.clientName = nameField.getText();
  }

  void initComponents() {
    nameLabel = new JLabel("Spielername:");
    nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameLabel.getPreferredSize().height));

    String defaultText = System.getProperty("user.name");
    if (defaultText == null)
      defaultText = "anonymus";

    nameField = new JTextField(defaultText);
    nameField.setPreferredSize(new Dimension(ClientGUIConstants.PREFERRED_FIELD_WIDTH, nameField.getPreferredSize().height));
    nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));
    nameField.addInputMethodListener(inputListener);
    startPlayingCheckBox = new JCheckBox(ClientGUIConstants.SET_UP_CHECKBOX_PLAYING_TEXT);
    startPlayingCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, startPlayingCheckBox.getPreferredSize().height));
    startPlayingCheckBox.setToolTipText(ClientGUIConstants.SET_UP_CHECKBOX_PLAYING_TOOLTIP);
    startPlayingCheckBox.addInputMethodListener(inputListener);
  }

  public ClientInfo getClientInfo() {
    return clientInfo;
  }
}