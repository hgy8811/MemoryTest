package com.ericpeng.memorytest.killer;

import android.content.Context;

import com.ericpeng.memorytest.R;
import com.ericpeng.memorytest.activity.LeakActivity;

/**
 * Created by liyun on 2016/11/27.
 */

public class MemoryKiller {

    private Context context;

    private String[] strings;

    public MemoryKiller() {
        this(null);
    }

    public MemoryKiller(Context context) {
        this.context = context;

        killMemory();
    }

    private void killMemory() {
        strings = new String[1024 * LeakActivity.MEMORY_KILL_BASE];
        for (int i = 0; i < strings.length; i++) {
            String base = context == null ? "" : context.getResources().getString(R.string.app_name);
            strings[i] = base + " String Object, No." + i;
        }
    }
}
