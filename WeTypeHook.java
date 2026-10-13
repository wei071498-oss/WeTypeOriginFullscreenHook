package com.example.wetypeoriginhook;

import android.app.Dialog;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Color;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Experimental LSPosed hook scoped to com.tencent.wetype.
 * This is a new implementation, not code extracted from vivo's APK.
 */
public final class WeTypeHook implements IXposedHookLoadPackage {
    private static final String TARGET = "com.tencent.wetype";
    private static final String TAG = "WeTypeOriginHook";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) return;
        XposedBridge.log(TAG + ": loaded in " + lpparam.packageName);

        try {
            XposedHelpers.findAndHookMethod(
                InputMethodService.class,
                "onWindowShown",
                new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        tryApply((InputMethodService) param.thisObject);
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": onWindowShown hook failed: " + t);
        }

        try {
            XposedHelpers.findAndHookMethod(
                InputMethodService.class,
                "onStartInputView",
                android.view.inputmethod.EditorInfo.class,
                boolean.class,
                new XC_MethodHook() {
                    @Override protected void afterHookedMethod(MethodHookParam param) {
                        tryApply((InputMethodService) param.thisObject);
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": onStartInputView hook failed: " + t);
        }
    }

    private static void tryApply(InputMethodService service) {
        try {
            Window window = service.getWindow().getWindow();
            if (window == null) return;
            if (Build.VERSION.SDK_INT >= 30) {
                window.setDecorFitsSystemWindows(false);
                window.setNavigationBarColor(Color.TRANSPARENT);
                window.setStatusBarColor(Color.TRANSPARENT);
                if (Build.VERSION.SDK_INT >= 29) {
                    window.setNavigationBarContrastEnforced(false);
                    window.setStatusBarContrastEnforced(false);
                }
            } else {
                View decor = window.getDecorView();
                int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                decor.setSystemUiVisibility(decor.getSystemUiVisibility() | flags);
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
            XposedBridge.log(TAG + ": applied edge-to-edge to IME window");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": apply failed: " + t);
        }
    }
}
