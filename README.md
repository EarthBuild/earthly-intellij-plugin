# earthly-intellij-plugin

IntelliJ plugin for Earthly language support. 

Check the currently released versions [on the JetBrains Marketplace](https://plugins.jetbrains.com/plugin/28062-earthbuild/versions).

## Features
- [x] Syntax highlighting for Earthfiles
- [x] Code completion
- [x] Commenter
- [x] Go to definition/usage
- [x] Find usages
- [ ] Formatter
- [ ] ... Let us know!

## Known Issues

### TextMate API Warnings
The plugin uses TextMate APIs that are marked as deprecated in IntelliJ 2025.1.x. These warnings are expected and don't affect functionality. The APIs will be migrated when:
- The new TextMate API is properly documented
- The deprecated APIs are actually removed from the platform

### Optional Module Warnings
When running plugin verification, you may see warnings about unresolved optional modules (Python, Ruby, etc.). These are expected for optional dependencies and don't affect the plugin's core functionality.


![Darcula theme](documentation/darcula.png)
![Light theme](documentation/light.png)

## Building
The following command generates a `earthly-intellij-plugin-<version>.zip` package in the current directory:
```
earthly +dist [--version=<version>]
```

For local development, this will generate a gradle wrapper (`gradle/`) corresponding to the gradle version used in the build itself:
```
earthly +generate-gradle-wrapper
```

## Signing
The following command generates a signed plugin package:
```
earthly +sign [--version=<version>]
```

## Publishing
The following command builds, signs and publishes the plugin to the [IntelliJ Marketplace](https://plugins.jetbrains.com/plugin/20392-earthly):
```
earthly --push +publish --version=<version>
```

Publishing is automatically triggered when a new git tag is pushed in the form of `vX.Y.Z`.

## Testing
```
earthly +ide
```

## Contributing

### Code of Conduct

Please refer to the [CNCF Community Code of Conduct v1.0](https://github.com/cncf/foundation/blob/main/code-of-conduct.md)

### CLA

#### Individual

All contributions must indicate agreement to the [Earthly Contributor License Agreement](https://gist.github.com/vladaionescu/ed990fa149a38a53ac74b64155bc6766) by logging into GitHub via the CLA assistant and signing the provided CLA. The CLA assistant will automatically notify the PRs that require CLA signing.

#### Entity

If you are an entity, please use the [Earthly Contributor License Agreement form](https://earthly.dev/cla-form) in addition to requiring your individual contributors to sign all contributions.
