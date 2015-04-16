package com.devmobility.killerpresence.util;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class CustomAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {

    private ValueAnimator mValueAnimator = null;

    private static final int RED = 0xffDA6565;
    private static final int BLUE = 0xff6952BF;
    private static final int PURPLE = 0xff6447AB;
    private static final int TEAL = 0xff0A7676;
    private static final int GREEN = 0xff67894B;
    private static final int DURATION = 3000;
    private static final String PROPERTY_NAME = "backgroundColor";

    private PageChangeListener mPageChangeListener;

    private ViewPager.OnPageChangeListener mOnPageChangeListener;

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.mOnPageChangeListener = listener;
    }

    public void setmViewPager(ViewPager mViewPager, int count, int... colors) {
        if (mViewPager.getAdapter() == null) {
            throw new IllegalStateException("No adapter on viewpager!");
        }
        mPageChangeListener.setNumPages(count);

        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mValueAnimator = ObjectAnimator.ofInt(this, PROPERTY_NAME, BLUE, TEAL, GREEN, GREEN, PURPLE, RED, TEAL);
        mValueAnimator.setEvaluator(new ArgbEvaluator());
        mValueAnimator.setDuration(DURATION);
        mValueAnimator.addUpdateListener(this);

    }

    public CustomAnimationView(Context context) {
        this(context, null, 0);

    }

    public CustomAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPageChangeListener = new PageChangeListener();
    }

    private void seek(long seekTime) {
        if (mValueAnimator == null) {
            createDefaultAnimation();
        }
        mValueAnimator.setCurrentPlayTime(seekTime);
    }

    private void createDefaultAnimation() {
        mValueAnimator = ObjectAnimator.ofInt(this, PROPERTY_NAME, BLUE, TEAL, GREEN, GREEN, PURPLE, RED, TEAL);
        mValueAnimator.setEvaluator(new ArgbEvaluator());
        mValueAnimator.setDuration(DURATION);
        mValueAnimator.addUpdateListener(this);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        private int pages;

        public int getNumPages() {
            return pages;
        }

        public void setNumPages(int pages) {
            this.pages = pages;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            int count = getNumPages() - 1;
            if (count != 0) {
                float length = (position + positionOffset) / count;
                int progress = (int) (length * DURATION);
                seek(progress);
            }

            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

        }

        @Override
        public void onPageSelected(int position) {
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    }
}


