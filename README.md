# WeType Origin Fullscreen Hook (experimental)

This is an **LSPosed/Xposed module project** scoped to `com.tencent.wetype`. It is not a normal standalone app that can inject into another process by itself. Device must have a compatible LSPosed/Xposed framework and the module must be enabled for WeChat Input Method.

## What it does

Hooks `InputMethodService.onWindowShown()` and `onStartInputView(EditorInfo, boolean)`. On Android 11+ it calls `Window.setDecorFitsSystemWindows(false)` and makes system bars transparent; on older Android it adds layout-stable/layout-hide-navigation flags.

This is a **new experimental implementation**, not original code extracted from the vivo APK. The static analysis found indicators such as `ImmNavigationBar`, `ImmNavigationBarView`, `refreshDisplayCutoutMode`, `layoutInDisplayCutoutMode`, and OriginUI utility names, but their complete implementation/call graph was not recovered.

## Important limitations

- This generic hook cannot guarantee WeType's internal keyboard root view will correctly account for navigation-bar insets. It may cause keyboard/nav-bar overlap or bottom blank space.
- It does not reproduce OriginOS proprietary behavior; it only tests a generic edge-to-edge window strategy.
- Hooking is framework-dependent. Package/class/method changes in WeType or Android can make it ineffective.
- Make a backup and have a way to disable LSPosed modules from recovery/safe mode before testing.

## Build

Requires Android Studio, JDK 17, Android SDK Platform 35, network access to Google Maven and Maven Central, and Gradle 8.7-compatible tooling for Android Gradle Plugin 8.6.1.

Open this folder in Android Studio, sync, then run `:app:assembleDebug`. The APK should be at `app/build/outputs/apk/debug/app-debug.apk`.

## Install/test

1. Install the built APK.
2. In LSPosed Manager, enable the module and scope it only to `com.tencent.wetype`.
3. Force-stop WeChat Input Method and reopen it.
4. Test gesture navigation and 3-button navigation; portrait/landscape; keyboard show/hide; candidate bar and emoji panels; display cutout devices.
5. Inspect LSPosed logs for `WeTypeOriginHook` and verify no overlap, clipped keys, or abnormal bottom padding.
6. If layout regresses, disable this module in LSPosed and force-stop WeType.

## Rollback
Disable the module in LSPosed. This project does not modify the WeType APK on disk.

## One-click cloud build (GitHub Actions)

The repository includes `.github/workflows/build-apk.yml`. Upload/push this project to a GitHub repository, open **Actions → Build LSPosed APK → Run workflow**, then download the `WeTypeOriginFullscreenHook-debug-apk` artifact from the completed run. This produces a debug-signed APK; for Play Store/release distribution, configure your own release signing key securely.
