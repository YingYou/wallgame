package com.yw.FlowAuto.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.yw.FlowAuto.R;
import com.yw.FlowAuto.util.TrafficInfo;
import com.yw.FlowAuto.util.Utils;

import java.text.DecimalFormat;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    
    private List<TrafficInfo> mlistAppInfo = null;
      
    LayoutInflater infater = null;
    
    public static final long B = 1;
    public static final long KB = B * 1024;
    public static final long MB = KB * 1024;
    public static final long GB = MB * 1024;
    
    public CustomAdapter(Context context, List<TrafficInfo> apps) {
        infater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        mlistAppInfo = apps ;  
    }

    public void setData(List<TrafficInfo> apps){

        mlistAppInfo = apps;
        notifyDataSetChanged();;
    }

    @Override  
    public int getCount() {  
        // TODO Auto-generated method stub  
        System.out.println("size" + mlistAppInfo.size());  
        return mlistAppInfo.size();  
    }  
    @Override  
    public Object getItem(int position) {  
        // TODO Auto-generated method stub  
        return mlistAppInfo.get(position);  
    }  
    @Override  
    public long getItemId(int position) {  
        // TODO Auto-generated method stub  
        return 0;  
    }  
    @Override  
    public View getView(int position, View convertview, ViewGroup arg2) {
        View view = null;  
        ViewHolder holder = null;  
        if (convertview == null || convertview.getTag() == null) {  
            view = infater.inflate(R.layout.listitem_flow_recode, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }   
        else{  
            view = convertview ;  
            holder = (ViewHolder) convertview.getTag() ;  
        }  
        TrafficInfo appInfo = (TrafficInfo) getItem(position);  
        holder.appIcon.setImageDrawable(appInfo.getAppicon());  
        holder.Label.setText(appInfo.getAppName());
		holder.gprs.setText(Utils.size2string(appInfo.getGPRS()));
        holder.wlan.setText(Utils.size2string(appInfo.getWIFI()));
        return view;  
    }  
  
    class ViewHolder {  
        ImageView appIcon;  
        TextView Label;  
        TextView gprs;
        TextView wlan;
  
        public ViewHolder(View view) {  
            this.appIcon = (ImageView) view.findViewById(R.id.itemicon);
            this.Label = (TextView) view.findViewById(R.id.itemtext);
            this.gprs = (TextView) view.findViewById(R.id.count_3g_flow);
            this.wlan = (TextView) view.findViewById(R.id.count_wifi_flow);
            
        }  
    }
    

}  
