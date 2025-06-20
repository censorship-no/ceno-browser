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

### android lint

```sh
[bundle exec] fastlane android lint
```

Run gradle lintDebug

### android setVersionNameSample

```sh
[bundle exec] fastlane android setVersionNameSample
```

Set version name in local.properties.sample using latest version listed in CHANGELOG.txt

### android setVersionName

```sh
[bundle exec] fastlane android setVersionName
```

Set version name in local.properties using latest version listed in CHANGELOG.txt

### android cleanAssembleNightly

```sh
[bundle exec] fastlane android cleanAssembleNightly
```

Clean build directory and re-build nightly apks and bundle

### android cleanAssembleRelease

```sh
[bundle exec] fastlane android cleanAssembleRelease
```

Clean build directory and re-build nightly apks and bundle

### android checksumRelease

```sh
[bundle exec] fastlane android checksumRelease
```

Create checksum of releases binaries

### android createNightlyNotes

```sh
[bundle exec] fastlane android createNightlyNotes
```

Create release notes from CHANGELOG.txt to ${VERSION_CODE}.txt for nightly release

### android createReleaseNotes

```sh
[bundle exec] fastlane android createReleaseNotes
```

Create and copy release notes from CHANGELOG.txt to ${VERSION_CODE}.txt for production release

### android cleanRepo

```sh
[bundle exec] fastlane android cleanRepo
```

Clean up repo after changes required for play store upload

### android screenshots

```sh
[bundle exec] fastlane android screenshots
```



### android nightly

```sh
[bundle exec] fastlane android nightly
```

Run tasks if new commits have been made to develop

### android prepareRelease

```sh
[bundle exec] fastlane android prepareRelease
```

Prepare release APKS, AAB, and release notes

### android buildRelease

```sh
[bundle exec] fastlane android buildRelease
```

Build release APKS, AAB, write checksum

### android publishMetadata

```sh
[bundle exec] fastlane android publishMetadata
```

Publish metadata and screenshots to Play Store

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
