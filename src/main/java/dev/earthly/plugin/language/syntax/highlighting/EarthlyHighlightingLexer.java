package dev.earthly.plugin.language.syntax.highlighting;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import dev.earthly.plugin.language.syntax.lexer.EarthlyElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EarthlyHighlightingLexer extends LexerBase {

    private static final int STATE_SOL = 0;
    private static final int STATE_MID = 1;
    private static final int STATE_DQUOTE = 2;
    private static final int STATE_SQUOTE = 3;

    private static final Pattern TARGET_DECL = Pattern.compile("([a-z]([a-zA-Z0-9.]|-)*)(:)");
    private static final Pattern FUNCTION_DECL = Pattern.compile("([A-Z][a-zA-Z0-9._]*)(:)?");
    private static final Pattern COMMENT_LINE = Pattern.compile("#[^\n\r]*");
    private static final Pattern INDENT = Pattern.compile("[ \t]+");
    private static final Pattern EOL = Pattern.compile("\r?\n");
    private static final Pattern ESCAPE = Pattern.compile("\\\\.");
    private static final Pattern LINE_CONTINUATION = Pattern.compile("\\\\$", Pattern.MULTILINE);

    private static final Pattern KEYWORD = Pattern.compile(
            "(FROM DOCKERFILE|SAVE ARTIFACT|SAVE IMAGE|GIT CLONE|DOCKER LOAD|DOCKER PULL|" +
            "WITH DOCKER|ELSE IF|STOP SIGNAL|" +
            "FROM|COPY|RUN|LABEL|EXPOSE|VOLUME|USER|ENV|ARG|BUILD|WORKDIR|ENTRYPOINT|CMD|" +
            "HEALTHCHECK|END|IF|ELSE|DO|COMMAND|FUNCTION|IMPORT|LOCALLY|FOR|VERSION|WAIT|TRY|" +
            "FINALLY|CACHE|HOST|PIPELINE|TRIGGER|PROJECT|SET|LET|ADD|ONBUILD|SHELL)" +
            "(?=\\s|$)");

    // Full reference: optional-path + name
    private static final Pattern FUNC_REF = Pattern.compile("([a-zA-Z0-9._\\-/:]*)\\+([A-Z][a-zA-Z0-9._]*)");
    private static final Pattern TARGET_REF = Pattern.compile("([a-zA-Z0-9._\\-/:]*)\\+([a-z][a-zA-Z0-9.\\-]*)(/\\S+)*");

    private static final Pattern SHELL_OP = Pattern.compile("(&&|>>|<<|[|;>])");
    private static final Pattern ASSIGN_OP = Pattern.compile("=");
    private static final Pattern FLAG = Pattern.compile("\\B-+[a-zA-Z0-9\\-_]+");
    private static final Pattern VARIABLE_BRACED = Pattern.compile("\\$\\{[a-zA-Z0-9.\\-_#]+}");
    private static final Pattern VARIABLE_PLAIN = Pattern.compile("\\$[a-zA-Z0-9_]+");
    private static final Pattern WORD = Pattern.compile("[^\\s\"'$#\\\\=|;&>+\\-]+");

    private CharSequence myBuffer;
    private int myEndOffset;
    private int myState;

    private int myTokenStart;
    private int myTokenEnd;
    private IElementType myTokenType;

    private final Deque<PendingToken> pendingTokens = new ArrayDeque<>();

    private record PendingToken(int start, int end, EarthlyElementType type) {}

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myBuffer = buffer;
        myEndOffset = endOffset;
        myState = initialState;
        myTokenStart = startOffset;
        myTokenEnd = startOffset;
        myTokenType = null;
        pendingTokens.clear();
        advance();
    }

    @Override
    public int getState() {
        return myState;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return myTokenType;
    }

    @Override
    public int getTokenStart() {
        return myTokenStart;
    }

    @Override
    public int getTokenEnd() {
        return myTokenEnd;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return myBuffer;
    }

    @Override
    public int getBufferEnd() {
        return myEndOffset;
    }

    @Override
    public void advance() {
        if (!pendingTokens.isEmpty()) {
            PendingToken t = pendingTokens.poll();
            myTokenStart = t.start;
            myTokenEnd = t.end;
            myTokenType = t.type;
            return;
        }

        myTokenStart = myTokenEnd;
        if (myTokenStart >= myEndOffset) {
            myTokenType = null;
            return;
        }

        switch (myState) {
            case STATE_DQUOTE -> advanceInString('"');
            case STATE_SQUOTE -> advanceInString('\'');
            default -> advanceNormal();
        }
    }

    private void advanceNormal() {
        CharSequence remaining = myBuffer.subSequence(myTokenStart, myEndOffset);
        Matcher m;

        // EOL — always check first
        m = EOL.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.EOL);
            myState = STATE_SOL;
            return;
        }

        if (myState == STATE_SOL) {
            // Leading indent at start of line
            m = INDENT.matcher(remaining);
            if (m.lookingAt()) {
                emit(m, EarthlyTokenSets.INDENT);
                myState = STATE_MID;
                return;
            }

            // Comment at start of line
            if (remaining.charAt(0) == '#') {
                m = COMMENT_LINE.matcher(remaining);
                if (m.lookingAt()) {
                    emit(m, EarthlyTokenSets.COMMENT2);
                    return;
                }
            }

            // Target declaration: lowercase-name:
            m = TARGET_DECL.matcher(remaining);
            if (m.lookingAt()) {
                // Emit just the target name, colon becomes next token
                emitRange(m.end(1), EarthlyTokenSets.TARGET);
                myState = STATE_MID;
                return;
            }

            // Function/command declaration or keyword at start of line
            m = FUNCTION_DECL.matcher(remaining);
            if (m.lookingAt()) {
                if (isKnownKeyword(m.group(1))) {
                    matchMidLine(remaining);
                    return;
                }
                emitRange(m.end(1), EarthlyTokenSets.FUNCTION);
                myState = STATE_MID;
                return;
            }
        }

        matchMidLine(remaining);
    }

    private void matchMidLine(CharSequence remaining) {
        Matcher m;
        myState = STATE_MID;

        // Non-newline whitespace
        m = INDENT.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.EMPTY);
            return;
        }

        // Comment
        if (remaining.charAt(0) == '#') {
            m = COMMENT_LINE.matcher(remaining);
            if (m.lookingAt()) {
                emit(m, EarthlyTokenSets.COMMENT2);
                return;
            }
        }

        // String start
        if (remaining.charAt(0) == '"') {
            emitRange(1, EarthlyTokenSets.getOrCreate("string.quoted.double.earthfile"));
            myState = STATE_DQUOTE;
            return;
        }
        if (remaining.charAt(0) == '\'') {
            emitRange(1, EarthlyTokenSets.getOrCreate("string.quoted.single.earthfile"));
            myState = STATE_SQUOTE;
            return;
        }

        // Variable ${...}
        m = VARIABLE_BRACED.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("variable.other.earthfile"));
            return;
        }

        // Variable $name
        m = VARIABLE_PLAIN.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("variable.other.earthfile"));
            return;
        }

        // Line continuation
        m = LINE_CONTINUATION.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("constant.character.escape.earthfile"));
            return;
        }

        // Escape sequence
        m = ESCAPE.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("constant.character.escape.earthfile"));
            return;
        }

        // Function reference: [path]+FUNCTION_NAME — split into up to 3 tokens
        m = FUNC_REF.matcher(remaining);
        if (m.lookingAt()) {
            emitReference(m, true);
            return;
        }

        // Target reference: [path]+target-name[/artifact] — split into up to 3 tokens
        m = TARGET_REF.matcher(remaining);
        if (m.lookingAt()) {
            emitReference(m, false);
            return;
        }

        // Plus sign not followed by a name
        if (remaining.charAt(0) == '+') {
            emitRange(1, EarthlyTokenSets.EMPTY);
            return;
        }

        // Keywords (multi-word first, then single-word)
        m = KEYWORD.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("keyword.other.special-method.earthfile"));
            return;
        }

        // Shell operators
        m = SHELL_OP.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("keyword.operator.shell.earthfile"));
            return;
        }

        // Assignment
        if (remaining.charAt(0) == '=') {
            emitRange(1, EarthlyTokenSets.getOrCreate("keyword.operator.assignment.earthfile"));
            return;
        }

        // Flags (--flag)
        m = FLAG.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("keyword.operator.flag.earthfile"));
            return;
        }

        // Colon (declaration separator)
        if (remaining.charAt(0) == ':') {
            emitRange(1, EarthlyTokenSets.FUNCTION_DOTS);
            return;
        }

        // Dash that didn't match as a flag
        if (remaining.charAt(0) == '-') {
            emitRange(1, EarthlyTokenSets.EMPTY);
            return;
        }

        // Generic word
        m = WORD.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.EMPTY);
            return;
        }

        // Single character fallback
        emitRange(1, EarthlyTokenSets.EMPTY);
    }

    private void emitReference(Matcher m, boolean isFunction) {
        int absStart = myTokenStart;
        int prefixLen = m.start(2) - (m.group(1).isEmpty() ? 0 : 0);
        int plusStart = absStart + m.start(2) - 1; // position of '+'
        int nameStart = absStart + m.start(2);
        int nameEnd = absStart + m.end(2);

        String prefixScope = isFunction
                ? "entity.name.function.call-target.earthfile"
                : "entity.name.type.target-target.earthfile";
        String plusScope = isFunction
                ? "entity.name.function.call-plus.earthfile"
                : "entity.name.type.base-plus.earthfile";
        EarthlyElementType nameType = isFunction
                ? EarthlyTokenSets.FUNCTION_CALL
                : EarthlyTokenSets.TARGET_CALL;

        boolean hasPrefix = plusStart > absStart;

        if (hasPrefix) {
            // Emit prefix as current token, queue plus and name
            myTokenEnd = plusStart;
            myTokenType = EarthlyTokenSets.getOrCreate(prefixScope);
            pendingTokens.add(new PendingToken(plusStart, nameStart, EarthlyTokenSets.getOrCreate(plusScope)));
            pendingTokens.add(new PendingToken(nameStart, nameEnd, nameType));
        } else {
            // No prefix — emit plus as current token, queue name
            myTokenEnd = nameStart;
            myTokenType = EarthlyTokenSets.getOrCreate(plusScope);
            pendingTokens.add(new PendingToken(nameStart, nameEnd, nameType));
        }

        // If there's artifact path after the name (e.g., +target/artifact)
        int fullEnd = absStart + m.end();
        if (fullEnd > nameEnd) {
            pendingTokens.add(new PendingToken(nameEnd, fullEnd, EarthlyTokenSets.EMPTY));
        }
    }

    private void advanceInString(char quote) {
        CharSequence remaining = myBuffer.subSequence(myTokenStart, myEndOffset);

        // End of string
        if (remaining.charAt(0) == quote) {
            EarthlyElementType type = quote == '"'
                    ? EarthlyTokenSets.getOrCreate("string.quoted.double.earthfile")
                    : EarthlyTokenSets.getOrCreate("string.quoted.single.earthfile");
            emitRange(1, type);
            myState = STATE_MID;
            return;
        }

        // Escape in string
        Matcher m = ESCAPE.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("constant.character.escaped.earthfile"));
            return;
        }

        // Variable in string
        m = VARIABLE_BRACED.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("variable.other.earthfile"));
            return;
        }
        m = VARIABLE_PLAIN.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.getOrCreate("variable.other.earthfile"));
            return;
        }

        // Unterminated string at EOL
        m = EOL.matcher(remaining);
        if (m.lookingAt()) {
            emit(m, EarthlyTokenSets.EOL);
            myState = STATE_SOL;
            return;
        }

        // String content
        EarthlyElementType stringType = quote == '"'
                ? EarthlyTokenSets.getOrCreate("string.quoted.double.earthfile")
                : EarthlyTokenSets.getOrCreate("string.quoted.single.earthfile");

        int i = 0;
        while (i < remaining.length()) {
            char c = remaining.charAt(i);
            if (c == quote || c == '\\' || c == '$' || c == '\n' || c == '\r') break;
            i++;
        }
        if (i == 0) i = 1;
        emitRange(i, stringType);
    }

    private void emit(Matcher m, EarthlyElementType type) {
        myTokenEnd = myTokenStart + m.end();
        myTokenType = type;
        myState = STATE_MID;
    }

    private void emitRange(int length, EarthlyElementType type) {
        myTokenEnd = myTokenStart + length;
        myTokenType = type;
    }

    private static boolean isKnownKeyword(String name) {
        return switch (name) {
            case "FROM", "COPY", "RUN", "LABEL", "EXPOSE", "VOLUME", "USER", "ENV", "ARG",
                 "BUILD", "WORKDIR", "ENTRYPOINT", "CMD", "HEALTHCHECK", "END", "IF", "ELSE",
                 "DO", "COMMAND", "FUNCTION", "IMPORT", "LOCALLY", "FOR", "VERSION", "WAIT",
                 "TRY", "FINALLY", "CACHE", "HOST", "PIPELINE", "TRIGGER", "PROJECT", "SET",
                 "LET", "ADD", "ONBUILD", "SHELL" -> true;
            default -> false;
        };
    }
}
