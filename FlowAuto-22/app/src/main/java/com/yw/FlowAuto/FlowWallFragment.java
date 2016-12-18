package com.yw.FlowAuto;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.jtschohl.androidfirewall.Api;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by yw on 15/12/3.
 */
public class FlowWallFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {


    @ViewInject(R.id.listview)
    ListView listView;

    /** indicates if the view has been modified and not yet saved */
    private boolean dirty = false;

    /**
     * variables for profile names
     */
    private String[] profileposition;

    public static final String TAG = "Wall";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_flow_wall,container,false);
        ViewUtils.inject(this, view);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String mode =  Api.MODE_BLACKLIST;
        final SharedPreferences.Editor editor = getActivity().getSharedPreferences(
                Api.PREFS_NAME, 0).edit();
        editor.putString(Api.PREF_MODE, mode);
        editor.commit();
//        disableOrEnable();

    }

    @Override
    public void onResume() {
        super.onResume();

        // No password lock
        showOrLoadApplications();
    }

    /**
     * If the applications are cached, just show them, otherwise load and show
     */
    public void showOrLoadApplications() {
        final Resources res = getResources();
        final String search = "";
        if (Api.applications == null) {
            // The applications are not cached.. so lets display the progress
            // dialog
//            final ProgressDialog progress = ProgressDialog.show(getActivity(),
//                    res.getString(R.string.working),
//                    res.getString(R.string.reading_apps), true);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Api.getApps(getActivity());
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    try {
//                        progress.dismiss();
                    } catch (Exception ex) {
                        Log.d("{AF} - error in showorloadapplications",
                                ex.getMessage());
                    }
                    createListView(search);
                }
            }.execute();
        } else {
            // the applications are cached, just show the list
            createListView(search);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {


        final Api.DroidApp app = (Api.DroidApp) compoundButton.getTag();
        if (app != null) {
            switch (compoundButton.getId()) {
                case R.id.itemcheck_wifi:
                    if (app.selected_wifi != b) {
                        app.selected_wifi = b;
                        this.dirty = true;
                        if(dirty){

                            applyOrSaveRules();
                        }else{

                            purgeRules();
                        }
                    }
                    break;
                case R.id.itemcheck_3g:
                    if (app.selected_3g != b) {
                        app.selected_3g = b;
                        this.dirty = true;
                    }
                    break;

            }
        }
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * sort apps when searched
     */

    class sortList implements Comparator<Api.DroidApp> {

        @Override
        public int compare(Api.DroidApp o1, Api.DroidApp o2) {
            if (o1.firstseen != o2.firstseen) {
                return (o1.firstseen ? -1 : 1);
            }
            boolean o1_selected = o1.selected_3g || o1.selected_wifi
                    || o1.selected_roaming || o1.selected_vpn
                    || o1.selected_lan || o1.selected_input_wifi;
            boolean o2_selected = o2.selected_3g || o2.selected_wifi
                    || o2.selected_roaming || o2.selected_vpn
                    || o2.selected_lan || o2.selected_input_wifi;

            if (o1_selected == o2_selected) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.names.get(0)
                        .toString(), o2.names.get(0).toString());
            }
            if (o1_selected)
                return -1;
            return 1;
        }
    }
    /**
     * Show the list of applications
     *
     * Thanks to Ukanth for the Search code so I didn't have to reinvent the
     * wheel
     *
     */
    private void createListView(final String searching) {
        this.dirty = false;
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

        boolean isResultsFound = false;
        if (searching != null && searching.length() > 1) {
            for (Api.DroidApp app : appnames) {
                for (String str : app.names) {
                    if (str.contains(searching.toLowerCase())
                            || str.toLowerCase().contains(
                            searching.toLowerCase())) {
                        namesearch.add(app);
                        isResultsFound = true;
                    }
                }
            }
        }
        final List<Api.DroidApp> apps = isResultsFound ? namesearch : searching
                .equals("") ? appnames : new ArrayList<Api.DroidApp>();
        // Sort applications - selected first, then alphabetically
        Collections.sort(apps, new sortList());

        final int defaultColor = Color.BLACK;
        final int color = Color.RED;

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final ListAdapter adapter = new ArrayAdapter<Api.DroidApp>(getActivity(),
                R.layout.listitem, R.id.itemtext, apps) {
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
                    convertView = inflater.inflate(R.layout.listitem, parent,
                            false);
                    Log.d(TAG, ">> inflate(" + convertView + ")");
                    entry = new ListEntry();
                    entry.box_wifi = (CheckBox) convertView
                            .findViewById(R.id.itemcheck_wifi);
                    entry.box_3g = (CheckBox) convertView
                            .findViewById(R.id.itemcheck_3g);


                    entry.text = (TextView) convertView
                            .findViewById(R.id.itemtext);
                    entry.icon = (ImageView) convertView
                            .findViewById(R.id.itemicon);
                    entry.box_wifi
                            .setOnCheckedChangeListener(FlowWallFragment.this);
                    entry.box_3g.setOnCheckedChangeListener(FlowWallFragment.this);

                    convertView.setTag(entry);
                } else {
                    // Convert an existing view
                    entry = (ListEntry) convertView.getTag();
                    entry.box_wifi = (CheckBox) convertView
                            .findViewById(R.id.itemcheck_wifi);
                    entry.box_3g = (CheckBox) convertView
                            .findViewById(R.id.itemcheck_3g);

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
                final CheckBox box_wifi = entry.box_wifi;
                box_wifi.setTag(entry.app);
                box_wifi.setChecked(entry.app.selected_wifi);
                final CheckBox box_3g = entry.box_3g;
                box_3g.setTag(entry.app);
                box_3g.setChecked(entry.app.selected_3g);

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
        private CheckBox box_wifi;
        private CheckBox box_3g;
        private TextView text;
        private ImageView icon;
        private Api.DroidApp app;
    }


    /**
     * Enables or disables the firewall
     */
    private void disableOrEnable() {
        final boolean enabled = !Api.isEnabled(getActivity());
        Log.d(TAG, "Changing enabled status to: " + enabled);
        Api.setEnabled(getActivity(), enabled);
        if (enabled) {
            applyOrSaveRules();
        } else {
            purgeRules();
        }

    }

    /**
     * Apply or save iptable rules, showing a visual indication
     */
    private void applyOrSaveRules() {
        final Resources res = getResources();
        final boolean enabled = Api.isEnabled(getActivity());
        final ProgressDialog progress = ProgressDialog.show(getActivity(), res
                .getString(R.string.working), res
                .getString(enabled ? R.string.applying_rules
                        : R.string.saving_rules), true);
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getActivity().getApplicationContext());
                int i;
                try {
                    progress.dismiss();
                } catch (Exception ex) {
                }
                if (enabled) {
                    Log.d(TAG, "Applying rules.");
                    if (Api.hasRootAccess(getActivity(), true)
                            && Api.applyIptablesRules(getActivity(), true)) {
                        Toast.makeText(getActivity(),
                                R.string.rules_applied, Toast.LENGTH_SHORT)
                                .show();

                        i = prefs.getInt("itemPosition", 0);
                        if (i == 0) {
                            saveDefaultProfile();
                        }

                    } else {
                        Log.d(TAG, "Failed - Disabling firewall.");
                        Api.setEnabled(getActivity(), false);

                    }

                }

                else {
                    Log.d(TAG, "Saving rules.");
                    Api.saveRules(getActivity());
                    Toast.makeText(getActivity(), R.string.rules_saved,
                            Toast.LENGTH_SHORT).show();
                    i = prefs.getInt("itemPosition", 0);
                    if (i == 0) {
                        saveDefaultProfile();
                    }

                }
                dirty = false;
            }
        };
        handler.sendEmptyMessageDelayed(0, 100);
    }

    private void saveDefaultProfile() {
        SharedPreferences prefs2 = getActivity().getSharedPreferences(Api.PREFS_NAME,
                Context.MODE_PRIVATE);
        final SharedPreferences prefs = getActivity().getSharedPreferences(Api.PREF_PROFILE,
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor editRules = prefs.edit();
        editRules.clear();
        for (Map.Entry<String, ?> entry : prefs2.getAll().entrySet()) {
            Object rule = entry.getValue();
            String keys = entry.getKey();
            if (rule instanceof Boolean)
                editRules.putBoolean(keys, ((Boolean) rule).booleanValue());
            else if (rule instanceof Float)
                editRules.putFloat(keys, ((Float) rule).floatValue());
            else if (rule instanceof String)
                editRules.putString(keys, ((String) rule));
            else if (rule instanceof Long)
                editRules.putLong(keys, ((Long) rule).longValue());
            else if (rule instanceof Integer)
                editRules.putInt(keys, ((Integer) rule).intValue());
        }
        editRules.commit();
    }

    /**
     * Purge iptable rules, showing a visual indication
     */
    private void purgeRules() {
        final Resources res = getResources();
        final ProgressDialog progress = ProgressDialog.show(getActivity(),
                res.getString(R.string.working),
                res.getString(R.string.deleting_rules), true);
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    progress.dismiss();
                } catch (Exception ex) {
                }
                if (!Api.hasRootAccess(getActivity(), true))
                    return;
                if (Api.purgeIptables(getActivity(), true)) {
                    Toast.makeText(getActivity(), R.string.rules_deleted,
                            Toast.LENGTH_SHORT).show();

                }
            }
        };
        handler.sendEmptyMessageDelayed(0, 100);
    }

}
