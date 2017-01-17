package com.ebanswers.multiscreen.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.ebanswers.multiscreen.CoreApplication;

/**
 * describe
 * Created by liudong on 2017/1/17.
 */

public class XposedConfig {

    public final static String SHARD_FILE_NAME = "Xposed_sp";

    private SharedPreferences mPref;

    private static XposedConfig instance;

    public static XposedConfig getInstance() {
        if (instance == null) {
            synchronized (XposedConfig.class) {
                if (instance == null)
                    instance = new XposedConfig();
            }
        }
        return instance;
    }

    private XposedConfig() {
        mPref = CoreApplication.getInstance().getSharedPreferences(SHARD_FILE_NAME, Context.MODE_WORLD_READABLE);
    }


    /**
     * 设置需要设置多窗口的App信息
     *
     * @param packageName
     * @param width
     * @param height
     * @param gravity
     */
    public void setPackageMsg(String packageName, int width, int height, int gravity) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(packageName, true);
        editor.putInt(packageName + "_width", width);
        editor.putInt(packageName + "_height", height);
        editor.putInt(packageName + "_gravity", gravity);
        editor.commit();
    }
}
