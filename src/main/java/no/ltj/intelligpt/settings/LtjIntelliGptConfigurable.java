package no.ltj.intelligpt.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class LtjIntelliGptConfigurable implements Configurable {

    private JPanel rootPanel;
    private JTextField apiKeyField;
    private JTextField modelField;
    private JCheckBox selectionOnlyCheckbox;

    private final LtjIntelliGptSettings settings;

    public LtjIntelliGptConfigurable() {
        this.settings = LtjIntelliGptSettings.getInstance();
    }

    @Override
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getDisplayName() {
        return "LTJ IntelliGPT";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (rootPanel == null) {
            rootPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;

            int row = 0;

            // API key label
            c.gridx = 0;
            c.gridy = row;
            c.weightx = 0;
            rootPanel.add(new JLabel("OpenAI API key:"), c);

            // API key field
            apiKeyField = new JTextField();
            c.gridx = 1;
            c.weightx = 1.0;
            rootPanel.add(apiKeyField, c);

            row++;

            // Model label
            c.gridx = 0;
            c.gridy = row;
            c.weightx = 0;
            rootPanel.add(new JLabel("Model (f.eks. gpt-4.1-mini):"), c);

            // Model field
            modelField = new JTextField();
            c.gridx = 1;
            c.weightx = 1.0;
            rootPanel.add(modelField, c);

            row++;

            // Selection only checkbox
            c.gridx = 0;
            c.gridy = row;
            c.gridwidth = 2;
            selectionOnlyCheckbox = new JCheckBox("Send bare markert tekst hvis noe er markert");
            rootPanel.add(selectionOnlyCheckbox, c);
        }

        reset();
        return rootPanel;
    }

    @Override
    public boolean isModified() {
        if (apiKeyField == null || modelField == null || selectionOnlyCheckbox == null) {
            return false;
        }
        boolean apiDifferent = !apiKeyField.getText().trim().equals(settings.getApiKey());
        boolean modelDifferent = !modelField.getText().trim().equals(settings.getModel());
        boolean selectionDifferent = selectionOnlyCheckbox.isSelected() != settings.isSendEditorSelectionOnly();
        return apiDifferent || modelDifferent || selectionDifferent;
    }

    @Override
    public void apply() {
        if (apiKeyField == null || modelField == null || selectionOnlyCheckbox == null) {
            return;
        }
        settings.setApiKey(apiKeyField.getText());
        settings.setModel(modelField.getText());
        settings.setSendEditorSelectionOnly(selectionOnlyCheckbox.isSelected());
    }

    @Override
    public void reset() {
        if (apiKeyField == null || modelField == null || selectionOnlyCheckbox == null) {
            return;
        }
        apiKeyField.setText(settings.getApiKey());
        modelField.setText(settings.getModel());
        selectionOnlyCheckbox.setSelected(settings.isSendEditorSelectionOnly());
    }

    @Override
    public void disposeUIResources() {
        rootPanel = null;
        apiKeyField = null;
        modelField = null;
        selectionOnlyCheckbox = null;
    }
}
