package client.gui.frame;

import client.business.Client;
import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.gui.ActionCollection;
import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Logger;

import static common.i18n.BundleStrings.CLIENT_GUI;

/**
 * User: Timm Herrmann
 * Date: 05.01.13
 * Time: 22:15
 *
 * This dialog shows the information of the {@link client.business.Client} and
 * {@link client.business.ConnectionInfo} objects. It can either be a dialog that makes the
 * objects of the Client and ConnectionInfo classes modifyable or it just shows their attributes.
 */
public class ConnectionDialog extends AbstractDefaultDialog {
  private static final Logger LOGGER =
      LoggingUtility.getLogger(ConnectionDialog.class.getName());

  private static final int STRUT_HEIGHT = 5;
  private static final int STRUT_WIDTH = 5;

  private JTextField passwordField;
  private TwoStateComponent<JComboBox<String>,JLabel,String> serverAddressField;
  private TwoStateComponent<JTextField,JLabel,String> serverPortField;
  private TwoStateComponent<JTextField,JLabel,String> nameField;
  private TwoStateComponent<JCheckBox,JLabel,Boolean> spectatorField;

  private boolean editable;
  private DialogChangeListener dialogChangeListener;

  /* Constructors */

  public ConnectionDialog(boolean editable) {
    this.editable = editable;

    /* initialise gui stuff */
    final JPanel dialogContent = getDialogContent();

    dialogContent.setLayout(new BoxLayout(dialogContent, BoxLayout.PAGE_AXIS));
    dialogContent.add(createMainPanel(I18nSupport.getValue(CLIENT_GUI, "border.settings.server"),
        getServerAddressPanel(), getServerPortPanel(), getPasswordPanel()));
    dialogContent.add(createMainPanel(I18nSupport.getValue(CLIENT_GUI, "border.settings.client"),
        getClientNamePanel(), getSpectatorPanel()));

    if(editable) setConnectionButton();
    withApplyButton(editable);
    withOkayButton(editable);

    resetContent();

    /* initialise frame */
    final FramePosition position = FramePosition.createFramePositions(
        getContentPane().getPreferredSize().width,
        getContentPane().getPreferredSize().height);

    setBounds(position.getRectangle());
    setResizable(false);
    setUnchangedTitle(I18nSupport.getValue(CLIENT_GUI, "dialog.title.connection"));
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    pack();

    initChangeListener();
  }

  public static void main(String[] args) {
    new ConnectionDialog(true).setVisible(true);
    new ConnectionDialog(false).setVisible(true);
  }

  /* Methods */

  private void initChangeListener() {
    dialogChangeListener = new DialogChangeListener(this);
    nameField.getFirstComponent().addCaretListener(dialogChangeListener);
    serverAddressField.getFirstComponent().addActionListener(dialogChangeListener);
    serverPortField.getFirstComponent().addCaretListener(dialogChangeListener);
    spectatorField.getFirstComponent().addActionListener(dialogChangeListener);
    if(editable)
      passwordField.addCaretListener(dialogChangeListener);
    change();
  }

