package com.ericpeng.memorytest.activity;

import android.app.Activity;
import android.os.Bundle;

import com.ericpeng.memorytest.R;
import com.ericpeng.memorytest.killer.MemoryKiller;
import com.ericpeng.memorytest.killer.MemoryKillerManager;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by liyun on 2016/11/27.
 */

public class LeakActivity extends Activity {

    public final static int MEMORY_KILL_BASE = 256;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leak);

        singleInstanceLeak();

//        threadLeak();

//        innerClassLeak();

//        rxJavaLeak();
    }

    private void innerClassLeak() {
        MemoryKillerManager.getInstance().addInnerClassInstance(new Callback());
    }

    private void singleInstanceLeak() {
        MemoryKillerManager.getInstance().addKiller(new MemoryKiller(this));
    }

    private void threadLeak() {
        new Thread() {
            @Override
            public void run() {
                MemoryKiller killer = new MemoryKiller();
                sleepOneHour();
            }
        }.start();
    }

    private void rxJavaLeak() {
        subscription = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("RaJava");
                subscriber.onNext("RaJava");
                subscriber.onNext("Kill Memory");
                subscriber.onCompleted();
            }
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return "Kill Memory".equals(s);
                    }
                })
                .map(new Func1<String, MemoryKiller>() {
                    @Override
                    public MemoryKiller call(String s) {
                        MemoryKiller killer = new MemoryKiller();
                        sleepOneHour();
                        return killer;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MemoryKiller>() {
                    @Override
                    public void call(MemoryKiller memoryKiller) {

                    }
                });
    }

    private Subscription subscription;

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
    }

    private void sleepOneHour() {
        try {
            Thread.sleep(3600 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class Callback {

        private MemoryKiller killer;

        public Callback() {
            killer = new MemoryKiller();
        }
    }
}
