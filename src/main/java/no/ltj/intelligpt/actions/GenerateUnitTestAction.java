package no.ltj.intelligpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.application.ApplicationManager;
import no.ltj.intelligpt.backend.BackendClientAsync;
import org.jetbrains.annotations.NotNull;

public class GenerateUnitTestAction extends AnAction {

    public GenerateUnitTestAction() {
        super("LTJ IntelliGPT – Generate Unit Test");
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

        String selected = editor.getSelectionModel().getSelectedText();
        if (selected == null || selected.isBlank()) {
            Messages.showWarningDialog(project, "Select some code first.", "LTJ IntelliGPT");
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(
                project,
                "Generating test with LTJ IntelliGPT...",
                false
        ) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {

                indicator.setIndeterminate(true);

                BackendClientAsync.sendAsync(
                        project,
                        "generate_test",
                        selected,
                        new BackendClientAsync.Callback() {
                            @Override
                            public void onSuccess(String result) {
                                ApplicationManager.getApplication().invokeLater(() ->
                                        Messages.showInfoMessage(project, result, "Generated Unit Test")
                                );
                            }

                            @Override
                            public void onError(String message) {
                                ApplicationManager.getApplication().invokeLater(() ->
                                        Messages.showErrorDialog(project, message, "LTJ IntelliGPT Error")
                                );
                            }
                        }
                );
            }
        });
    }
}
