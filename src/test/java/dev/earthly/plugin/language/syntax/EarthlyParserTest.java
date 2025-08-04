package dev.earthly.plugin.language.syntax;

import com.intellij.testFramework.ParsingTestCase;
import dev.earthly.plugin.language.syntax.parser.EarthlyParserDefinition;

public class EarthlyParserTest extends ParsingTestCase {
    
    public EarthlyParserTest() {
        super("", "earthly", new EarthlyParserDefinition());
    }

    public void testParsingTestData() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }
}
