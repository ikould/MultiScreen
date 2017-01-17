package com.ebanswers.multiscreen.acitivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.ebanswers.multiscreen.R;
import com.ebanswers.multiscreen.config.XposedConfig;
import com.ebanswers.multiscreen.utils.Constants;
import com.ebanswers.multiscreen.utils.ScreenUtil;
import com.ebanswers.multiscreen.utils.SuUtil;


public class MainActivity extends AppCompatActivity {

    public static String[] bottomPackageNames = new String[]{Constants.KUGOU_MUSIC_HD, Constants.ZHANG_CHU, Constants.SYSTEM_SETTING};
    private final int middleHeight = 40;//中间空余的高度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        for (int i = 0; i < bottomPackageNames.length; i++) {
            XposedConfig.getInstance().setPackageMsg(bottomPackageNames[i],
                    ScreenUtil.getScreenWidth(this),
                    (ScreenUtil.getOriginalHeight(this) - middleHeight) / 2,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        }
        XposedConfig.getInstance().setPackageMsg(Constants.FAMILY_ALBUM,
                ScreenUtil.getScreenWidth(this),
                (ScreenUtil.getOriginalHeight(this) - middleHeight) / 2,
                Gravity.TOP | Gravity.CENTER_HORIZONTAL);
    }

    private void initView() {
        //动态设置中间高度
        RelativeLayout buttonGroup = (RelativeLayout) findViewById(R.id.main_button_group);
        View mainLine1 = findViewById(R.id.main_line1);
        RelativeLayout.LayoutParams line1LayoutParams = (RelativeLayout.LayoutParams) mainLine1.getLayoutParams();
        if (line1LayoutParams != null) {
            line1LayoutParams.topMargin = (ScreenUtil.getOriginalHeight(this) - middleHeight) / 2;
            mainLine1.setLayoutParams(line1LayoutParams);
        }
        RelativeLayout.LayoutParams groupLayoutParams = (RelativeLayout.LayoutParams) buttonGroup.getLayoutParams();
        if (groupLayoutParams != null) {
            groupLayoutParams.height = middleHeight;
            buttonGroup.setLayoutParams(groupLayoutParams);
        }
        //启动家庭相册
        startMultiWindowApp(this, Constants.FAMILY_ALBUM);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_kugou:
                startApp(0);
                break;
            case R.id.btn_zhangchu:
                startApp(1);
                break;
            case R.id.btn_setting:
                startApp(2);
                break;
        }
    }

    private void startApp(int index) {
        for (int i = 0; i < bottomPackageNames.length; i++) {
            if (i != index)
                SuUtil.kill(bottomPackageNames[i]);
        }
        startMultiWindowApp(this, bottomPackageNames[index]);
    }

    /**
     * 以多窗口打开App
     *
     * @param context     上下文
     * @param packageName 包名
     * @return
     */
    public static boolean startMultiWindowApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent;
        try {
            intent = new Intent(packageManager.getLaunchIntentForPackage(packageName));
            context.startActivity(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
