package no.ltj.intelligpt.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class IntelliGPTSettingsConfigurable implements SearchableConfigurable {

    private JTextField backendUrlField;
    private JPanel panel;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "LTJ IntelliGPT";
    }

    @Override
    public @Nullable JComponent createComponent() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Backend URL:"));
        backendUrlField = new JTextField(IntelliGPTSettingsState.getInstance().backendUrl);
        panel.add(backendUrlField);

        return panel;
    }

    @Override
    public boolean isModified() {
        return !backendUrlField.getText().equals(IntelliGPTSettingsState.getInstance().backendUrl);
    }

    @Override
    public void apply() {
        IntelliGPTSettingsState.getInstance().backendUrl = backendUrlField.getText();
    }

    @Override
    public @Nullable String getId() {
        return "LTJ.IntelliGPT.Settings";
    }
}
