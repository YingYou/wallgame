package com.yw.FlowAuto;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.lidroid.xutils.ViewUtils;
import com.yw.FlowAuto.view.PerformanceImageView;
import com.yw.FlowAuto.view.SegmentedGroup;

import java.io.DataOutputStream;

/**
 * 
 * @author yangwei
 * @date
 */
public class BaseActivity extends FragmentActivity {


    private ActionBar actionBar = null;
    RadioButton button_share_left;
    RadioButton button_share_right;
    private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        FlowApplication application = (FlowApplication) getApplication();
		 application.addActivity(this);
//		 CommonUtils.hideSoftWindowPan(this);
        actionBar  = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);
        //当前应用的代码执行目录
        upgradeRootPermission(getPackageCodePath());
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }
	
	@Override
	public void onBackPressed() {
//		CommonUtils.hideSoftWindowPan(this);
		finish();
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}

    public void initHomeActionBar(View.OnClickListener clickListener){

//        getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
        actionBar.setTitle("");

        actionBar.setCustomView(R.layout.custom_action_bar);
        actionBar.setDisplayShowHomeEnabled(false);
        PerformanceImageView imageView = (PerformanceImageView) actionBar.getCustomView().findViewById(R.id.detail_iv);
        imageView.setClickable(true);
        imageView.setOnClickListener(clickListener);

        PerformanceImageView settingsImageView = (PerformanceImageView)actionBar.getCustomView().findViewById(R.id.setting_iv);
        settingsImageView.setClickable(true);
        settingsImageView.setOnClickListener(clickListener);

    }

    public void initSettingsActionBar(View.OnClickListener clickListener,String titleStr){

        actionBar.setTitle("");
        actionBar.setCustomView(R.layout.custom_settings_action_bar);
        actionBar.setDisplayShowHomeEnabled(false);
        PerformanceImageView imageView = (PerformanceImageView) actionBar.getCustomView().findViewById(R.id.back_flow);
        TextView titleTv = (TextView)actionBar.getCustomView().findViewById(R.id.setting_flow_tv);
        titleTv.setText(titleStr);
        imageView.setClickable(true);
        imageView.setOnClickListener(clickListener);

    }

    public void initFlowDetailActionBar(RadioGroup.OnCheckedChangeListener listener,View.OnClickListener clickListener){

        actionBar.setTitle("");

        actionBar.setCustomView(R.layout.custom_action_bar_detail);
        actionBar.setDisplayShowHomeEnabled(false);
        button_share_left = (RadioButton)actionBar.getCustomView().findViewById(R.id.button_share_left);
        button_share_right = (RadioButton)actionBar.getCustomView().findViewById(R.id.button_share_right);
        SegmentedGroup segmented_share = (SegmentedGroup)actionBar.getCustomView().findViewById(R.id.segmented_detail);
        button_share_left.setChecked(true);
        PerformanceImageView performanceImageView = (PerformanceImageView)actionBar.getCustomView().findViewById(R.id.back_flow);
        performanceImageView.setClickable(true);
        performanceImageView.setOnClickListener(clickListener);
        segmented_share.setOnCheckedChangeListener(listener);

    }

    public void setSegmentedShare(int selectId){
        if (selectId == 0){
            button_share_left.setChecked(true);
            button_share_right.setChecked(false);
        }else{
            button_share_left.setChecked(false);
            button_share_right.setChecked(true);
        }

    }
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


}
