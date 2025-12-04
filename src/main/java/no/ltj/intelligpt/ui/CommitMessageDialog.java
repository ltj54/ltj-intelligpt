package no.ltj.intelligpt.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Enkel dialog som viser/redigerer en commit-melding.
 * Brukes ikke nødvendigvis av dagens actions, men er CE-kompatibel.
 */
public class CommitMessageDialog extends DialogWrapper {

    private final JTextArea textArea;

    public CommitMessageDialog(@Nullable Project project, String initialText) {
        super(project);
        setTitle("LTJ IntelliGPT – Commit Message");
        this.textArea = new JTextArea(10, 60);
        this.textArea.setText(initialText);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public String getCommitMessage() {
        return textArea.getText();
    }
}
