# Changelog

All notable changes to the EarthBuild IntelliJ Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2025-08-01

### Added
- Enhanced plugin description with comprehensive feature documentation
- Detailed change notes embedded in plugin.xml for better marketplace presentation
- Demo Earthfile for documentation and taking screenshots
- Professional marketplace-ready description with proper HTML formatting

## [1.0.1] - 2025-08-01

### Fixed
- Resolved version conflict for marketplace upload
- Fixed 7 warnings about optional plugin dependencies by adding proper config-file attributes
- Added Kotlin plugin compatibility mode support (K1 and K2)

### Added
- Configuration files for optional dependencies:
  - Java integration (`earthly-java.xml`)
  - Kotlin integration (`earthly-kotlin.xml`)
  - PHP integration (`earthly-php.xml`)
  - Ruby integration (`earthly-ruby.xml`)
  - Python integration (`earthly-python.xml`)
  - Go integration (`earthly-go.xml`)

## [1.0.0] - 2025-07-31

### Added
- Full support for IntelliJ Platform 2025.1.3
- Support for IntelliJ Platform builds 232 through 251.*
- Migrated to IntelliJ Platform Gradle Plugin 2.6.0
- Community fork of the original Earthly organization plugin

### Changed
- Updated minimum IntelliJ version to 2023.2 (build 232)
- Migrated from deprecated IntelliJ Platform Plugin 1.x to 2.x
- Updated test infrastructure for compatibility with new platform APIs
- Adjusted PSI output format to match IntelliJ 2025.1.3 changes

### Fixed
- Parser tests now compatible with new TextMate scope format
- Resolved all compilation warnings except TextMate API deprecations (non-blocking)
- Fixed StringUtils deprecation by migrating to IntelliJ's StringUtil

### Known Issues
- TextMate API deprecation warnings (functionality not affected)
- These will be addressed in a future release when the new TextMate API stabilizes

## [Pre-1.0.0]

### Original Features (from Earthly organization)
- Earthfile syntax highlighting via TextMate grammar
- Go to definition for targets and functions
- Find usages for targets and functions
- Code completion for target and function references
- Comment/uncomment support
- File type recognition (Earthfile, *.earth, *.earthly)
- Basic refactoring support (safe delete)

---

For more details on each release, see the [GitHub Releases](https://github.com/EarthBuild/earthly-intellij-plugin/releases) page.