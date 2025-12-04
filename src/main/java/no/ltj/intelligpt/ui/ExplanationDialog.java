package no.ltj.intelligpt.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ExplanationDialog extends DialogWrapper {

    private final String explanation;

    public ExplanationDialog(@Nullable Project project, String explanation) {
        super(project, true);
        this.explanation = explanation;
        setTitle("LTJ IntelliGPT – Code Explanation");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JTextArea area = new JTextArea(explanation);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(700, 500));

        return scroll;
    }
}
