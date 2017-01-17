package com.ebanswers.multiscreen.control;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;

import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * App窗口控制
 * <p>
 * 1.将XposedBridgeApi-54.jar放入main中lib下
 * 2.app build中 provided files('src/main/lib/XposedBridgeApi-54.jar')
 * 3.assets 中xposed_init指向Xposed入口MainXposed.java，实现IXposedHookLoadPackage接口
 * 4.MainXposed中注册AppControl.getInstance().initHook();
 * Created by liudong on 2017/1/12.
 */

public class AppControl {

    private static final int DEFAULT_WIDTH_SIZE = 400;
    private static final int DEFAULT_HEIGHT_SIZE = 400;

    private static final String LOG_TAG = "XHaloFloatingWindow(SDK: " + Build.VERSION.SDK_INT + ") - ";

    private static AppControl instance;
    private XSharedPreferences mPref;

    public static AppControl getInstance() {
        if (instance == null) {
            synchronized (AppControl.class) {
                instance = new AppControl();
            }
        }
        return instance;
    }

    private AppControl() {
    }

    public void initHook(XC_LoadPackage.LoadPackageParam lpparam, XSharedPreferences mPref) {
        this.mPref = mPref;
        if (lpparam.packageName.equals("android")) {
            try {
                onHookActivityRecord(lpparam);
                removeAppStartingWindow(lpparam);
            } catch (Throwable e) {
                XposedBridge.log(LOG_TAG + "(ActivityStack)");
                XposedBridge.log(e);
            }
        }

        try {
            onHookAppOnCreate(lpparam);
        } catch (Throwable e) {
            XposedBridge.log(LOG_TAG + "(inject_Activity)");
            XposedBridge.log(e);
        }

        try {
            injectGenerateLayout(lpparam);
        } catch (Throwable e) {
            XposedBridge.log(LOG_TAG + "(injectGenerateLayout)");
            XposedBridge.log(e);
        }
    }

    /**
     * Activity onCreate Hooks
     *
     * @param lpparam
     * @throws Throwable
     */
    private void onHookAppOnCreate(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.hookAllMethods(Activity.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Activity activity = (Activity) param.thisObject;
                String packageName = activity.getPackageName();
                Log.d("AppControl", "onHookAppOnCreate: onCreate packageName = " + packageName);
                if (isAppPass(packageName)) {
                    setWindowStyle(activity.getWindow(), packageName);
                }
            }
        });
    }

    /**
     * 设置Window的样式（宽、高、方位，window类型）
     *
     * @param window
     * @param packageName
     */
    private void setWindowStyle(Window window, String packageName) {
        mPref.reload();
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        addPrivateFlagNoMoveAnimationToLayoutParam(layoutParams);
        window.setLayout(mPref.getInt(packageName + "_width", DEFAULT_WIDTH_SIZE), mPref.getInt(packageName + "_height", DEFAULT_HEIGHT_SIZE));
        window.setGravity(mPref.getInt(packageName + "_gravity", Gravity.TOP | Gravity.CENTER_HORIZONTAL));
    }

    /**
     * ActivityRecord Hook
     *
     * @param lpparam
     * @throws Throwable
     */
    private void onHookActivityRecord(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Class<?> classActivityRecord = findClass("com.android.server.am.ActivityRecord",
                lpparam.classLoader);
        XposedBridge.hookAllConstructors(classActivityRecord,
                new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = null;
                        ActivityInfo activity_info = null;
                        if (Build.VERSION.SDK_INT >= 19) { // Android 4.4 onwards
                            intent = (Intent) param.args[4];
                            activity_info = (ActivityInfo) param.args[6];
                        } else if (Build.VERSION.SDK_INT == 18) { // Android 4.3
                            intent = (Intent) param.args[5];
                            activity_info = (ActivityInfo) param.args[7];
                        } else if (Build.VERSION.SDK_INT <= 17) { // Android 4.2 & below
                            intent = (Intent) param.args[4];
                            activity_info = (ActivityInfo) param.args[6];
                        }
                        boolean isAppPass = isAppPass(activity_info.packageName);
                        Log.d("AppControl", "afterHookedMethod: activity_info.applicationInfo.packageName = "
                                + activity_info.packageName + " isAppPass = " + isAppPass);
                        if (intent == null || !isAppPass)
                            return;
                        XposedHelpers.setBooleanField(param.thisObject, "fullscreen", false);
                    }
                }
        );
    }

    /**
     * 实现Appp打开出现黑色背景 1）
     *
     * @param lpp
     * @throws Throwable
     */
    private void injectGenerateLayout(final XC_LoadPackage.LoadPackageParam lpp)
            throws Throwable {
        Class<?> cls = findClass("com.android.internal.policy.impl.PhoneWindow", lpp.classLoader);
        XposedBridge.hookAllMethods(cls, "generateLayout", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Window window = (Window) param.thisObject;
                String packageName = window.getContext().getPackageName();
                Log.d("AppControl", "injectGenerateLayout afterHookedMethod: PhoneWindow packageName = " + packageName + " method = " + param.method.getName());
                if (packageName.startsWith("com.android.systemui")) return;
                if (packageName.equals("android")) return;
                if (mPref != null && mPref.getBoolean(packageName, false)) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
                    window.setWindowAnimations(android.R.style.Animation_Dialog);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
                }
            }
        });
    }

    /**
     * 实现Appp打开出现黑色背景 2）
     * <p>
     * Removes the app starting placeholder screen before the app contents is shown.
     * Does this by making 'createIfNeeded' to false
     */
    private void removeAppStartingWindow(final XC_LoadPackage.LoadPackageParam lpp) throws Throwable {
        Class<?> hookClass = findClass("com.android.server.wm.WindowManagerService", lpp.classLoader);
        XposedBridge.hookAllMethods(hookClass, "setAppStartingWindow", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("AppControl", "hookAllMethods: setAppStartingWindow beforeHookedMethod");
                if ("android".equals((String) param.args[1])) return;
                if (param.args[param.args.length - 1] instanceof Boolean) {
                    param.args[param.args.length - 1] = Boolean.FALSE;
                }
            }
        });
    }

    /**
     * 是否是需要过滤的APP
     *
     * @param packageName
     * @return
     */
    private boolean isAppPass(String packageName) {
        mPref.reload();
        return mPref.getBoolean(packageName, false);
    }

    public static void addPrivateFlagNoMoveAnimationToLayoutParam(WindowManager.LayoutParams params) {
        if (Build.VERSION.SDK_INT <= 15) return;
        try {
            Field fieldPrivateFlag = XposedHelpers.findField(WindowManager.LayoutParams.class, "privateFlags");
            fieldPrivateFlag.setInt(params, (fieldPrivateFlag.getInt(params) | 0x00000040));
        } catch (Exception e) {
            Log.d("AppControl", "addPrivateFlagNoMoveAnimationToLayoutParam: e = " + e);
        }
    }
}
