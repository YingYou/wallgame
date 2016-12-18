package com.yw.FlowAuto;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bigkoo.svprogresshud.SVProgressHUD;
import com.jtschohl.androidfirewall.Api;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.yw.FlowAuto.util.MySQLiteOpenHelper;
import com.yw.FlowAuto.util.TrafficInfo;
import com.yw.FlowAuto.view.CustomAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by yw on 15/12/3.
 */
public class FlowCountFragment extends Fragment{

    @ViewInject(R.id.listview_count)
    ListView listView;

    MySQLiteOpenHelper dbHelper;
    ArrayList<TrafficInfo> appList = new ArrayList<TrafficInfo>();
    CustomAdapter browseAppAdapter;

    public static final String TAG = "FlowCountFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragement_flow_count,container,false);
        ViewUtils.inject(this,view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        browseAppAdapter = new CustomAdapter(
                getActivity(), appList);
        listView.setAdapter(browseAppAdapter);
        SVProgressHUD.showWithProgress(getActivity(),"加载中", SVProgressHUD.SVProgressHUDMaskType.Black);
        mThreadLoadApps.start();
    }

    @Override
    public void onResume() {
        super.onResume();
//        showOrLoadApplications();

    }



    private Thread mThreadLoadApps = new Thread(){
        @Override
        public void run() {

            dbHelper = new MySQLiteOpenHelper(getActivity());
            appList = dbHelper.queryByTime(getActivity(),"today");

            hander.sendEmptyMessage(0); //

        }
    };
    private Handler hander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    SVProgressHUD.dismiss(getActivity());
                    browseAppAdapter.notifyDataSetChanged(); //
//                    listView.setAdapter(browseAppAdapter); //
                    browseAppAdapter.setData(appList);
                    break;
                default:
                    //do something
                    break;
            }
        }
    };

    /**
     * If the applications are cached, just show them, otherwise load and show
     */
    public void showOrLoadApplications() {
        final Resources res = getResources();
        final String search = "";
        if (Api.applications == null) {
            // The applications are not cached.. so lets display the progress
            // dialog
            final ProgressDialog progress = ProgressDialog.show(getActivity(),
                    res.getString(R.string.working),
                    res.getString(R.string.reading_apps), true);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Api.getApps(getActivity());
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    try {
                        progress.dismiss();
                    } catch (Exception ex) {
                        Log.d("{AF} - error in showorloadapplications",
                                ex.getMessage());
                    }
                    createListView();
                }
            }.execute();
        } else {
            // the applications are cached, just show the list
            createListView();
        }
    }


    /**
     * Show the list of applications
     *
     * Thanks to Ukanth for the Search code so I didn't have to reinvent the
     * wheel
     *
     */
    private void createListView() {

        List<Api.DroidApp> namesearch = new ArrayList<Api.DroidApp>();
        List<Api.DroidApp> appnames = Api.getApps(getActivity());
        List<Api.DroidApp> tempname = new ArrayList<Api.DroidApp>();

        for (Api.DroidApp app : appnames) {
            //非系统程序
            if (app!=null && app.appinfo != null && (app.appinfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                tempname.add(app);
            }
            //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
            else if (app!=null && app.appinfo != null && (app.appinfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
                tempname.add(app);
            }
        }
        appnames = tempname;

        final int defaultColor = Color.BLACK;
        final int color = Color.RED;

        final List<Api.DroidApp> apps = appnames;


        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final ListAdapter adapter = new ArrayAdapter<Api.DroidApp>(getActivity(),
                R.layout.listitem_flow_recode, R.id.itemtext, apps) {
            SharedPreferences prefs = getActivity().getSharedPreferences(Api.PREFS_NAME, 0);
            boolean vpnenabled = prefs.getBoolean(Api.PREF_VPNENABLED, false);
            boolean roamenabled = prefs.getBoolean(Api.PREF_ROAMENABLED, false);
            boolean lanenabled = prefs.getBoolean(Api.PREF_LANENABLED, false);
            boolean inputwifienabled = prefs.getBoolean(Api.PREF_INPUTENABLED,
                    false);
            boolean colorenabled = prefs.getBoolean(Api.PREF_APPCOLOR, false);

            @Override
            public View getView(final int position, View convertView,
                                ViewGroup parent) {
                ListEntry entry;
                if (convertView == null) {
                    // Inflate a new view
                    convertView = inflater.inflate(R.layout.listitem_flow_recode, parent,
                            false);
                    Log.d(TAG, ">> inflate(" + convertView + ")");
                    entry = new ListEntry();

                    entry.text = (TextView) convertView
                            .findViewById(R.id.itemtext);
                    entry.icon = (ImageView) convertView
                            .findViewById(R.id.itemicon);
                    entry.count = (TextView)convertView.findViewById(R.id.count_wifi_flow);
                    entry.mobile_count = (TextView)convertView.findViewById(R.id.count_3g_flow);

                   convertView.setTag(entry);
                } else {
                    // Convert an existing view
                    entry = (ListEntry) convertView.getTag();

                }
                entry.app = apps.get(position);
                entry.text.setText(entry.app.toString());

                ApplicationInfo app = entry.app.appinfo;

                if (app != null
                        && (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    entry.text.setTextColor(defaultColor);
                } else {
                    entry.text.setTextColor(color);
                }


                entry.icon.setImageDrawable(entry.app.cached_icon);
                if (!entry.app.icon_loaded && entry.app.appinfo != null) {
                    // this icon has not been loaded yet - load it on a
                    // separated thread
                    try {
                        new LoadIconTask().execute(entry.app,
                                getActivity().getPackageManager(), convertView);
                    } catch (RejectedExecutionException r) {

                    }
                }


                return convertView;
            }
        };
        if (listView == null) {
            Api.applications = null;
            showOrLoadApplications();
        } else {
            this.listView.setAdapter(adapter);
        }

    }


    /**
     * Asynchronous task used to load icons in a background thread.
     */
    private static class LoadIconTask extends AsyncTask<Object, Void, View> {
        @Override
        protected View doInBackground(Object... params) {
            try {
                final Api.DroidApp app = (Api.DroidApp) params[0];
                final PackageManager pkgMgr = (PackageManager) params[1];
                final View viewToUpdate = (View) params[2];
                if (!app.icon_loaded) {
                    app.cached_icon = pkgMgr.getApplicationIcon(app.appinfo);
                    app.icon_loaded = true;
                }
                // Return the view to update at "onPostExecute"
                // Note that we cannot be sure that this view still references
                // "app"
                return viewToUpdate;
            } catch (Exception e) {
                Log.e(TAG, "Error loading icon", e);
                return null;
            }
        }

        protected void onPostExecute(View viewToUpdate) {
            try {
                // This is executed in the UI thread, so it is safe to use
                // viewToUpdate.getTag()
                // and modify the UI
                final ListEntry entryToUpdate = (ListEntry) viewToUpdate
                        .getTag();
                entryToUpdate.icon
                        .setImageDrawable(entryToUpdate.app.cached_icon);
            } catch (Exception e) {
                Log.e(TAG, "Error showing icon", e);
            }
        };
    }



    /**
     * Entry representing an application in the screen
     */
    private static class ListEntry {
        private TextView text;
        private ImageView icon;
        private TextView count;
        private TextView mobile_count;
        private Api.DroidApp app;
    }


}
