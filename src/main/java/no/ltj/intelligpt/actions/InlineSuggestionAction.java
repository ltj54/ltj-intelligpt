package no.ltj.intelligpt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import no.ltj.intelligpt.backend.BackendClientAsync;
import no.ltj.intelligpt.ui.InlineSuggestionRenderer;
import org.jetbrains.annotations.NotNull;

public class InlineSuggestionAction extends AnAction {

    public InlineSuggestionAction() {
        super("LTJ IntelliGPT – Inline Suggestion");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        if (project == null) return;

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            Messages.showErrorDialog(project, "No editor available.", "LTJ IntelliGPT");
            return;
        }

        SelectionModel sel = editor.getSelectionModel();
        String code = sel.getSelectedText();
        if (code == null || code.isBlank()) {
            Messages.showWarningDialog(project, "Select some code to improve.", "LTJ IntelliGPT");
            return;
        }

        BackendClientAsync.sendAsync(project, "inline_suggestion", code, new BackendClientAsync.Callback() {
            @Override
            public void onSuccess(String suggestion) {

                if (suggestion == null || suggestion.isBlank()) {
                    Messages.showInfoMessage(project, "No suggestion returned.", "LTJ IntelliGPT");
                    return;
                }

                int offset = sel.getSelectionEnd();
                MarkupModel mm = editor.getMarkupModel();

                RangeHighlighter hi = mm.addRangeHighlighter(
                        offset,
                        offset,
                        HighlighterLayer.ADDITIONAL_SYNTAX,
                        null,
                        HighlighterTargetArea.EXACT_RANGE
                );
                hi.setCustomRenderer(new InlineSuggestionRenderer(suggestion));

                int choice = Messages.showYesNoDialog(
                        project,
                        "Insert this suggestion?\n\n" + suggestion,
                        "LTJ IntelliGPT Inline Suggestion",
                        "Insert",
                        "Cancel",
                        null
                );

                if (choice == Messages.YES) {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        editor.getDocument().replaceString(
                                sel.getSelectionStart(),
                                sel.getSelectionEnd(),
                                suggestion
                        );
                    });
                }

                mm.removeHighlighter(hi);
            }

            @Override
            public void onError(String message) {
                Messages.showErrorDialog(project, message, "LTJ IntelliGPT Error");
            }
        });
    }
}
