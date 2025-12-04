package no.ltj.intelligpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import no.ltj.intelligpt.backend.BackendClientAsync;
import org.jetbrains.annotations.NotNull;

public class CommitMessageAction extends AnAction {

    public CommitMessageAction() {
        super("LTJ IntelliGPT – Commit Message");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        if (project == null) return;

        String input = Messages.showTextAreaDialog(
                null,
                "",
                "Paste diff / description for commit message",
                null
        );

        if (input == null || input.isBlank()) {
            return;
        }

        BackendClientAsync.sendAsync(project, "commit_message", input, new BackendClientAsync.Callback() {
            @Override
            public void onSuccess(String result) {
                Messages.showTextAreaDialog(
                        null,
                        result,
                        "Generated Commit Message",
                        null
                );
            }

            @Override
            public void onError(String message) {
                Messages.showErrorDialog(project, message, "LTJ IntelliGPT Error");
            }
        });
    }
}
