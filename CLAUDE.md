# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Building the Plugin

#### Using Gradle (Recommended)
```bash
# Build the plugin
./gradlew buildPlugin

# Clean build
./gradlew clean buildPlugin

# Build without running tests
./gradlew buildPlugin -x test

# Run plugin verifier to check compatibility
./gradlew verifyPlugin
```

#### Using Earthly (Alternative)
```bash
# Generate the plugin package
earthly +dist --version=<version>

# Generate gradle wrapper if missing
earthly +generate-gradle-wrapper
```

### Testing
```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "dev.earthly.plugin.language.syntax.EarthlyParserTest"

# Run a specific test method
./gradlew test --tests "dev.earthly.plugin.language.syntax.EarthlyParserTest.testParsingTestData"

# Run tests with debugging output
./gradlew test --info

# Open IntelliJ with plugin installed for manual testing
./gradlew runIde

# Using Earthly
earthly +test
earthly +ide
```

### Publishing
```bash
# Sign the plugin (requires CERTIFICATE_CHAIN, PRIVATE_KEY, PRIVATE_KEY_PASSWORD env vars)
./gradlew signPlugin

# Publish to JetBrains Marketplace (requires PUBLISH_TOKEN env var)
./gradlew publishPlugin

# Dry run (validate without publishing)
./gradlew publishPlugin --dry-run

# Using Earthly
earthly +sign --version=<version>
earthly --push +publish --version=<version>
```

**Note**: See SECRETS_SETUP.md for instructions on generating signing certificates and publishing tokens.

### Version Management
```bash
# Update version in build.gradle.kts before building
# Current version placeholder: 0.0.0

# Build with specific version
./gradlew buildPlugin -Pversion=1.0.0

# For Earthly builds, pass version as argument
earthly +dist --version=1.0.0
```

## Architecture Overview

The plugin follows the standard IntelliJ Platform Plugin architecture with a focus on language support features.

### Core Language Infrastructure

The plugin defines Earthly as a custom language in IntelliJ through three foundational components:

- **EarthlyLanguage** (`dev.earthly.plugin.metadata.EarthlyLanguage`): Singleton that registers "Earthly" as a language in the IDE
- **EarthlyFileType** (`dev.earthly.plugin.metadata.EarthlyFileType`): Associates file patterns (Earthfile, *.earth, *.earthly) with the Earthly language
- **EarthlyParserDefinition** (`dev.earthly.plugin.language.syntax.parser.EarthlyParserDefinition`): Central configuration that binds the lexer, parser, and PSI tree structure

### Syntax Processing Pipeline

The plugin uses a dual-layer approach for syntax processing:

1. **TextMate Grammar Layer**: Initial syntax highlighting via `earthfile.tmLanguage.json` provides immediate visual feedback
2. **Custom Parser Layer**: Full AST construction for advanced IDE features

Key components:
- **EarthlyHighlightingLexer**: Tokenizes Earthfile content, recognizing Earthly-specific constructs
- **EarthlyParser**: Builds the AST from lexer tokens, understanding target definitions, function declarations, and references
- **EarthlyTokenSets**: Defines token categories (comments, keywords, operators) for consistent handling

### PSI (Program Structure Interface) Model

The plugin models Earthly constructs as PSI elements, enabling IDE features:

- **EarthlyTargetPsiElement**: Represents target definitions (e.g., `build:`)
- **EarthlyFunctionPsiElement**: Represents function declarations  
- **EarthlyTargetCallPsiElement**: Represents target invocations (e.g., `+build`)
- **EarthlyFunctionCallPsiElement**: Represents function calls
- **EarthlyReference**: Provides the linking mechanism between definitions and usages

These elements form a tree structure that the IDE uses for navigation, refactoring, and analysis.

### IDE Feature Implementation

Each IDE feature is implemented through specific extension points:

- **Syntax Highlighting**: `EarthlySyntaxHighlighterFactory` + `EarthlyEditorHighlighterProvider` combine TextMate and semantic highlighting
- **Code Completion**: `EarthlyCompletionContributor` suggests targets and functions based on current context
- **Reference Resolution**: `EarthlyReferenceContributor` enables Ctrl+Click navigation and find usages
- **Commenting**: `EarthlyCommenter` defines `#` as the comment prefix for Cmd+/ functionality
- **Find Usages**: `EarthlyFindUsagesProvider` formats search results with meaningful descriptions
- **Refactoring Support**: `EarthlyRefactoringSupportProvider` currently supports safe delete operations
- **Name Validation**: `EarthlyNamesValidator` enforces naming rules for targets and functions

### Testing Infrastructure

- Parser tests use `ParsingTestCase` to verify AST construction
- Test data in `src/test/testData/` includes `.earthly` input files and `.txt` expected output
- Manual testing via `runIde` task launches a sandboxed IDE instance

## Important Configuration

### Platform Compatibility
- IntelliJ Platform version: 2025.1.3
- Supported build range: 232 to 251.*
- JDK version: 17 (required for building)
- Gradle version: 8.5
- Kotlin version: 2.0.0 (required for IntelliJ 2025.1+)
- IntelliJ Platform Gradle Plugin: 2.6.0

### Dependencies
- **Required**: TextMate bundles plugin (`org.jetbrains.plugins.textmate`)
- **Runtime**: Apache Commons Collections 4.4
- **Test**: JUnit 4.13.2
- **Optional IDE integrations**: Java, Kotlin, PHP, Ruby, Python, Go

### Environment Variables
- `CERTIFICATE_CHAIN`: Plugin signing certificate chain
- `PRIVATE_KEY`: Private key for signing
- `PRIVATE_KEY_PASSWORD`: Password for private key
- `PUBLISH_TOKEN`: JetBrains Marketplace API token

### CI/CD Integration
- GitHub Actions workflows in `.github/workflows/`:
  - `build.yml`: Automated build and test on push/PR
  - `publish.yml`: Automated publishing on version tags (v*)
- Earthly targets provide containerized builds as an alternative

## Development Workflow

### Making Changes to Language Features

1. **Modifying Grammar**: Edit `earthfile.tmLanguage.json` for syntax highlighting changes
2. **Parser Changes**: Update `EarthlyParser` and related PSI elements
3. **Adding IDE Features**: Implement new contributors in the syntax package
4. **Testing**: Add test cases to `ParsingTestData.earthly` and expected output

### Debugging Tips

- Use `./gradlew runIde` to test changes in a live IDE instance
- Enable internal mode in the sandbox IDE for additional debugging tools
- Parser test failures show detailed AST differences
- Check `idea.log` in the sandbox instance for runtime errors

### Migration Context

This is a fork of the original Earthly organization plugin. Key changes:
- Removed dependency on Earthly organization infrastructure
- Updated to support latest IntelliJ 2025.1.3
- Migrated to IntelliJ Platform Gradle Plugin 2.x
- Repository now at github.com/EarthBuild/earthly-intellij-plugin

### Known Technical Debt

1. **TextMate API Deprecation**: The plugin uses deprecated TextMate APIs (TextMateSyntaxTable, etc.) that work in IntelliJ 2025.1.x but are scheduled for removal. Migration is blocked on proper documentation of the new API.

2. **Optional Module Warnings**: Plugin verification shows warnings about unresolved optional modules (Python, Ruby). These are expected and don't affect functionality.

3. **Future Migration Path**: See EarthlyHighlightingLexerV2.java for a placeholder for the future TextMate API migration.