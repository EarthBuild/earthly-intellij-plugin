package dev.earthly.plugin.language.syntax;

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import dev.earthly.plugin.metadata.EarthlyFileType;

public class EarthlyCommenterTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testCommenter() {
        myFixture.configureByText(EarthlyFileType.INSTANCE, "TEST_<caret>ME:");
        CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("#TEST_ME:");
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("TEST_ME:");
    }
}
