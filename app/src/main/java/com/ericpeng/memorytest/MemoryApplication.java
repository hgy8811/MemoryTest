package com.ericpeng.memorytest;

import android.app.Application;
import android.content.Context;

/**
 * Created by ericpeng on 16-11-29.
 */

public class MemoryApplication extends Application {

    protected static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
