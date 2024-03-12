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

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android cleanAssembleDebugTest

```sh
[bundle exec] fastlane android cleanAssembleDebugTest
```

Build debug and test APK for screenshots

### android assembleDebugTest

```sh
[bundle exec] fastlane android assembleDebugTest
```

Build debug and test APK for screenshots

### android screenshots

```sh
[bundle exec] fastlane android screenshots
```



### android internal

```sh
[bundle exec] fastlane android internal
```

Upload a build to Play Store internal testing track

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Deploy a new version to the Google Play

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
