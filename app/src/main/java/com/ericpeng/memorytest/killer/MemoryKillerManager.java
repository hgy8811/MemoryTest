package com.ericpeng.memorytest.killer;

import com.ericpeng.memorytest.activity.LeakActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyun on 2016/11/27.
 */

public class MemoryKillerManager {

    private MemoryKillerManager() {
        killers = new ArrayList<>();
        callbacks = new ArrayList<>();
    }

    private static class InstanceHolder {
        private final static MemoryKillerManager sInstance = new MemoryKillerManager();
    }

    public static MemoryKillerManager getInstance() {
        return InstanceHolder.sInstance;
    }

    List<MemoryKiller> killers;

    List<LeakActivity.Callback> callbacks;

    public void addKiller(MemoryKiller killer) {
        killers.add(killer);
    }

    public void addInnerClassInstance(LeakActivity.Callback callback) {
        callbacks.add(callback);
    }
}
