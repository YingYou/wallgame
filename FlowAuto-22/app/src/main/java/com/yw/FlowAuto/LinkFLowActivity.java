package com.yw.FlowAuto;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yw on 15/12/9.
 */
public class LinkFLowActivity extends BaseActivity implements View.OnClickListener{

    @ViewInject(R.id.edit_1)
    EditText MonthPlanValue;

    @ViewInject(R.id.edit_2)
    EditText UsedTrafficValue;

    @ViewInject(R.id.edit_4)
    EditText PackagePlusValue;

    @ViewInject(R.id.save_btn)
    Button save_btn;

    @ViewInject(R.id.warningLineValue)
    TextView TWarningLineValue;

    @ViewInject(R.id.warningLineSeekBar)
    SeekBar WarningLineSeekBar;


    private int intMonthPlanValue;//月套餐
    private float floatUsedTrafficValue;//已用流量
    private int intPackagePlusValue;//加油包
    private int intWarningLineSeekBar;//警戒 1-100

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_flow);

        ViewUtils.inject(this);
        initSettingsActionBar(this,"参数设置");

        SharedPreferences userInfo = getSharedPreferences("traffic",MODE_PRIVATE);

        intMonthPlanValue=userInfo.getInt("MonthPlanValue", 0);
        floatUsedTrafficValue=userInfo.getFloat("UsedTrafficValue", 0f);
        intWarningLineSeekBar=userInfo.getInt("WarningLineSeekBar", 100);
        intPackagePlusValue=userInfo.getInt("PackagePlusValue", 0);

        MonthPlanValue.setText(intMonthPlanValue==0?"":(intMonthPlanValue+""));
        WarningLineSeekBar.setProgress(intWarningLineSeekBar);
        UsedTrafficValue.setText(floatUsedTrafficValue==0f?"":(floatUsedTrafficValue+""));
        PackagePlusValue.setText(intPackagePlusValue==0?"":(intPackagePlusValue+""));
        TWarningLineValue.setText(intWarningLineSeekBar+"%");

        if(floatUsedTrafficValue!=0f && intMonthPlanValue !=0 && floatUsedTrafficValue*100/intMonthPlanValue>intWarningLineSeekBar){
            //消息通知栏
            //定义NotificationManager
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
            //定义通知栏展现的内容信息
            int icon = R.drawable.ic_launcher;
            CharSequence tickerText = "流量超警戒啦(￣▽￣')";
            long when = System.currentTimeMillis();
//            Notification notification = new Notification(icon, tickerText, when);

            //定义下拉通知栏时要展现的内容信息
            Context context = getApplicationContext();
            CharSequence contentTitle = "流量超警戒啦(￣▽￣')";
            CharSequence contentText = "小心使用呦，免得浪费RMB(＃°Д°)";
            Intent notificationIntent = new Intent(this, MainActivity.class);
//            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                    notificationIntent, 0);
//            notification.setLatestEventInfo(context, contentTitle, contentText,
//                    contentIntent);

            //用mNotificationManager的notify方法通知用户生成标题栏消息通知
//            mNotificationManager.notify(1, notification);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.notify_icon);
            builder.setWhen(when);
            builder.setTicker(tickerText);
            builder.setContentTitle("流量超警戒啦(￣▽￣')");
            builder.setContentText("小心使用呦，免得浪费RMB(＃°Д°)");
            builder.setAutoCancel(true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            builder.setContentIntent(pendingIntent);
            Notification notification = builder.getNotification();
            mNotificationManager.notify(1, notification);
        }


        WarningLineSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){


            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                // TODO Auto-generated method stub
                TWarningLineValue.setText(WarningLineSeekBar.getProgress()+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intMonthPlanValue =Integer.parseInt(MonthPlanValue.getText().toString().trim().equals("")?"0":MonthPlanValue.getText().toString().trim());
                floatUsedTrafficValue =Float.parseFloat(UsedTrafficValue.getText().toString().trim().equals("")?"0":UsedTrafficValue.getText().toString().trim());
                intWarningLineSeekBar =WarningLineSeekBar.getProgress();
                intPackagePlusValue =Integer.parseInt(PackagePlusValue.getText().toString().trim().equals("")?"0":PackagePlusValue.getText().toString().trim());

                SharedPreferences userInfo = getSharedPreferences("traffic",MODE_PRIVATE);

                //如果已用流量发生改变就存储当前时间
                float temp = userInfo.getFloat("UsedTrafficValue", floatUsedTrafficValue);
                if (temp!=floatUsedTrafficValue) {
                    SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                    String currentTime = formatter.format(curDate);
                    userInfo.edit().putString("Date", currentTime).commit();
                }
                userInfo.edit().putInt("MonthPlanValue", intMonthPlanValue).commit();
                userInfo.edit().putFloat("UsedTrafficValue", floatUsedTrafficValue).commit();
                userInfo.edit().putInt("WarningLineSeekBar", intWarningLineSeekBar).commit();
                userInfo.edit().putInt("PackagePlusValue", intPackagePlusValue).commit();

                Toast toast = Toast.makeText(LinkFLowActivity.this, "保存成功", Toast.LENGTH_SHORT);
                toast.show();

                Intent intent  = new Intent(LinkFLowActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                LinkFLowActivity.this.startActivity(intent);
                finish();
            }
        });


    }

    @Override
    public void onClick(View v) {

        finish();
    }
}
