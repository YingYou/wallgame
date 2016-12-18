package com.yw.FlowAuto;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.Stack;

/**
 * Created by yw on 15/12/2.
 */
public class FlowApplication extends Application{

    public Stack<Activity> activityStack;

    /**
     * Shows a toast message
     *
     * @param context Any context belonging to this application
     * @param message The message to show
     */
    public static void toast(Context context, String message) {
        // this is a static method so it is easier to call,
        // as the context checking and casting is done for you

        if (context == null) return;

        if (!(context instanceof FlowApplication)) {
            context = context.getApplicationContext();
        }

        if (context instanceof FlowApplication) {
            final Context c = context;
            final String m = message;

            ((FlowApplication)context).runInApplicationThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(c, m, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private static Handler mApplicationHandler = new Handler();

    /**
     * Run a runnable in the main application thread
     *
     * @param r Runnable to run
     */
    public void runInApplicationThread(Runnable r) {
        mApplicationHandler.post(r);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            // workaround bug in AsyncTask, can show up (for example) when you toast from a service
            // this makes sure AsyncTask's internal handler is created from the right (main) thread
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
        }
    }

    /**
     * add Activity ���Activity��ջ
     */
    public void addActivity(Activity activity){
        if(activityStack ==null){
            activityStack =new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * get current Activity ��ȡ��ǰActivity��ջ�����һ��ѹ��ģ�
     */
    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * ����ǰActivity��ջ�����һ��ѹ��ģ�
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * ����ָ����Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * ��ȡactivityջ�ĳ���
     */
    public int getActivitySize(){
        return activityStack.size();
    }

    /**
     * ����ָ�������Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * ��������Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    /**
     * �˳�Ӧ�ó���
     */
    public void exit() {
        try {
            finishAllActivity();
        } catch (Exception e) {
        }
    }
    /**
     * �˳�Ӧ�ó���
     */
    public void exitNotContainMain() {
        try {

            for (int i = 0, size = activityStack.size(); i < size; i++) {
                if (null != activityStack.get(i)) {
                    if(!activityStack.get(i).getClass().getName().contains("MainActivity")){
                        activityStack.get(i).finish();
                    }
                }
            }
            activityStack.clear();
        } catch (Exception e) {
        }
    }

}
