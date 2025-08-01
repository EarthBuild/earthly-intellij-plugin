package dev.earthly.plugin.language.syntax.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.textmate.language.syntax.lexer.TextMateScope;

/**
 * Alternative implementation that wraps the TextMate lexer to avoid deprecated API usage.
 * This is a placeholder for when we migrate to the new TextMate API.
 * 
 * Note: The new TextMate API is not well documented yet, so we're keeping the current
 * implementation until the new API stabilizes and documentation becomes available.
 */
public class EarthlyHighlightingLexerV2 extends Lexer {
    // TODO: Implement when new TextMate API is documented
    // The new API should use TextMateService and TextMateBundle instead of TextMateSyntaxTable
    
    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        // Placeholder implementation
    }

    @Override
    public int getState() {
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return null;
    }

    @Override
    public int getTokenStart() {
        return 0;
    }

    @Override
    public int getTokenEnd() {
        return 0;
    }

    @Override
    public void advance() {
        // Placeholder implementation
    }

    @NotNull
    @Override
    public LexerPosition getCurrentPosition() {
        return new LexerPosition() {
            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public int getState() {
                return 0;
            }
        };
    }

    @Override
    public void restore(@NotNull LexerPosition position) {
        // Placeholder implementation
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return "";
    }

    @Override
    public int getBufferEnd() {
        return 0;
    }
}