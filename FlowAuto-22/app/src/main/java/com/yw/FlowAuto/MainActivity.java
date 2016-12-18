package com.yw.FlowAuto;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.bigkoo.svprogresshud.SVProgressHUD;
import com.jtschohl.androidfirewall.Api;
import com.jtschohl.androidfirewall.RootShell;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.timqi.sectorprogressview.ColorfulRingProgressView;
import com.umeng.update.UmengUpdateAgent;
import com.yw.FlowAuto.util.MySQLiteOpenHelper;
import com.yw.FlowAuto.util.TrafficInfo;
import com.yw.FlowAuto.util.Utils;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends BaseActivity implements View.OnClickListener
{

    @ViewInject(R.id.crpv)
    ColorfulRingProgressView colorfulRingProgressView;
    @ViewInject(R.id.currentUserFlow)
    TextView currentUserFlow;//本月已用
    @ViewInject(R.id.dayUserFlow)
    TextView dayUserFlow;//今日已用
    @ViewInject(R.id.monthUserFlow)
    TextView monthUserFlow;//距月结日

    @ViewInject(R.id.tvPercent)
    TextView tvPercent;

    @ViewInject(R.id.assist_wifi_3g)
    TextView assist_wifi_3g;

    @ViewInject(R.id.wifi_conent)
    TextView wifi_conent;

    @ViewInject(R.id.flow_circle_value)
    TextView flow_circle_value;

    @ViewInject(R.id.flow_day_value)
    TextView flow_day_value;

    MySQLiteOpenHelper dbHelper;
    ArrayList<TrafficInfo> appList;
    ArrayList<TrafficInfo>todayAppList;

    public static final String TAG = "MainActivity";
    /** indicates if the view has been modified and not yet saved */
    private boolean dirty = false;

    private double usedFlowMonth;
    float trafficText;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try {
			/* enable hardware acceleration on Android >= 3.0 */
            final int FLAG_HARDWARE_ACCELERATED = WindowManager.LayoutParams.class
                    .getDeclaredField("FLAG_HARDWARE_ACCELERATED").getInt(null);
            getWindow().setFlags(FLAG_HARDWARE_ACCELERATED,
                    FLAG_HARDWARE_ACCELERATED);
        } catch (Exception e) {
        }
        checkPreferences();

        setContentView(R.layout.main);
        ViewUtils.inject(this);

        UmengUpdateAgent.update(this);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        Api.assertBinaries(this, true);
        initHomeActionBar(this);
        initCurrentWIFI();



        MySQLiteOpenHelper dbhelper = new MySQLiteOpenHelper(this);
        SQLiteDatabase db = dbhelper.getWritableDatabase();

        long sum=0;
        float usedTrafficText=0;

        SharedPreferences userInfo = getSharedPreferences("traffic",
                Context.MODE_PRIVATE);


        float monthPlan = userInfo.getInt("MonthPlanValue", 0);

        //以下一段是月初加油包清零
        int monthFromFile = userInfo.getInt("Month", 1);
        Date date=new Date();

        //今天是几号
        int day=date.getDate();
        Calendar c=Calendar.getInstance();
        c.setTime(date);
        int month = c.get(Calendar.MONTH)+1;
        if (monthFromFile != month){

            SharedPreferences.Editor editor = userInfo.edit();

            editor.putInt("Month", month);
            editor.putInt("PackagePlusValue", 0);

            editor.commit();
        }
//读取手动流量校验的月份
        String changedDate = userInfo.getString("Date", "2000-13-32 25:61:61");
        int changedMonth =  Integer.parseInt(String.valueOf(changedDate.charAt(5))
                + String.valueOf(changedDate.charAt(6)));

        //根据本月是否手动校正过读取已用流量
        if (month!=changedMonth){
            String sql = "select GPRS from TrafficInfo where time>=datetime('now', 'start of month')";
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                sum += cursor.getLong(cursor.getColumnIndex("GPRS"));
                Log.i("trafficDisplay", sum + "M");
            }
            usedTrafficText = ((float)sum)/1024/1024;
        } else {
            String sql = "select GPRS from TrafficInfo where time>=datetime('"+ changedDate +"')";
            Cursor cursor = db.rawQuery(sql, null);
            //
            while (cursor.moveToNext()) {
                sum += cursor.getLong(cursor.getColumnIndex("GPRS"));
                Log.i("trafficDisplay", sum + "M");
            }
            usedTrafficText = ((float)sum)/1024/1024;
            usedTrafficText += userInfo.getFloat("UsedTrafficValue", 0);
        }

        //
        SharedPreferences.Editor editorflow = userInfo.edit();
        //
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//
        String currentTime = formatter.format(curDate);
        userInfo.edit().putString("Date", currentTime).commit();
        editorflow.putFloat("UsedTrafficValue", usedTrafficText);

        editorflow.commit();

        trafficText = monthPlan + userInfo.getInt("PackagePlusValue", 0);

        //
        DecimalFormat df = new DecimalFormat("0.00");
        currentUserFlow.setText(df.format(usedTrafficText) + "M");
        usedFlowMonth = usedTrafficText*1024*1024;
        monthUserFlow.setText(df.format(trafficText) + "M");

