package com.ebanswers.multiscreen.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class ScreenUtil {

    private static boolean isInit;
    private static int originalHeight;
    private static int screenWidth;
    private static int screenHeight;

    public static void initScreenParams(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Log.d("ScreenUtil", "initScreenParams: display.getWidth = " + display.getWidth() + " getHeight = " + display.getHeight());
            display.getMetrics(metrics);
            @SuppressWarnings("rawtypes")
            Class c;
            try {
                c = Class.forName("android.view.Display");
                @SuppressWarnings("unchecked")
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, metrics);
                originalHeight = metrics.heightPixels;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            metrics = context.getResources().getDisplayMetrics();
            Log.d("ScreenUtil", "initScreenParams: e = " + e);

        }
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        isInit = true;
    }

    /**
     * 获取屏幕的宽度px
     *
     * @param context 上下文
     * @return 屏幕宽px
     */
    public static int getScreenWidth(Context context) {
        if (!isInit)
            initScreenParams(context);
        return screenWidth;
    }

    /**
     * 获取屏幕的高度px，不包含虚拟键盘的高度
     *
     * @param context 上下文
     * @return 屏幕高px
     */
    public static int getScreenHeight(Context context) {
        if (!isInit)
            initScreenParams(context);
        return screenHeight;
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 获取屏幕原始尺寸高度，包括虚拟功能键高度
     *
     * @param context
     * @return
     */
    public static int getOriginalHeight(Context context) {
        if (!isInit)
            initScreenParams(context);
        return originalHeight;
    }

    /**
     * 获取 虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getKeyboardHeight(Context context) {
        int totalHeight = getOriginalHeight(context);
        int contentHeight = getScreenHeight(context);
        Log.d("ScreenUtil", "getKeyboardHeight: totalHeight = " + totalHeight + " contentHeight = " + contentHeight);
        return totalHeight - contentHeight;
    }
}
