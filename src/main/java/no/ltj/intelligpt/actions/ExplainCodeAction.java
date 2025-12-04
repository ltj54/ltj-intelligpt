package no.ltj.intelligpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import no.ltj.intelligpt.backend.BackendClientAsync;
import org.jetbrains.annotations.NotNull;

public class ExplainCodeAction extends AnAction {

    public ExplainCodeAction() {
        super("LTJ IntelliGPT – Explain Code");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        if (project == null) return;

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            Messages.showErrorDialog(project, "No active editor.", "LTJ IntelliGPT");
            return;
        }

        SelectionModel sel = editor.getSelectionModel();
        String text = sel.getSelectedText();
        if (text == null || text.isBlank()) {
            Messages.showWarningDialog(project, "Select some code first.", "LTJ IntelliGPT");
            return;
        }

        BackendClientAsync.sendAsync(project, "explain_code", text, new BackendClientAsync.Callback() {
            @Override
            public void onSuccess(String result) {
                Messages.showInfoMessage(project, result, "Explanation");
            }

            @Override
            public void onError(String message) {
                Messages.showErrorDialog(project, message, "LTJ IntelliGPT Error");
            }
        });
    }
}
