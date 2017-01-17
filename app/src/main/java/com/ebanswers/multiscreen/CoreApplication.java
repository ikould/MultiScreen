package com.ebanswers.multiscreen;

import android.app.Application;

/**
 * describe
 * Created by liudong on 2017/1/11.
 */

public class CoreApplication extends Application {

    private static CoreApplication instance;

    public static CoreApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
