package com.stefano.andrea.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * AutoCompleteWithouPopupView
 */
public class AutoCompleteWithoutPopupView extends AutoCompleteTextView {

    private ProgressBar mLoadingIndicator;
    private ImageView mClearButton;

    public AutoCompleteWithoutPopupView(Context context) {
        super(context);
    }

    public AutoCompleteWithoutPopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompleteWithoutPopupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLoadingIndicator(ProgressBar view) {
        mLoadingIndicator = view;
    }

    public void setClearButton (ImageView clearButton) {
        mClearButton = clearButton;
    }

    @Override
    public void showDropDown() {
        if (mClearButton != null)
            mClearButton.setEnabled(true);
    }

    @Override
    public void dismissDropDown() {
        if (mClearButton != null)
            mClearButton.setEnabled(false);
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mLoadingIndicator != null)
            mLoadingIndicator.setVisibility(View.VISIBLE);
        super.performFiltering(text, keyCode);
    }

    @Override
    public void onFilterComplete(int count) {
        if (mLoadingIndicator != null)
            mLoadingIndicator.setVisibility(View.INVISIBLE);
        super.onFilterComplete(count);
    }
}
