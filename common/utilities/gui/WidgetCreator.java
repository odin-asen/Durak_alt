package common.utilities.gui;

import common.resources.ResourceGetter;
import common.utilities.LoggingUtility;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
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

  public static JButton makeToolBarButton(String pictureName, String toolTipText,
                                    String actionCommand, ActionListener listener,
                                    int virtualKey) {
    JButton button = new JButton();
    button.setToolTipText(toolTipText);
    button.setActionCommand(actionCommand);
    button.setMnemonic(virtualKey);
    button.addActionListener(listener);
    button.setIcon(ResourceGetter.getImage(pictureName));

    return button;
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

  public static void changeButton(AbstractButton button, String pictureName, String text,
                                  String actionCommand, String toolTipText) {
    if(button == null)
      return;

    button.setText(text);
    button.setActionCommand(actionCommand);
    if(pictureName != null) {
      ImageIcon icon = ResourceGetter.getImage(pictureName);
      button.setIcon(icon);
    }
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

  public static DurakPopup createPopup(Color backgroundColour, String text,
                                       Rectangle topFrameBounds, double openSeconds) {
    final DurakPopup popup = new DurakPopup(backgroundColour);
    popup.setText(text);
    popup.setSize(popup.getPrefferedSize());
    popup.setLocation(topFrameBounds.x + topFrameBounds.width - popup.getWidth() - 20,
        topFrameBounds.y + topFrameBounds.height - popup.getHeight() - 20);
    popup.setOpenSeconds(openSeconds);

    return popup;
  }
}
