package com.stefano.andrea.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * CustomFAB
 */
public class CustomFAB extends FloatingActionButton {

    private boolean mForceHide = false;
    private boolean isMovedUp = false;
    private float movedHeight;
    private final Interpolator mInterpolator;

    public CustomFAB(Context context) {
        super(context);
        this.mInterpolator = new AccelerateDecelerateInterpolator();
    }

    public CustomFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInterpolator = new AccelerateDecelerateInterpolator();
    }

    public CustomFAB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mInterpolator = new AccelerateDecelerateInterpolator();
    }

    public void setForceHide (boolean forceHide) {
        mForceHide = forceHide;
    }

    @Override
    public void show(boolean animate) {
        if (!mForceHide) {
            super.show(animate);
            if (isMovedUp) {
                moveUp(movedHeight);
            }
        }
    }

    @Override
    public void hide(boolean animate) {
        if (!mForceHide)
            super.hide(animate);
    }

    public void moveUp (float height) {
        isMovedUp = true;
        movedHeight = height;
        ViewPropertyAnimator.animate(this).setInterpolator(mInterpolator).translationY(-height);
    }

    public void moveDown (float height) {
        isMovedUp = false;
        float translation = height - (getHeight() / 2) - getMarginBottom();
        ViewPropertyAnimator.animate(this).setInterpolator(mInterpolator).translationY(translation);
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams)layoutParams).bottomMargin;
        }

        return marginBottom;
    }
}
