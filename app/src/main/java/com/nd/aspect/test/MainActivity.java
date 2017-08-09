package com.nd.aspect.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Button mBtnClickToast;
    private Button mBtnClickToast2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnClickToast = (Button)findViewById(R.id.btn_click_toast);
        mBtnClickToast.setOnClickListener(this);
        //增加了一个Button
        mBtnClickToast2 = (Button)findViewById(R.id.btn_click_toast2);
        mBtnClickToast2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        if(viewId == R.id.btn_click_toast) {
            Toast.makeText(this, "点击了弹出Toast的Button", Toast.LENGTH_LONG).show();
            Log.d(TAG, "点击了弹出Toast的Button");
        } else if(viewId == R.id.btn_click_toast2) {
            //第二个Button的点击处理
            Toast.makeText(this, "点击了第二个Button", Toast.LENGTH_LONG).show();
            Log.d(TAG, "点击了第二个Button");
        }

    }
}
