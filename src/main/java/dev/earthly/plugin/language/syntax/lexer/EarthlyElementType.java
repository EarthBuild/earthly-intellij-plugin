package dev.earthly.plugin.language.syntax.lexer;

import com.intellij.psi.tree.IElementType;
import dev.earthly.plugin.metadata.EarthlyLanguage;
import org.jetbrains.annotations.NotNull;

public class EarthlyElementType extends IElementType {
  private final String myScopeName;

  public EarthlyElementType(@NotNull String scopeName) {
    super(scopeName, EarthlyLanguage.INSTANCE);
    myScopeName = scopeName;
  }

  @NotNull
  public String getScopeName() {
    return myScopeName;
  }

  @Override
  public int hashCode() {
    return myScopeName.hashCode();
  }

  @Override
  public String toString() {
    return myScopeName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return myScopeName.equals(((EarthlyElementType) o).myScopeName);
  }
}
