package com.stefano.andrea.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.melnykov.fab.FloatingActionButton;

/**
 * CustomFAB
 */
public class CustomFAB extends FloatingActionButton {

    private boolean mForceHide = false;

    public CustomFAB(Context context) {
        super(context);
    }

    public CustomFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFAB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setForceHide (boolean forceHide) {
        mForceHide = forceHide;
    }

    @Override
    public void show(boolean animate) {
        if (!mForceHide)
            super.show(animate);
    }

    @Override
    public void hide(boolean animate) {
        if (!mForceHide)
            super.hide(animate);
    }
}
