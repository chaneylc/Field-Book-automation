package com.fieldbook.tracker.traits;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class BarcodeTraitLayout extends BaseTraitLayout {

    public BarcodeTraitLayout(Context context) {
        super(context);
    }

    public BarcodeTraitLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarcodeTraitLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setNaTraitsText() {
    }

    @Override
    public String type() {
        return "barcode";
    }

    @Override
    public void init() {

    }

    @Override
    public void loadLayout() {
        super.toggleVisibility(View.VISIBLE);
    }

    @Override
    public void deleteTraitListener() {

    }
}