# Vardely

Vardely is an Arabic and English Android focus app. It lets a person choose installed apps to block, optionally protect selected apps with a local PIN, and view on-device block counts.

## Included in this build

- Arabic and English interface
- Installed-app list with each app's launcher icon and search
- Opt-in app blocking through Android Accessibility
- Optional PIN lock for selected apps
- Daily and seven-day local activity counts
- Custom Vardely launcher icon
- No advertising, analytics, account, or server connection

The Accessibility service checks the active app's package name only. It cannot retrieve screen content and does not transmit data. App blocking and app locking require the user to enable Android Accessibility access after reading the in-app disclosure.

## Build

Use JDK 17, Android SDK 36, and Gradle 8.13:

```sh
gradle --no-daemon assembleDebug
gradle --no-daemon lintDebug
gradle --no-daemon bundleRelease
```

The debug APK is for device testing. The release AAB produced without a private upload key is unsigned and cannot be uploaded to Google Play. Never commit signing keys or passwords to this repository.

## Store artwork

Editable Canva feature graphic (1024 × 500): https://www.canva.com/d/3_qz8hIOhWj-fkR

## Privacy

See [PRIVACY.md](PRIVACY.md). The Play listing and policy contact details must match the developer's verified account before submission.
