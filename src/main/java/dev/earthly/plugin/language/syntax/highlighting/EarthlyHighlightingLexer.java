package dev.earthly.plugin.language.syntax.highlighting;

import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.Interner;
import org.jetbrains.plugins.textmate.language.TextMateLanguageDescriptor;
import org.jetbrains.plugins.textmate.language.syntax.TextMateSyntaxTable;
import org.jetbrains.plugins.textmate.language.syntax.lexer.TextMateElementType;
import org.jetbrains.plugins.textmate.language.syntax.lexer.TextMateHighlightingLexer;
import org.jetbrains.plugins.textmate.language.syntax.lexer.TextMateScope;
import org.jetbrains.plugins.textmate.plist.CompositePlistReader;
import org.jetbrains.plugins.textmate.plist.Plist;
import org.jetbrains.plugins.textmate.plist.PlistReader;

import java.io.InputStream;
import java.io.BufferedInputStream;

/**
 * TextMate-based syntax highlighting lexer for Earthly files.
 * 
 * NOTE: This implementation uses deprecated TextMate APIs (TextMateSyntaxTable, etc.)
 * that are scheduled for removal in future IntelliJ releases. These APIs still work
 * correctly in IntelliJ 2025.1.x but will need to be migrated when:
 * 1. The new TextMate API is properly documented
 * 2. The deprecated APIs are actually removed
 * 
 * The warnings about these deprecated APIs are expected and don't affect functionality.
 * See EarthlyHighlightingLexerV2 for a placeholder for the future migration.
 */
public class EarthlyHighlightingLexer extends TextMateHighlightingLexer {

  private final static String EARTHFILE_DESC = "earthfile.tmLanguage.json";

  public EarthlyHighlightingLexer() {
    super(getTextMateLanguageDescriptor(), 20000);
  }

  public IElementType getTokenType() {
    TextMateElementType tokenType = (TextMateElementType) super.getTokenType();
    if (tokenType == null) {
      return null;
    }
    TextMateScope scope = tokenType.getScope();
    return EarthlyTokenSets.mapToType(scope);
  }

  private static TextMateLanguageDescriptor getTextMateLanguageDescriptor() {
    try {
      TextMateSyntaxTable syntaxTable = new TextMateSyntaxTable();
      Interner<CharSequence> interner = Interner.createWeakInterner();
      PlistReader plistReader = new CompositePlistReader();
      Plist plist = plistReader.read(getLanguageGrammar());
      CharSequence scopeName = syntaxTable.loadSyntax(plist, interner);
      return new TextMateLanguageDescriptor(scopeName, syntaxTable.getSyntax(scopeName));
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private static InputStream getLanguageGrammar() {
    return new BufferedInputStream(EarthlyHighlightingLexer.class.getClassLoader().getResourceAsStream(EARTHFILE_DESC));
  }
}
