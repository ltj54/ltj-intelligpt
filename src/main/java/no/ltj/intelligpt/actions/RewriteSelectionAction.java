package no.ltj.intelligpt.actions;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import no.ltj.intelligpt.gpt.OpenAiClient;
import no.ltj.intelligpt.settings.LtjIntelliGptSettings;
import org.jetbrains.annotations.NotNull;

public class RewriteSelectionAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (project == null || editor == null) {
            Messages.showErrorDialog(
                    "Ingen aktiv editor er åpen.",
                    "LTJ IntelliGPT – Selection Rewrite"
            );
            return;
        }

        if (!editor.getSelectionModel().hasSelection()) {
            Messages.showInfoMessage(
                    "Denne funksjonen krever at du markerer tekst først.",
                    "LTJ IntelliGPT"
            );
            return;
        }

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || selectedText.isBlank()) {
            Messages.showInfoMessage(
                    "Den markerte teksten er tom.",
                    "LTJ IntelliGPT"
            );
            return;
        }

        LtjIntelliGptSettings settings = LtjIntelliGptSettings.getInstance();

        String apiKey = settings.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("OPENAI_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            Messages.showErrorDialog(
                    project,
                    "Ingen OpenAI API key er satt.\n" +
                            "Gå til Settings → LTJ IntelliGPT eller sett OPENAI_API_KEY.",
                    "LTJ IntelliGPT"
            );
            return;
        }

        String defaultInstruction =
                "Forbedre koden, rydd opp og legg inn TODO hvis det trengs.";

        String instruction = Messages.showInputDialog(
                project,
                "Hva vil du at IntelliGPT skal gjøre med den markerte teksten?",
                "LTJ IntelliGPT – Rewrite Selection",
                Messages.getQuestionIcon(),
                defaultInstruction,
                null
        );

        if (instruction == null || instruction.isBlank()) {
            return;
        }

        String finalApiKey = apiKey;
        String model = settings.getModel();
        String original = selectedText;

        ProgressManager.getInstance().run(
                new Task.Backgroundable(project, "LTJ IntelliGPT – Rewrite Selection") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setIndeterminate(true);
                        indicator.setText("Sender markert tekst til OpenAI …");

                        try {

                            String rewritten = OpenAiClient.rewriteFile(
                                    finalApiKey,
                                    model,
                                    instruction,
                                    original
                            );

                            if (rewritten == null || rewritten.isBlank()) {
                                showError("GPT returnerte ingen tekst.");
                                return;
                            }

                            ApplicationManager.getApplication().invokeLater(() -> {
                                showDiffAndMaybeApply(project, editor, original, rewritten);
                            });

                        } catch (Exception ex) {
                            showError("Feil ved GPT-kall:\n" + ex.getMessage());
                        }
                    }

                    private void showError(String msg) {
                        ApplicationManager.getApplication().invokeLater(() ->
                                Messages.showErrorDialog(project, msg, "LTJ IntelliGPT"));
                    }
                }
        );
    }

    private void showDiffAndMaybeApply(Project project,
                                       Editor editor,
                                       String original,
                                       String rewritten) {

        DiffContentFactory factory = DiffContentFactory.getInstance();

        DiffContent c1 = factory.create(original);
        DiffContent c2 = factory.create(rewritten);

        SimpleDiffRequest req = new SimpleDiffRequest(
                "LTJ IntelliGPT – Rewrite Selection Preview",
                c1,
                c2,
                "Original",
                "GPT Forslag"
        );

        DiffManager.getInstance().showDiff(project, req);

        int result = Messages.showYesNoCancelDialog(
                "Vil du erstatte markeringen med GPT-forslaget?",
                "LTJ IntelliGPT",
                "Apply",
                "Cancel",
                "Cancel",
                Messages.getQuestionIcon()
        );

        if (result == Messages.YES) {
            Document doc = editor.getDocument();
            int start = editor.getSelectionModel().getSelectionStart();
            int end = editor.getSelectionModel().getSelectionEnd();

            ApplicationManager.getApplication().runWriteAction(() ->
                    doc.replaceString(start, end, rewritten)
            );
        }
    }
}
