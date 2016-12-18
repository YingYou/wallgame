package com.yw.FlowAuto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.yw.FlowAuto.view.SharePageAdapter;

import java.util.ArrayList;

/**
 * Created by yw on 15/12/3.
 */
public class FlowDetailActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener,View.OnClickListener{

    public SharePageAdapter adapter;

    /**pagerview */
    @ViewInject(R.id.pager)
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_details);
        ViewUtils.inject(this);
        initFlowDetailActionBar(FlowDetailActivity.this,FlowDetailActivity.this);

        ArrayList<Fragment> list =new ArrayList<Fragment>();
//		list.add(mNewTrackQueryFragment);
        list.add(new FlowCountFragment());
        list.add(new FlowWallFragment());
        adapter = new SharePageAdapter(this.getSupportFragmentManager(),this);
        pager.setOffscreenPageLimit(0);
        pager.setAdapter(adapter);

        adapter.addFragment(list);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                Log.d("TAG", "onPageScrolled--i" + i);
                setSegmentedShare(i);

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.button_share_left:
                        Log.d("TAG", "button_share_left");
                        pager.setCurrentItem(0);

                        break;

                    case R.id.button_share_right:
                        Log.d("TAG", "button_share_right");
                        pager.setCurrentItem(1);
                        break;

                }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.back_flow:
                finish();
                break;
        }
    }
}
