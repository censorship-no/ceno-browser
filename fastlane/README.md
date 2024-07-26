fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android cleanAssembleDebugTest

```sh
[bundle exec] fastlane android cleanAssembleDebugTest
```

Clean build directory and re-build debug and test apks

### android assembleDebugTest

```sh
[bundle exec] fastlane android assembleDebugTest
```

Build debug and test apks

### android cleanAssembleNightly

```sh
[bundle exec] fastlane android cleanAssembleNightly
```

Clean build directory and re-build nightly apks and bundle

### android screenshots

```sh
[bundle exec] fastlane android screenshots
```



### android nightly

```sh
[bundle exec] fastlane android nightly
```

Run tasks if new commits have been made to develop

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
