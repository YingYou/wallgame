package com.yw.FlowAuto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by yw on 15/12/8.
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener{

    @ViewInject(R.id.flow_setting_two)
    RelativeLayout flow_setting_two;

    @ViewInject(R.id.flow_setting_one)
    RelativeLayout flow_setting_one;

    @ViewInject(R.id.flow_setting_three)
    RelativeLayout flow_setting_three;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_settings);
        ViewUtils.inject(this);

        initSettingsActionBar(this,"设置");
        flow_setting_one.setOnClickListener(this);
        flow_setting_two.setOnClickListener(this);
        flow_setting_three.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){

            case R.id.back_flow:

                finish();
                break;

            case R.id.flow_setting_one:

                Intent intent = new Intent();
                intent.setClass(SettingsActivity.this,LinkFLowActivity.class);
                this.startActivity(intent);
                break;

            case R.id.flow_setting_two:

                AlertDialog dialog = null;
                dialog = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("声明")
                        .setMessage(R.string.warningNoteString).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                }
            }).create();
                dialog.show();
                break;
            case R.id.flow_setting_three:

                AlertDialog dialog1 = null;
                dialog1 = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("声明")
                        .setMessage(R.string.tip_message).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        }).create();
                dialog1.show();
                break;
        }

    }
}
