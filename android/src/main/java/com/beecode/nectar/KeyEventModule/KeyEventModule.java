package com.beecode.nectar.KeyEventModule;

import android.support.annotation.Nullable;
import android.view.KeyEvent;

import com.beecode.nectar.KeyEventAdapter;
import com.beecode.nectar.NectarActivity;
import com.beecode.nectar.NectarEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by airyuxun on 2016/11/17.
 */

public class KeyEventModule extends ReactContextBaseJavaModule {
    private NectarActivity activity;
    ReactApplicationContext reactContext;
    private List<KeyDef> keyList = new ArrayList<KeyDef>();
    private DefKeyEventAdapter adapter = new DefKeyEventAdapter();
    private NectarGlobalEventListener listener = new NectarGlobalEventListener();
    public KeyEventModule(ReactApplicationContext reactContext, NectarActivity activity) {
        super(reactContext);
        this.reactContext = reactContext;
        this.activity = activity;
        activity.addEventListener(listener);
    }

    @Override
    public String getName() {
        return "KeyEventModule";
    }
    class NectarGlobalEventListener implements NectarEventListener {

        @Override
        public String onEvent(String name,@Nullable WritableMap params) {

            if(reactContext!=null)reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(name, params);
            return null;
        }

        @Override
        public <T> T getAdapter(Class<T> clazz) {
            if(clazz == KeyEventAdapter.class){
                return (T) adapter;
            }
            return null;
        }
    }
    @ReactMethod
    public void keyEventDef(String keyEventDef, Promise promise) {
        try {
            putKeyEventDef(keyEventDef);
            promise.resolve("done");
        } catch (JSONException e) {
            promise.reject("keyDef deseralize faild",e.getMessage());
            e.printStackTrace();
        }
    }
    public boolean putKeyEventDef(String keyEventDef) throws JSONException {
        JSONArray array = new JSONArray(keyEventDef);
        keyList = new ArrayList<KeyDef>();
        for(int i=0;i<array.length();i++){
            JSONObject o =array.getJSONObject(i);
            KeyDef key = new KeyDef();
            key.setKeyCode(o.optInt("keyCode",-1));
            key.setName(o.optString("name","unkown"));
            key.setScanCode(o.optInt("scanCode",-1));
            key.setNeedPropagate(o.optBoolean("needPropagate",true));
            keyList.add(key);
        }
        return true;
    }
    class DefKeyEventAdapter implements  KeyEventAdapter{

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            KeyDef regDef = null;
            for(KeyDef keDef:keyList){
                if(keDef.getKeyCode() == keyCode){
                    regDef=keDef;
                    break;
                }

            }
            WritableMap eventMap = Arguments.createMap();
            eventMap.putString("name","keyDown");
            eventMap.putInt("keyCode",event.getKeyCode());
            eventMap.putInt("scanCode",event.getScanCode());
            eventMap.putInt("metaState",event.getMetaState());
            if(regDef == null){
                listener.onEvent("keyDown",eventMap);
                return false;
            }
            eventMap.putString("def",regDef.toString());
            listener.onEvent("keyDown",eventMap);
            return !regDef.isNeedPropagate();
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            KeyDef regDef = null;
            for(KeyDef keDef:keyList){
                if(keDef.getKeyCode() == keyCode ){
                    regDef=keDef;
                    break;
                }

            }
            WritableMap eventMap = Arguments.createMap();
            eventMap.putString("name","keyUp");
            eventMap.putInt("keyCode",event.getKeyCode());
            eventMap.putInt("scanCode",event.getScanCode());
            eventMap.putInt("metaState",event.getMetaState());

            if(regDef == null){
                listener.onEvent("keyUp",eventMap);
                return false;
            }
            eventMap.putString("def",regDef.toString());
            listener.onEvent("keyUp",eventMap);
            return !regDef.isNeedPropagate();
        }
    }

}
