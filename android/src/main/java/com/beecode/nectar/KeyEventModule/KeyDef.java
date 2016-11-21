package com.beecode.nectar.KeyEventModule;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by airyuxun on 2016/11/17.
 */

public class KeyDef {
    private int keyCode;
    private boolean needPropagate;
    private String name;

    public int getScanCode() {
        return scanCode;
    }

    public void setScanCode(int scanCode) {
        this.scanCode = scanCode;
    }

    private  int scanCode;
    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public boolean isNeedPropagate() {
        return needPropagate;
    }

    public void setNeedPropagate(boolean needPropagate) {
        this.needPropagate = needPropagate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        try{
            JSONObject o =new JSONObject();
            o.put("keyCode",keyCode);
            o.put("needPropagate",needPropagate);
            o.put("name",name);
            o.put("scanCode",scanCode);
        }catch (JSONException e){
           e.printStackTrace();
        }
        return super.toString();
    }
}
