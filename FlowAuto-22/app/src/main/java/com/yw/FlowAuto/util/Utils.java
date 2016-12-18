package com.yw.FlowAuto.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DecimalFormat;

/**
 * Created by yw on 15/12/9.
 */
public class Utils {


    public static void saveFlowInfo(Context context,String param1,String param2){


        SharedPreferences preferences = context.getSharedPreferences("flow_user",Context.MODE_PRIVATE);

        SharedPreferences.Editor deit = preferences.edit();
        deit.putString("flow_month",param1);
        deit.putString("flow_used",param2);
        deit.commit();

    }

    public  static void saveFlowDate(Context context,String date){

        SharedPreferences preferences = context.getSharedPreferences("flow_user",Context.MODE_PRIVATE);

        SharedPreferences.Editor deit = preferences.edit();
        deit.putString("flow_date",date);
        deit.commit();
    }

    public static String getFlowDate(Context context){

        SharedPreferences preferences = context.getSharedPreferences("flow_user",Context.MODE_PRIVATE);
        return preferences.getString("flow_date","0");

    }

    public static String getFlowMonth(Context context){

        SharedPreferences preferences = context.getSharedPreferences("flow_user",Context.MODE_PRIVATE);
        return preferences.getString("flow_month","0");

    }

    public static String getFlowUsed(Context context){

        SharedPreferences preferences = context.getSharedPreferences("flow_user",Context.MODE_PRIVATE);
        return preferences.getString("flow_used","0");
    }

    public static String isWIfiConnect(Context context){
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            // we are not connected anywhere now
            return "0";
        }
        else if ((activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) ||
                (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIMAX)) {
            // that's wifi
            return "1";

        } else {
            // that's mobile
            return "2";
        }
    }

    public static String size2string(long size){
        DecimalFormat df = new DecimalFormat("0.00");
        String mysize = "";
        if( size > 1024*1024){
            mysize = df.format( size / 1024f / 1024f ) +"M";
        }else if( size > 1024 ){
            mysize = df.format( size / 1024f ) +"K";
        }else{
            mysize = size + "B";
        }
        return mysize;
    }
}
