package dev.earthly.plugin.language.syntax.highlighting;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import dev.earthly.plugin.language.syntax.lexer.EarthlyElementType;
import dev.earthly.plugin.language.syntax.psi.*;

import java.util.concurrent.ConcurrentHashMap;

public class EarthlyTokenSets {
    private static final ConcurrentHashMap<String, EarthlyElementType> cache = new ConcurrentHashMap<>();

    public final static EarthlyElementType EMPTY = getOrCreate("");
    public final static EarthlyElementType FUNCTION = getOrCreate("entity.name.function.function.earthfile");
    public final static EarthlyElementType FUNCTION_DOTS = getOrCreate("entity.name.function.function-dots.earthfile");
    public final static EarthlyElementType FUNCTION_CALL = getOrCreate("entity.name.function.call-name.earthfile");
    public final static EarthlyElementType TARGET = getOrCreate("entity.name.class.target.earthfile");
    public final static EarthlyElementType TARGET_CALL = getOrCreate("entity.name.type.target.earthfile");
    public final static EarthlyElementType BASE_CALL = getOrCreate("entity.name.type.base-name.earthfile");
    public final static EarthlyElementType COMMENT1 = getOrCreate("comment.line.earthfile");
    public final static EarthlyElementType COMMENT2 = getOrCreate("comment.line.number-sign.earthfile");
    public final static EarthlyElementType COMMENT_PUNCTUATION = getOrCreate("punctuation.definition.comment.earthfile");

    public final static EarthlyElementType EOL = getOrCreate("constant.eol.earthfile");
    public final static EarthlyElementType INDENT = getOrCreate("constant.indent.earthfile");

    public final static TokenSet COMMENTS = TokenSet.create(COMMENT1, COMMENT2, COMMENT_PUNCTUATION);
    public final static TokenSet IDENTIFIERS = TokenSet.create(FUNCTION, FUNCTION_CALL, TARGET, TARGET_CALL, BASE_CALL);

    public static EarthlyElementType getOrCreate(String scopeName) {
        return cache.computeIfAbsent(scopeName, EarthlyElementType::new);
    }

    public static class Factory {
        public static EarthlyPsiElement createElement(ASTNode node) {
            IElementType type = node.getElementType();
            if (type.equals(FUNCTION)) {
                return new EarthlyFunctionPsiElement(node);
            } else if (type.equals(FUNCTION_CALL)) {
                return new EarthlyFunctionCallPsiElement(node);
            } else if (type.equals(TARGET)) {
                return new EarthlyTargetPsiElement(node);
            } else if (type.equals(TARGET_CALL)) {
                return new EarthlyTargetCallPsiElement(node);
            } else if (type.equals(BASE_CALL)) {
                return new EarthlyTargetCallPsiElement(node);
            }
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}
