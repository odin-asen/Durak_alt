package common.utilities.gui;

import common.resources.ResourceGetter;
import common.utilities.LoggingUtility;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 20.10.12
 * Time: 01:06
 */
public class WidgetCreator {
  private static final Logger LOGGER = LoggingUtility.getLogger(WidgetCreator.class.getName());

  public static <T> JComboBox<T> makeComboBox(Vector<T> comboBoxContent, int maxRowCount,
                                               int preferredWidth, String toolTipText) {
    final JComboBox<T> comboBox = new JComboBox<T>(comboBoxContent);

    comboBox.setEditable(true);
    comboBox.setToolTipText(toolTipText);
    comboBox.setPreferredSize(new Dimension(preferredWidth, comboBox.getPreferredSize().height));
    comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboBox.getPreferredSize().height));
    comboBox.setMaximumRowCount(maxRowCount);

    return comboBox;
  }

  public static <T extends JTextField> T makeTextField(Class<T> fieldClass,
      int preferredWidth, String toolTipText)
      throws IllegalAccessException, InstantiationException {
    final T field = fieldClass.newInstance();
    field.setPreferredSize(new Dimension(preferredWidth, field.getPreferredSize().height));
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
    field.setToolTipText(toolTipText);

    return field;
  }

  public static JTextField makeIntegerTextField(String text, int preferredWidth, String toolTipText) {
    final NumberFormat format = NumberFormat.getNumberInstance();
    final JTextField field = new JFormattedTextField(format);

    format.setMaximumFractionDigits(0);
    format.setGroupingUsed(false);

    field.setText(text);
    field.setToolTipText(toolTipText);
    field.setPreferredSize(new Dimension(preferredWidth, field.getPreferredSize().height));
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));

    return field;
  }

  public static JButton makeButton(Icon icon, String text, String toolTipText,
                                   String actionCommand, ActionListener listener) {
    JButton button = new JButton();

    button.setText(text);
    button.setToolTipText(toolTipText);
    button.setActionCommand(actionCommand);
    button.setIcon(icon);
    button.addActionListener(listener);
    button.setPreferredSize(button.getPreferredSize());
    button.setMaximumSize(button.getPreferredSize());

    return button;
  }

  public static void changeButton(AbstractButton button, Icon icon, String text,
                                  String actionCommand, String toolTipText) {
    if(button == null)
      return;

    button.setText(text);
    button.setActionCommand(actionCommand);
    button.setIcon(icon);
    button.setToolTipText(toolTipText);
  }

  public static Border createStatusBorder() {
    final Border lowered = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    return lowered;
  }

  public static Border createPopupBorder() {
    Border line = BorderFactory.createLineBorder(Color.BLACK);
    Border compound = BorderFactory.createCompoundBorder(
        BorderFactory.createBevelBorder(BevelBorder.RAISED),
        BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    return BorderFactory.createCompoundBorder(line, compound);
  }

  /**
   * Creates a popup window with the specified background colour, the message text and
   * the time it is open. The popup pops out at a specified location depending on the top frame
   * bounds.
   * @param backgroundColour Specifies the background colour of the window.
   * @param text Text that will be shown.
   * @param topFrameBounds Frame that indicates the popup area.
   * @param popupLocation Location within the frame bounds to pop up. One of the DurakPopup constants.
   * @param openSeconds Time the popup will be opaque until it disappears.
   * @return
   */
  public static DurakPopup createPopup(Color backgroundColour, String text,
                                       Rectangle topFrameBounds, int popupLocation,
                                       double openSeconds) {
    final DurakPopup popup = new DurakPopup(backgroundColour, new JLabel(text),
        topFrameBounds, popupLocation);
    popup.setOpenSeconds(openSeconds);
    return popup;
  }

  public static DurakPopup createPopup(Color backgroundColour, String text, Action buttonAction,
                                       boolean closeAfterAction, Rectangle topFrameBounds,
                                       int popupLocation, double openSeconds) {
    final JPanel panel = new JPanel();
    final JLabel label = new JLabel(text);
    final JButton button = new JButton();

    button.setAction(buttonAction);
    label.setBackground(backgroundColour);
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.add(label);
    panel.add(button);

    final DurakPopup popup = new DurakPopup(backgroundColour, panel, topFrameBounds, popupLocation);
    popup.setOpenSeconds(openSeconds);
    if(closeAfterAction) {
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          popup.setVisible(false);
          popup.dispose();
        }
      });
    }
    return popup;
  }

  public static void doAction(Object source, Action action) {
    final ActionEvent event = new ActionEvent(source, 0,
        (String) action.getValue(Action.ACTION_COMMAND_KEY));
    action.actionPerformed(event);
  }

  public static void initialiseAction(Action action, KeyStroke accelerator,
                                      String longDescription, Integer mnemonicVirtualKey,
                                      String actionCommand, String text,
                                      String shortDescription, Icon smallIcon) {
    action.putValue(Action.ACCELERATOR_KEY, accelerator);
    action.putValue(Action.LONG_DESCRIPTION, longDescription);
    action.putValue(Action.MNEMONIC_KEY, mnemonicVirtualKey);
    action.putValue(Action.ACTION_COMMAND_KEY, actionCommand);
    action.putValue(Action.NAME, text);
    action.putValue(Action.SHORT_DESCRIPTION, shortDescription);
    action.putValue(Action.SMALL_ICON, smallIcon);
  }

  public static Action createActionCopy(final Action action) {
    Action copy = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        WidgetCreator.doAction(e.getSource(), action);
      }
    };
    copy.putValue(Action.ACCELERATOR_KEY, action.getValue(Action.ACCELERATOR_KEY));
    copy.putValue(Action.LONG_DESCRIPTION, action.getValue(Action.LONG_DESCRIPTION));
    copy.putValue(Action.MNEMONIC_KEY, action.getValue(Action.MNEMONIC_KEY));
    copy.putValue(Action.ACTION_COMMAND_KEY, action.getValue(Action.ACTION_COMMAND_KEY));
    copy.putValue(Action.NAME, action.getValue(Action.NAME));
    copy.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));
    copy.putValue(Action.SMALL_ICON, action.getValue(Action.SMALL_ICON));
    return copy;
  }
}