  private void setConnectionButton() {
    for (ActionListener listener : okayButton.getActionListeners()) {
      okayButton.removeActionListener(listener);
    }
    /* It seems that the action listener that will be added last, will be executed first. */
    /* Therefore the fill-methods listener will be added last. */
    okayButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(GameClient.getClient().isConnected()) {
          closeDialog();
        }
      }
    });
    okayButton.setAction(ActionCollection.CONNECT);
    okayButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveContent();
      }
    });
  }

  void saveContent() {
    /* save connection info */
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    connectionInfo.setServerAddress(serverAddressField.getValue());
    connectionInfo.setServerPort(Integer.parseInt(serverPortField.getValue()));
    connectionInfo.setPassword(passwordField.getText());

    /* save client info */
    final Client client = Client.getOwnInstance();
    client.setName(nameField.getValue());
    client.setSpectating(spectatorField.getValue());
    change();
  }

  void resetContent() {
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    final Client client = Client.getOwnInstance();

    serverAddressField.setValue(connectionInfo.getServerAddress());
    serverPortField.setValue(connectionInfo.getServerPort().toString());
    if(editable) passwordField.setText(connectionInfo.getPassword());
    nameField.setValue(client.getName());
    spectatorField.setValue(client.getSpectating());
    change();
  }

  void closeDialog() {
    setVisible(false);
    dispose();
  }

  private ValueAccessor createTextLabelAccessor(final JTextComponent field, final JLabel label) {
    return new ValueAccessor<String>() {
      public String getValue() {
        return field.getText();
      }
      public void setValue(String value) {
        field.setText(value);
        label.setText(value);
      }
    };
  }

  protected boolean valuesHaveChanged() {
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    final Client client = Client.getOwnInstance();

    return !serverAddressField.getValue().equals(connectionInfo.getServerAddress())
        || !serverPortField.getValue().equals(connectionInfo.getServerPort().toString())
        || !passwordField.getText().equals(connectionInfo.getPassword())
        || !nameField.getValue().equals(client.getName())
        || !spectatorField.getValue().equals(client.getSpectating());
  }

  private JPanel createHorizontalPanel(String labelString, Component... components) {
    final JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(1, 2));
    if(labelString != null) {
      final JLabel label = new JLabel(labelString);
      label.setMaximumSize(label.getPreferredSize());
      panel.add(label);
    }
    for(Component component : components)
      panel.add(component);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  private JPanel createMainPanel(String borderTitle, JPanel... panels) {
    final JPanel mainPanel = new JPanel();

    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder(borderTitle));

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    for (JPanel panel : panels) {
      mainPanel.add(panel);
      mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    }
    return mainPanel;
  }

  /* Getter and Setter */

  private JPanel getPasswordPanel() {
    try {
      passwordField = WidgetCreator.makeTextField(JPasswordField.class,
          ClientGUIConstants.PREFERRED_FIELD_WIDTH,
          I18nSupport.getValue(CLIENT_GUI, "checkbox.tooltip.password"));
    } catch (Exception e) {
      LOGGER.severe("Error creating password panel.\nMessage: "+e.getMessage());
    }

    if(!editable)
      return new JPanel();

    return createHorizontalPanel(I18nSupport.getValue(CLIENT_GUI, "label.text.password"),
        passwordField);
  }

  private JPanel getServerPortPanel() {
    final JTextField field = WidgetCreator.makeIntegerTextField("",
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_GUI, "field.tooltip.server.port"));
    final JLabel valueLabel = new JLabel("");
    final ValueAccessor<String> accessor = createTextLabelAccessor(field, valueLabel);
    serverPortField = new TwoStateComponent<JTextField,JLabel,String>(field, valueLabel, accessor);

    return createHorizontalPanel(I18nSupport.getValue(CLIENT_GUI, "label.text.port"),
        serverPortField.getComponent(editable));
  }

  private JPanel getServerAddressPanel() {
    final Vector<String> comboBoxContent = new Vector<String>();
    final JComboBox<String> combobox = WidgetCreator.makeComboBox(
        comboBoxContent, 3, ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_GUI, "combobox.tooltip.server.address"));
    combobox.addActionListener(new IPComboBoxListener(combobox, comboBoxContent));
    final JLabel valueLabel = new JLabel("");
    final ValueAccessor<String> accessor = new ValueAccessor<String>() {
      public String getValue() {return combobox.getSelectedItem().toString();}
      public void setValue(String value) {
        valueLabel.setText(value);
        combobox.setSelectedItem(value);
      }
    };
    serverAddressField = new TwoStateComponent<JComboBox<String>,JLabel,String>(
        combobox,valueLabel,accessor);

    return createHorizontalPanel(I18nSupport.getValue(CLIENT_GUI, "label.text.address"),
        serverAddressField.getComponent(editable));
  }

  private JPanel getSpectatorPanel() {
    final JCheckBox checkbox = new JCheckBox(
        I18nSupport.getValue(CLIENT_GUI, "checkbox.text.spectator"));
    final JLabel valueLabel = new JLabel("");
    final ValueAccessor<Boolean> accessor = new ValueAccessor<Boolean>() {
      public Boolean getValue() {return checkbox.isSelected();}
      public void setValue(Boolean value) {
        String key = "label.text.spectator."+value.toString(); //NON-NLS
        valueLabel.setText(I18nSupport.getValue(CLIENT_GUI, key));
        checkbox.setSelected(value);
      }
    };
    spectatorField = new TwoStateComponent<JCheckBox,JLabel,Boolean>(checkbox,valueLabel,accessor);

    final JPanel panel;

    if(editable)
      panel = createHorizontalPanel(null, Box.createHorizontalStrut(STRUT_WIDTH),
          spectatorField.getComponent(editable));
    else panel = createHorizontalPanel(I18nSupport.getValue(CLIENT_GUI, "label.text.status"),
        spectatorField.getComponent(editable));

    return panel;
  }

  private JPanel getClientNamePanel() {
    final JTextField field = new JTextField(Client.getOwnInstance().getName());
    final JLabel valueLabel = new JLabel("");
    final ValueAccessor<String> accessor = createTextLabelAccessor(field, valueLabel);
    nameField = new TwoStateComponent<JTextField, JLabel, String>(field, valueLabel, accessor);

    return createHorizontalPanel(I18nSupport.getValue(CLIENT_GUI, "label.text.player.name"),
        nameField.getComponent(editable));
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

class TwoStateComponent<A extends JComponent, B extends JComponent,T> {
  private A firstComponent;
  private B secondComponent;
  private ValueAccessor<T> valueAccessor;

  TwoStateComponent(A firstComponent, B secondComponent, ValueAccessor<T> accessor) {
    this.firstComponent = firstComponent;
    this.secondComponent = secondComponent;
    this.valueAccessor = accessor;
  }

  public JComponent getComponent(boolean firstState) {
    if(firstState) return firstComponent;
    else return secondComponent;
  }

  public A getFirstComponent() {
    return firstComponent;
  }

  public B getSecondComponent() {
    return secondComponent;
  }

  public T getValue() {
    return valueAccessor.getValue();
  }

  public void setValue(T value) {
    valueAccessor.setValue(value);
  }
}

interface ValueAccessor<T> {
  public T getValue();
  public void setValue(T value);
}