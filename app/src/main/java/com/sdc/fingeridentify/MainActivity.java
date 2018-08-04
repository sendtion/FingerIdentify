package com.sdc.fingeridentify;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wei.android.lib.fingerprintidentify.FingerprintIdentify;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

/**
 * 指纹识别
 * https://github.com/uccmawei/FingerprintIdentify/blob/master/other/README_ZH.md
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView startVerify;

    private AlertDialog dialog;
    private FingerprintIdentify mFingerprintIdentify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        startVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    private void initView() {
        startVerify = (TextView) findViewById(R.id.tv_start_verify);

        mFingerprintIdentify = new FingerprintIdentify(this);                       // 构造对象
        mFingerprintIdentify = new FingerprintIdentify(this, new BaseFingerprint.FingerprintIdentifyExceptionListener() {
            @Override
            public void onCatchException(Throwable exception) {
                dialog.dismiss();
                Log.e(TAG, "onCatchException: " + exception.getMessage() );
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });    // 构造对象，并监听错误回调（错误仅供开发使用）
        mFingerprintIdentify.isFingerprintEnable();                                 // 指纹硬件可用并已经录入指纹
        mFingerprintIdentify.isHardwareEnable();                                    // 指纹硬件是否可用
        mFingerprintIdentify.isRegisteredFingerprint();                             // 是否已经录入指纹
        //mFingerprintIdentify.startIdentify(maxTimes, listener);                     // 开始验证指纹识别
        //mFingerprintIdentify.cancelIdentify();                                      // 关闭指纹识别
        //mFingerprintIdentify.resumeIdentify();                                      // 恢复指纹识别并保证错误次数不变

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("请开始验证指纹");
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFingerprintIdentify.cancelIdentify();
            }
        });
        builder.setPositiveButton("使用密码", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFingerprintIdentify.cancelIdentify();
                Toast.makeText(MainActivity.this, "使用密码", Toast.LENGTH_SHORT).show();
            }
        });
        dialog = builder.create();
    }

    private void showDialog(){
        startVerify();

        dialog.show();
    }

    private void startVerify() {
        mFingerprintIdentify.startIdentify(3, new BaseFingerprint.FingerprintIdentifyListener() {
            @Override
            public void onSucceed() {
                dialog.dismiss();
                // 验证成功，自动结束指纹识别
                Toast.makeText(MainActivity.this, "识别成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotMatch(int availableTimes) {
                // 指纹不匹配，并返回可用剩余次数并自动继续验证
                Toast.makeText(MainActivity.this, "指纹不匹配，剩余" + availableTimes + "次", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                dialog.dismiss();
                // 错误次数达到上限或者API报错停止了验证，自动结束指纹识别
                // isDeviceLocked 表示指纹硬件是否被暂时锁定
                if (isDeviceLocked){
                    Toast.makeText(MainActivity.this, "指纹识别被锁定，请稍后再试", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "识别失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartFailedByDeviceLocked() {
                dialog.dismiss();
                // 第一次调用startIdentify失败，因为设备被暂时锁定
                Toast.makeText(MainActivity.this, "识别失败，设备被暂时锁定", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
