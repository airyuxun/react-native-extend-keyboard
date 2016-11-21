package com.beecode.nectar;

import android.view.KeyEvent;

/**
 * Created by airyuxun on 2016/11/17.
 */

public interface KeyEventAdapter {
    boolean onKeyDown(int keyCode, KeyEvent event) ;

    boolean onKeyUp(int keyCode, KeyEvent event);
}
