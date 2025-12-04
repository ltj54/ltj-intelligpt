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
import com.intellij.openapi.vfs.VirtualFile;
import no.ltj.intelligpt.gpt.OpenAiClient;
import no.ltj.intelligpt.settings.LtjIntelliGptSettings;
import org.jetbrains.annotations.NotNull;

public class ModifyCurrentFileAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || editor == null || file == null) {
            Messages.showErrorDialog(
                    "Fant ingen aktiv fil i editoren.",
                    "LTJ IntelliGPT"
            );
            return;
        }

        Document document = editor.getDocument();
        String originalText = document.getText();

        LtjIntelliGptSettings settings = LtjIntelliGptSettings.getInstance();

        String apiKey = settings.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("OPENAI_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            Messages.showErrorDialog(
                    project,
                    "Ingen OpenAI API key er satt.\n" +
                            "Gå til Settings → LTJ IntelliGPT og legg den inn,\n" +
                            "eller sett env-variabelen OPENAI_API_KEY.",
                    "LTJ IntelliGPT"
            );
            return;
        }

        String defaultInstruction =
                "Forbedre koden, rydd opp, og legg inn tydelige TODO-kommentarer der det trengs.";

        String instruction = Messages.showInputDialog(
                project,
                "Hva vil du at IntelliGPT skal gjøre med denne filen?",
                "LTJ IntelliGPT – Instruksjon",
                Messages.getQuestionIcon(),
                defaultInstruction,
                null
        );

        if (instruction == null || instruction.isBlank()) {
            return;
        }

        String textToSend = originalText;

        if (settings.isSendEditorSelectionOnly()
                && editor.getSelectionModel().hasSelection()) {

            String selection = editor.getSelectionModel().getSelectedText();
            if (selection != null && !selection.isBlank()) {
                textToSend = selection;
            }
        }

        String finalApiKey = apiKey;
        String finalTextToSend = textToSend;
        String model = settings.getModel();

        ProgressManager.getInstance().run(
                new Task.Backgroundable(project, "LTJ IntelliGPT") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setIndeterminate(true);
                        indicator.setText("Sender fil til OpenAI …");

                        try {
                            String newContent = OpenAiClient.rewriteFile(
                                    finalApiKey,
                                    model,
                                    instruction,
                                    finalTextToSend
                            );

                            if (newContent == null || newContent.isBlank()) {
                                showError("Tomt svar fra OpenAI.");
                                return;
                            }

                            String updatedForDiff;

                            if (finalTextToSend.equals(originalText)) {
                                updatedForDiff = newContent;
                            } else {
                                updatedForDiff =
                                        originalText.replace(finalTextToSend, newContent);
                            }

                            ApplicationManager.getApplication().invokeLater(() ->
                                    showDiffAndMaybeApply(
                                            project,
                                            file,
                                            originalText,
                                            updatedForDiff,
                                            document)
                            );

                        } catch (Exception ex) {
                            showError("Feil ved kall til OpenAI:\n" + ex.getMessage());
                        }
                    }

                    private void showError(String message) {
                        ApplicationManager.getApplication().invokeLater(() ->
                                Messages.showErrorDialog(project, message, "LTJ IntelliGPT"));
                    }
                }
        );
    }

    /** Viser diff og spør om du vil bruke endringene. */
    private void showDiffAndMaybeApply(Project project,
                                       VirtualFile file,
                                       String originalText,
                                       String newText,
                                       Document document) {

        DiffContentFactory factory = DiffContentFactory.getInstance();

        // IntelliJ 2024/2025 API: create(String) — IKKE create(Project, String)
        DiffContent originalContent = factory.create(originalText);
        DiffContent newContentObj = factory.create(newText);

        SimpleDiffRequest request = new SimpleDiffRequest(
                "LTJ IntelliGPT – Preview changes",
                originalContent,
                newContentObj,
                "Original",
                "Forslag fra GPT"
        );

        DiffManager.getInstance().showDiff(project, request);

        int result = Messages.showYesNoCancelDialog(
                "Vil du erstatte innholdet i filen med forslaget fra GPT?\nDu kan angre med Ctrl+Z.",
                "LTJ IntelliGPT",
                "Apply",
                "Cancel",
                null,
                Messages.getQuestionIcon()
        );

        if (result == Messages.YES) {
            ApplicationManager.getApplication().runWriteAction(() ->
                    document.setText(newText)
            );
        }
    }
}
