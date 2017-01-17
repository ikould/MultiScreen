package com.ebanswers.multiscreen.entrance;

import android.util.Log;

import com.ebanswers.multiscreen.config.XposedConfig;
import com.ebanswers.multiscreen.control.AppControl;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Xposed入口
 * Created by liudong on 2017/1/12.
 */

public class MainXposed implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private XSharedPreferences mPref;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.d("MainXposed", " multiscreen initHook: loadPackageParam = " + loadPackageParam.packageName);
        AppControl.getInstance().initHook(loadPackageParam, mPref);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        mPref =  new XSharedPreferences("com.ebanswers.multiscreen", XposedConfig.SHARD_FILE_NAME);
    }
}