//        float usedPercentage = ((trafficText-usedTrafficText) / trafficText) ;
//
//        float tvParentFloat = trafficText - usedTrafficText;
//        if (tvParentFloat >= 1024){
//
//            tvPercent.setText(String.valueOf(tvParentFloat/1024)+"G");
//        }else
//        tvPercent.setText(String.valueOf(tvParentFloat)+"M");

        toggleLogtarget();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isCurrentUserOwner(getApplicationContext());
            SharedPreferences prefs2 = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            final String chainName = prefs2.getString("chainName", "");
            Log.d(TAG, "Executed isCurrentUserOwner " + chainName);
        } else {
            final SharedPreferences prefs2 = getSharedPreferences(
                    Api.PREFS_NAME, 0);
            final SharedPreferences.Editor editor = prefs2.edit();
            editor.putString("chainName", "droidwall");
            editor.commit();
            Log.d(TAG,
                    "Skipping isCurrentUserOwner "
                            + prefs2.getString("chainName", ""));
        }

        toggleUser();

//
//        colorfulRingProgressView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ObjectAnimator anim = ObjectAnimator.ofFloat(v, "percent",
//                        0, ((ColorfulRingProgressView) v).getPercent());
//                anim.setInterpolator(new LinearInterpolator());
//                anim.setDuration(1000);
//                anim.start();
//            }
//        });

