package no.ltj.intelligpt.ui;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.markup.CustomHighlighterRenderer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class InlineSuggestionRenderer implements CustomHighlighterRenderer {

    private final String suggestion;

    public InlineSuggestionRenderer(@NotNull String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public void paint(@NotNull Editor editor,
                      @NotNull RangeHighlighter highlighter,
                      @NotNull Graphics g) {

        if (suggestion.isEmpty()) return;

        int offset = highlighter.getStartOffset();
        VisualPosition caretPos = editor.offsetToVisualPosition(offset);
        Point p = editor.visualPositionToXY(caretPos);

        EditorColorsScheme scheme = editor.getColorsScheme();
        g.setColor(new JBColor(new Color(255, 255, 255, 120),
                new Color(200, 200, 200, 120)));
        g.setFont(scheme.getFont(EditorFontType.PLAIN));

        FontMetrics fm = g.getFontMetrics();
        int y = p.y + editor.getLineHeight() - fm.getDescent();

        String text = suggestion.replaceAll("\\s+", " ");
        g.drawString(text, p.x, y);
    }
}
