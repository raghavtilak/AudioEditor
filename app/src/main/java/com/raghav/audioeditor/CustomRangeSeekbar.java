package com.raghav.audioeditor;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;

public class CustomRangeSeekbar extends CrystalRangeSeekbar {

    public CustomRangeSeekbar(Context context) {
        super(context);
    }

    public CustomRangeSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRangeSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected float getBarHeight() {
        float dip = 1000f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        return px;
    }
}