package client.gui.frame;

import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static common.i18n.BundleStrings.GUI_ACTION;
import static common.i18n.BundleStrings.GUI_TITLE;

/**
 * User: Timm Herrmann
 * Date: 22.01.13
 * Time: 19:22
 *
 * This is the default dialog class. The default dialog has a dialog content that may or may not
 * be changed, depending on the extending subclass. The content widgets, such as JLabel or
 * JCheckBox can be placed to the dialog content panel. The dialog content panel itself is
 * contained in the content pane of the DefaultDialog-object with a vertical box layout.
 * Under the dialog content panel is the button panel. The button panel has buttons with
 * default actions that are calling the default dialogs methods that should be overridden by the
 * subclass. The button panel contains the buttons with a {@link java.awt.FlowLayout#TRAILING} for
 * the {@link java.awt.FlowLayout}.
 * The okay button has a default action that calls the {@link #saveContent} and afterward the
 * {@link #closeDialog} method. The cancel button has a default action that calls the
 * {@link #resetContent} method and also {@code closeDialog}. The apply button just calls just the
 * {@link #saveContent} method.
 */
public abstract class AbstractDefaultDialog extends JDialog implements Changeable {
  private static final int ACTION_OKAY = 0;
  private static final int ACTION_CANCEL = 1;
  private static final int ACTION_APPLY = 2;

  protected String unchangedTitle;
  protected JButton okayButton;
  protected JButton cancelButton;
  protected JButton applyButton;

  private JPanel dialogContent;
  private JPanel buttonPanel;

  /* Constructors */

  public AbstractDefaultDialog() {
    final Container contentPane = getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
    contentPane.add(getDialogContent());
    contentPane.add(getButtonPanel());

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        resetContent();
        closeDialog();
        final Window window = e.getWindow();
        window.setVisible(false);
        window.dispose();
      }
    });
  }

  /* Methods */

  public void change() {
    final boolean changed = valuesHaveChanged();
    if(changed)
      setTitle(I18nSupport.getValue(GUI_TITLE, "0.changed", unchangedTitle));
    else setTitle(unchangedTitle);
    applyButton.setEnabled(changed);
  }

  protected void withApplyButton(boolean with) {
    applyButton.setVisible(with);
  }

  @SuppressWarnings("UnusedDeclaration")
  protected void withCancelButton(boolean with) {
    cancelButton.setVisible(with);
  }

  protected void withOkayButton(boolean with) {
    okayButton.setVisible(with);
  }

  abstract protected boolean valuesHaveChanged();

  /**
   * Saves the content of the content panel somewhere.
   */
  abstract void saveContent();

  /**
   * Resets the changed values in the dialog content panel.
   */
  abstract void resetContent();

  /**
   * Closes the dialog and does all necessary stuff that has to be done before closing.
   */
  abstract void closeDialog();

  /* Getter and Setter */

  public void setUnchangedTitle(String unchangedTitle) {
    this.unchangedTitle = unchangedTitle;
  }

  protected JPanel getDialogContent() {
    if(dialogContent != null)
      return dialogContent;

    dialogContent = new JPanel();
    return dialogContent;
  }

  protected JPanel getButtonPanel() {
    if(buttonPanel != null)
      return buttonPanel;

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));

    okayButton = new JButton(new DefaultAction(ACTION_OKAY));
    cancelButton = new JButton(new DefaultAction(ACTION_CANCEL));
    applyButton = new JButton(new DefaultAction(ACTION_APPLY));

    buttonPanel.add(okayButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(applyButton);

    return buttonPanel;
  }

  private class DefaultAction extends AbstractAction {
    private DefaultAction(Integer actionType) {
      String text = null, tooltip = null;
      Icon icon = null;
      if(actionType == ACTION_OKAY) {
        text = I18nSupport.getValue(GUI_ACTION, "name.default.okay");
        tooltip = I18nSupport.getValue(GUI_ACTION, "tooltip.default.okay");
        icon = ResourceGetter.getGeneralIcon("action.default.okay");
      } else if(actionType == ACTION_CANCEL) {
        text = I18nSupport.getValue(GUI_ACTION, "name.default.cancel");
        tooltip = I18nSupport.getValue(GUI_ACTION, "tooltip.default.cancel");
        icon = ResourceGetter.getGeneralIcon("action.default.cancel");
      } else if(actionType == ACTION_APPLY) {
        text = I18nSupport.getValue(GUI_ACTION, "name.default.apply");
        tooltip = I18nSupport.getValue(GUI_ACTION, "tooltip.default.apply");
        icon = ResourceGetter.getGeneralIcon("action.default.apply");
      }
      WidgetCreator.initialiseAction(this, null, null, null, actionType.toString(), text,
          tooltip, icon);
    }

    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(Integer.toString(ACTION_APPLY))) {
        saveContent();
      } else if(e.getActionCommand().equals(Integer.toString(ACTION_CANCEL))) {
        resetContent();
        closeDialog();
      } else if(e.getActionCommand().equals(Integer.toString(ACTION_OKAY))) {
        saveContent();
        closeDialog();
      }
    }
  }
}

/**
 * This listener can be registered by subcomponents in a dialog. As the class implements several
 * listeners an object of this class can be added to a subcomponent for the specified purpose, e.g.
 * for caret changes in a TextComponent. The dialog should implement the Changeable interface,
 * because its only method will be called in the listener method implementations.
 */
class DialogChangeListener implements ChangeListener, CaretListener, ActionListener {
  Changeable changeable;

  DialogChangeListener(Changeable changeable) {
    this.changeable = changeable;
  }

  public void actionPerformed(ActionEvent e) {
    changeable.change();
  }

  public void caretUpdate(CaretEvent e) {
    changeable.change();
  }

  public void stateChanged(ChangeEvent e) {
    changeable.change();
  }
}

interface Changeable {
  public void change();
}