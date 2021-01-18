package com.example.scarlet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;

public class MyViewPager extends HorizontalInfiniteCycleViewPager {

    private boolean enabled;

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
