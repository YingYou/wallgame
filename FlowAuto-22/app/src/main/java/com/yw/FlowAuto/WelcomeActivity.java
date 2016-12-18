package com.yw.FlowAuto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateConfig;
import com.yw.FlowAuto.service.TrafficService;

/**
 * Created by yw on 15/12/17.
 */
public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_wecome);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

//        UpdateConfig.setDebug(true);
        Intent MainService=new Intent(WelcomeActivity.this, TrafficService.class);
        startService(MainService);//启动服务

        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        },1000);
    }
}