//        colorfulRingProgressView.setPercent(usedPercentage*100);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.back_flow:
                break;

            case R.id.detail_iv:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,FlowDetailActivity.class);
                startActivity(intent);
                break;

            case R.id.setting_iv:

                Intent intent1 = new Intent();
                intent1.setClass(MainActivity.this,SettingsActivity.class);
                startActivity(intent1);

                break;
        }
    }

    /**
     * Check if the stored preferences are OK
     */
    private void checkPreferences() {
        final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = prefs.edit();
        boolean changed = false;
        if (prefs.getString(Api.PREF_MODE, "").length() == 0) {
            editor.putString(Api.PREF_MODE, Api.MODE_WHITELIST);
            changed = true;
        }
		/* delete the old preference names */
        if (prefs.contains("AllowedUids")) {
            editor.remove("AllowedUids");
            changed = true;
        }
        if (prefs.contains("Interfaces")) {
            editor.remove("Interfaces");
            changed = true;
        }
        if (changed)
            editor.commit();
    }

    /**
     * many thanks to Mr. Cernekee for this GPLv3 code Original code located
     * here:
     *
     * https://github.com/ukanth/afwall/blob/master/src/dev/ukanth/ufirewall/Api
     * .java
     *
     */

    private void toggleLogtarget() {
        final Context ctx = getApplicationContext();

        LogProbeCallback cb = new LogProbeCallback();
        cb.ctx = ctx;

        new RootShell.RootCommand().setReopenShell(true)
                .setFailureToast(R.string.log_failed).setCallback(cb)
                .setLogging(true).run(ctx, "cat /proc/net/ip_tables_targets");
    }

    private class LogProbeCallback extends RootShell.RootCommand.Callback {
        public Context ctx;

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                Api.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = prefs.edit();
        boolean nflog = false;
        boolean log = false;

        public void cbFunc(RootShell.RootCommand state) {
            if (state.exitCode != 0) {
                return;
            }

            for (String str : state.lastCommandResult.toString().split("\n")) {
                if ("NFLOG".equals(str)) {
                    nflog = true;
                    Log.d(TAG, "NFLOG fetch " + Api.PREF_LOGTARGET);
                } else if ("LOG".equals(str)) {
                    Log.d(TAG, "LOG fetch " + Api.PREF_LOGTARGET);
                    log = true;
                }
                if (nflog == true && log == true) {
                    editor.putString(Api.PREF_LOGTARGET, "LOG");
                    editor.commit();
                }
                if (log == true && nflog == false) {
                    editor.putString(Api.PREF_LOGTARGET, "LOG");
                    editor.commit();
                }
                if (log == false && nflog == true) {
                    editor.putString(Api.PREF_LOGTARGET, "NFLOG");
                    editor.commit();
                }
                if (log == false && nflog == false) {
                    editor.putString(Api.PREF_LOGTARGET, "");
                    editor.commit();
                    Log.d(TAG, "Empty fetch " + Api.PREF_LOGTARGET);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    public void isCurrentUserOwner(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Method getUserHandle = UserManager.class
                        .getMethod("getUserHandle");
                int userHandle = (Integer) getUserHandle.invoke(context
                        .getSystemService(Context.USER_SERVICE));
                Log.d(TAG, String.format("Found user value = %d", userHandle));
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("userID", userHandle);
                editor.commit();
            } catch (Exception e) {
                Log.d(TAG, "Exception on isCurrentUserOwner " + e.getMessage());
            }
        }
    }

    public void toggleUser() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        boolean multiuserenabled = getApplicationContext()
                .getSharedPreferences(Api.PREFS_NAME, 0).getBoolean(
                        Api.PREF_MULTIUSER, false);
        if (prefs.getLong("userID", 0) == 0) {
            Log.d(TAG, "userHandle is 0");
            editor.putString("chainName", "droidwall");
            editor.commit();
            Log.d(TAG, "User = " + prefs.getLong("userID", 0)
                    + " and CHAINNAME = " + prefs.getString("chainName", ""));
        } else {
            Log.d(TAG, "userHandle greater than 0");
            if (multiuserenabled) {
                editor.putString("chainName", prefs.getLong("userID", 0)
                        + "droidwall");
                editor.commit();
                Log.d(TAG,
                        "User = " + prefs.getLong("userID", 0)
                                + " and CHAINNAME = "
                                + prefs.getString("chainName", ""));
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.multiuser_title);
                builder.setMessage(R.string.multiuser_disabled);
                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
//                                userSettings();
                            }
                        });
                AlertDialog dialog = builder.show();
                TextView msg = (TextView) dialog
                        .findViewById(android.R.id.message);
                msg.setGravity(Gravity.CENTER);
            }
        }
    }


    private void initCurrentWIFI(){

        String  isWifi = Utils.isWIfiConnect(MainActivity.this);
        if(isWifi.equals("1")){

            Drawable drawable= getResources().getDrawable(R.drawable.assist_switcher_wifi_on);

            /// 这一步必须要做,否则不会显示.

            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

            assist_wifi_3g.setCompoundDrawables(drawable,null,null,null);
            assist_wifi_3g.setText("WIFI");
            wifi_conent.setText("正在使用WIFI网络");
        }else if(isWifi.equals("2")){

            Drawable drawable= getResources().getDrawable(R.drawable.assist_switcher_mobile_data_on);

            /// 这一步必须要做,否则不会显示.

            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

            assist_wifi_3g.setCompoundDrawables(drawable,null,null,null);
            assist_wifi_3g.setText("2g/3g/4g");
            wifi_conent.setText("正在使用移动网络");
        }else {
            SVProgressHUD.showErrorWithStatus(MainActivity.this,"无网络连接");
            wifi_conent.setText("当前无网络连接");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTask();
    }

    private void startTask(){

       new AsyncTask<Void,Void,String>(){

           @Override
           protected void onPreExecute() {
               SVProgressHUD.showWithProgress(MainActivity.this,"加载中", SVProgressHUD.SVProgressHUDMaskType.None);
               dbHelper = new MySQLiteOpenHelper(MainActivity.this);
           }

           @Override
           protected String doInBackground(Void... voids) {

               long allFlowCount = 0;
               long todayAllFlowCount = 0;
               long allFlowMobileCount = 0;
               long todatAllMobileFlowCount = 0;
               appList = dbHelper.queryByTime(MainActivity.this,"sameMonth");
               todayAppList = dbHelper.queryByTime(MainActivity.this,"today");
               if (appList != null){
                   for (int i = 0; i < appList.size(); i++) {

                       allFlowCount = allFlowCount + appList.get(i).getWIFI();
                       allFlowMobileCount = allFlowMobileCount + appList.get(i).getGPRS();
                   }

               }


               if (todayAppList != null){
                   for (int i = 0; i < todayAppList.size(); i++) {

                       todayAllFlowCount = todayAllFlowCount + todayAppList.get(i).getWIFI();
                       todatAllMobileFlowCount = todatAllMobileFlowCount +todayAppList.get(i).getGPRS();
                   }
               }


               return Utils.size2string(allFlowCount)+"_"+
                       Utils.size2string(todayAllFlowCount)+"_"+
                       String.valueOf(allFlowMobileCount)+"_"+
                       Utils.size2string(todatAllMobileFlowCount);
           }

           @Override
           protected void onPostExecute(String s) {

               // 注意要加\\,要不出不来,yeah
               String[] names = s.split("\\_");
               SVProgressHUD.dismiss(MainActivity.this);
               flow_circle_value.setText(names[0]);//本月已用
               flow_day_value.setText(names[1]);
               currentUserFlow.setText(Utils.size2string(Long.valueOf(names[2])+((long)usedFlowMonth)));

               dayUserFlow.setText(names[3]);

//               float usedPercentage = ((trafficText*1024*1024-Long.valueOf(names[2])-(long)usedFlowMonth)) / trafficText*1024*1024 ;

               float tvParentFloat = trafficText*1024*1024 - Long.valueOf(names[2])-((long)usedFlowMonth);
               tvPercent.setText(Utils.size2string((long)tvParentFloat));
               float usedPercentage = tvParentFloat/(trafficText*1024*1024);
Log.d("TAG","perce=="+usedPercentage);
               colorfulRingProgressView.setPercent(usedPercentage*100);

           }
       }.execute();
   }
}
