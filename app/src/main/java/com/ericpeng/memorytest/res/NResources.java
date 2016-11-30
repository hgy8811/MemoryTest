package com.ericpeng.memorytest.res;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.ericpeng.memorytest.MemoryApplication;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NResources {

    private Context context;
    private Handler mUIHandler;
    private ExecutorService mPools;
    private ArrayMap<String, NRunnable> nRunnableArrayMap;
    private boolean released = false;

    private NResources() {
        mUIHandler = new Handler(Looper.getMainLooper());
        nRunnableArrayMap = new ArrayMap<>();
        mPools = new ThreadPoolExecutor(1, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        context = MemoryApplication.getContext();
    }

    /**
     * call this method when your process exit.
     */
    public void release() {
        mUIHandler.removeCallbacksAndMessages(null);
        mPools.shutdown();

        released = true;
        nRunnableArrayMap = null;
        mUIHandler = null;
        mPools = null;
        context = null;
    }

    private static class InstanceHolder {
        private final static NResources sInstance = new NResources();
    }

    public static NResources getInstance() {
        return InstanceHolder.sInstance;
    }

    public void setBackground(View view, int resId) {
        setBackground(view, "", resId);
    }

    public void setBackground(View view, String filePath) {
        setBackground(view, filePath, 0);
    }

    public void setBackground(View view, String filePath, int defaultResId) {
        set("Background@" + view.hashCode(), view, filePath, defaultResId);
    }

    public void setSrc(ImageView view, int resId) {
        setSrc(view, "", resId);
    }

    public void setSrc(ImageView view, String filePath) {
        setSrc(view, filePath, 0);
    }

    public void setSrc(ImageView view, String filePath, int defaultResId) {
        set("Src@" + view.hashCode(), view, filePath, defaultResId);
    }

    private void set(String key, View view, String filePath, int defaultResId) {
        if (released) return;

        NRunnable oldLoadRunnable = nRunnableArrayMap.get(key);
        if (oldLoadRunnable != null) oldLoadRunnable.abort();

        NRunnable loadRunnable = new LoadRunnable(new Wrapper(key, view, defaultResId, filePath));
        nRunnableArrayMap.put(key, loadRunnable);
        mPools.execute(loadRunnable);
    }

    private class LoadRunnable implements NRunnable {
        private Wrapper wrapper;
        private DisplayRunnable displayRunnable;
        private boolean abort = false;

        public LoadRunnable(Wrapper wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public void run() {
            String key = wrapper.runnableKey;
            boolean decodeFile = false;
            if (!TextUtils.isEmpty(wrapper.filePath) && new File(wrapper.filePath).exists()) decodeFile = true;

            View view = wrapper.viewWeakRef.get();
            Drawable drawable = null;
            Bitmap bitmap = null;
            displayRunnable = new DisplayRunnable();
            displayRunnable.setDisplayType(key.startsWith("Src") ? DisplayType.SRC : DisplayType.BACKGROUND);

            // Setup 1, try load drawable or default res.
            try {
                if (!abort) drawable = context.getResources().getDrawable(wrapper.resId);
            } catch (Throwable e) {
                // include all error, e.g. ResourcesNotFound\OOM
                e.printStackTrace();
            }

            // Setup 2, if got drawable from res, display it.
            if ((!decodeFile || drawable != null) && !abort) {
                displayRunnable.fill(wrapper.viewWeakRef ,drawable);
                mUIHandler.post(displayRunnable);
            }

            // Setup 3, decode bitmap form file, if need be.
            if (decodeFile && !abort) {
                int width = view.getWidth();
                int height = view.getHeight();
                bitmap = BitmapUtil.decodeFile(wrapper.filePath, width, height);
            }

            // Setup 4, if got bitmap from file, display it.
            if (bitmap != null && !bitmap.isRecycled() && !abort) {
                displayRunnable.fill(wrapper.viewWeakRef, new BitmapDrawable(bitmap));
                mUIHandler.post(displayRunnable);
            }

            // Setup 5, remove task record.
            nRunnableArrayMap.remove(key);
        }

        @Override
        public void abort() {
            if (displayRunnable != null) displayRunnable.abort();
            abort = true;
        }
    }

    private class DisplayRunnable implements NRunnable {
        private WeakReference<View> viewWeakRef;
        private Drawable drawable;
        private DisplayType displayType = DisplayType.BACKGROUND;
        private boolean abort = false;

        public void fill(WeakReference<View> viewWeakRef, Drawable drawable) {
            this.viewWeakRef = viewWeakRef;
            this.drawable = drawable;
        }

        public void setDisplayType(DisplayType displayType) {
            this.displayType = displayType;
        }

        @Override
        public void run() {
            if (abort) return;

            View view = viewWeakRef.get();

            if (view != null && !abort) {
                switch (displayType) {
                    case SRC:
                        if (view instanceof ImageView) ((ImageView) view).setImageDrawable(drawable);
                        break;
                    case BACKGROUND:
                        view.setBackgroundDrawable(drawable);
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void abort() {
            abort = true;
        }
    }

    private enum DisplayType {
        BACKGROUND, SRC
    }

    interface NRunnable extends Runnable {
        void abort();
    }

    class Wrapper {
        String runnableKey;
        WeakReference<View> viewWeakRef;
        String filePath;
        int resId;

        public Wrapper(String key, View view, int resId, String filePath) {
            this.runnableKey = key;
            this.viewWeakRef = new WeakReference<>(view);
            this.resId = resId;
            this.filePath = filePath;
        }
    }
}