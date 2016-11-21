package com.beecode.nectar;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.WritableMap;

/**
 * Created by airyuxun on 2016/10/26.
 */

public interface NectarEventListener {
    public String onEvent(String name,@Nullable WritableMap params);
    public <T> T getAdapter(Class<T> clazz);
}
