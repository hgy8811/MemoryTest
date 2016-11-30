package com.ericpeng.memorytest.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ericpeng.memorytest.R;
import com.ericpeng.memorytest.res.NResources;

/**
 * Created by liyun on 2016/11/27.
 */

public class NResourcesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nresources);

//        findViewById(R.id.imageView).setBackgroundResource(R.drawable.main_bg);
        NResources.getInstance().setBackground(findViewById(R.id.imageView), R.drawable.main_bg);
    }
}
