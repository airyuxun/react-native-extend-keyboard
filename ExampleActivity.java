package com.beecode.nectar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.beecode.nectar.eventAdapter.KeyEventAdapter;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.JavaOnlyMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by airyuxun on 16/3/16.
 */
public class NectarActivity extends ReactActivity{

    NectarAppInfo info;
    private NectarPackage nectarPackage;
    private NectarApplication application;
    private long exitTime = 0;
    private static final String PREFS_DEBUG_SERVER_HOST_KEY = "debug_http_host";
    private SharedPreferences mPreferences;
    private List<NectarEventListener> listeners = new ArrayList<NectarEventListener>();
    public void addEventListener(NectarEventListener listener){
        listeners.add(listener);
    }
    public void removeEventListener(NectarEventListener listener){
        listeners.remove(listener);
    }
    public void removeEventListener(int listener){
        listeners.remove(listener);
    }

    public List<NectarEventListener> getListeners() {
        return listeners;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {


        for(NectarEventListener listener:listeners){

            if(listener!=null){
                KeyEventAdapter keyEventAdapter = listener.getAdapter(KeyEventAdapter.class);
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if(keyEventAdapter == null){
                        WritableMap eventMap = Arguments.createMap();
                        eventMap.putString("name","kewDown");
                        eventMap.putInt("keyCode",event.getKeyCode());
                        eventMap.putInt("scanCode",event.getScanCode());
                        eventMap.putInt("metaState",event.getMetaState());
                        listener.onEvent("keyDown",eventMap);
                    }else{
                        if(keyEventAdapter.onKeyUp(event.getKeyCode(), event)){
                            return true;
                        }else{
                            return super.dispatchKeyEvent(event);
                        }
                    }
                }else if(event.getAction() == KeyEvent.ACTION_UP){

                    if(keyEventAdapter == null){
                        JavaOnlyMap eventMap = new JavaOnlyMap();
                        eventMap.putString("name","keyUp");
                        eventMap.putInt("scanCode",event.getScanCode());
                        eventMap.putInt("metaState",event.getMetaState());
                        eventMap.putInt("keyCode",event.getKeyCode());
                        listener.onEvent("keyUp",eventMap);
                    }else {
                         if(keyEventAdapter.onKeyUp(event.getKeyCode(), event)){
                            return true;
                         }else{
                             return super.dispatchKeyEvent(event);
                         }
                    }

                }

            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {


        return super.onKeyUp(keyCode, event);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        if(NectarAppInfo.ACTION_NECTAR_BACK_APP.equals(intent.getAction())) {
            if(!info.module.equals("MDP")) {
                onBackPressed();
            }
        } else if(NectarAppInfo.ACTION_NECTAR_OPEN_HOTAPP.equals(intent.getAction())) {
            try {
                String strInfo = intent.getStringExtra(NectarAppInfo.PARAM_NAME);
                final NectarAppInfo appInfo = NectarAppInfo.fromJson(new JSONObject(strInfo));
                UiThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openApp(appInfo);
                    }
                });
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        }

    };

    protected void openApp(NectarAppInfo appInfo) {
        loadApp(appInfo, "forward");
    }

    protected  void loadApp(NectarAppInfo appInfo, String direction) {

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREFS_DEBUG_SERVER_HOST_KEY, getHostOfApp(appInfo.root));
        editor.commit();

        Intent i =new Intent(this,NectarActivity.class);
        getBundleFile(appInfo, i);
        i.putExtra("mainComponentName", appInfo.module);
        i.putExtra("BundleAssetName", appInfo.entry);

        String webRoot = getWebRoot(appInfo.web, appInfo.root);
        Bundle launchConfig =  new Bundle();
        if(appInfo.module.equals("MDP") && direction.equals("back")) {
            launchConfig.putString("WebRoot", webRoot);
            launchConfig.putBoolean("isBacktoMDP", true);
        } else {
            launchConfig.putString("WebRoot", webRoot);
            launchConfig.putBoolean("isBacktoMDP", false);
        }
        List<ResolveInfo> packageInfos = getPackageManager().queryIntentActivities(i, 0);
        for(int j=0; j< packageInfos.size(); j++) {
            String packageName = packageInfos.get(j).activityInfo.packageName;
            launchConfig.putString("packageName", packageName);
        }
        i.putExtra("launchOptions",launchConfig);

        try {
            i.putExtra("NectarAppInfo", NectarAppInfo.toJson(appInfo).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        startActivityForResult(i, 1);
    }

    protected String getBundleFile(NectarAppInfo info,Intent intent) {
        String bundleFile;
        String jsMain="index.android";
        if(info.root.startsWith(NectarView.PRE_HTTPS)) {
            bundleFile = info.root + info.entry;
            jsMain = "";
            for(int i=3;i<info.root.split("/").length;i++) {
                jsMain += info.root.split("/")[i] + "/";
            }
            jsMain += info.entry.endsWith(NectarView.EXT_BUNDLE)? info.entry.substring(0, info.entry.length() - NectarView.EXT_BUNDLE.length()) : info.entry;
        } else if(info.root.startsWith(NectarView.PRE_ASSETS)) {
            bundleFile = NectarView.PRE_REACT_ASSETS + info.root.substring(NectarView.PRE_ASSETS.length()) + info.entry;
        } else if(info.root.startsWith(NectarView.PRE_FILE)) {
            bundleFile = info.root.substring(NectarView.PRE_FILE.length()) + info.entry;
        } else {
            bundleFile = info.root + info.entry;
        }
        intent.putExtra("JSBundleFile", bundleFile);
        intent.putExtra("JSMainModuleName", jsMain);
        return  bundleFile;
    }
    public static final String PATH_WEB_ASSETS="file:///android_asset/";
    public static String getWebRoot(String web, String root) {
        if(web==null) return root;
        if(web.startsWith(PATH_WEB_ASSETS)) {
            return "asset:///" + web.substring(PATH_WEB_ASSETS.length());
        }
        if(web.indexOf("://")!=-1) return web;
        return root + web;
    }

    protected String getHostOfApp(String root) {
        if(root.startsWith(NectarView.PRE_HTTPS)) {
            return root.split("/")[2];
        }
        String mk = "://";
        int start = root.indexOf(mk);
        if(start==-1) {
            start=0;
        } else {
            start = start + mk.length();
        }
        int end = root.length();
        if(root.endsWith("/")) {
            end = end - 1;
        }
        if(end<=start) {
            root = "";
        } else {
            root = root.substring(start, end);
        }
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1) {
            this.setResult(1);
            this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NectarAppInfo.ACTION_NECTAR_OPEN_HOTAPP);
        intentFilter.addAction(NectarAppInfo.ACTION_NECTAR_BACK_APP);
        this.registerReceiver(receiver, intentFilter);

        application = ((NectarApplication)this.getApplication());
        if(application.hasInstance()) {
            application.clear();
        }

        Map<String, Object> config = new HashMap<>();
        config.put("WebRoot", getIntent().getBundleExtra("launchOptions").getString("WebRoot"));
        nectarPackage = new NectarPackage(this,config);
        ((NectarApplication)this.getApplication()).setNectarPackage(nectarPackage);
        ((NectarApplication)this.getApplication()).setIntent(getIntent());
        String appInfoString = getIntent().getStringExtra("NectarAppInfo");

        try {
            info=NectarAppInfo.fromJson(new JSONObject(appInfoString));
            if(info.module.equals("MDP")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    protected Bundle getLaunchOptions() {
        return getIntent().getBundleExtra("launchOptions");
    }

    @Override
    protected String getMainComponentName() {
        return getIntent().getStringExtra("mainComponentName");
    }

    @Override
    public void onBackPressed() {
        if(info.module.equals("MDP")) {
            if((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出MDP", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                clear();
                this.setResult(0);
                this.finish();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        this.unregisterReceiver(this.receiver);
        super.onDestroy();
    }

    public void clear() {
        ReactRootView mReactRootView = getReactRootView();
        if (mReactRootView != null) {
            mReactRootView.unmountReactApplication();
            mReactRootView = null;
        }
        getReactNativeHost().clear();
    }

}
