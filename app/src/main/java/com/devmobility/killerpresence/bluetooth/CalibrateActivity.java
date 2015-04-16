package com.devmobility.killerpresence.bluetooth;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.devmobility.killerpresence.R;
import com.devmobility.killerpresence.util.CustomAnimationView;

public class CalibrateActivity extends FragmentActivity {

    private static final String TAG = CalibrateActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private BluetoothMgr mBluetoothMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        if (mBluetoothMgr == null) {
            mBluetoothMgr = new BluetoothMgr(this);
            mBluetoothMgr.instantiate();
        }

        MyFragmentStatePager adapter = new MyFragmentStatePager(getSupportFragmentManager());
        CustomAnimationView customAnimationView = (CustomAnimationView) findViewById(R.id.color_animation_view);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(1);
        //viewPager.setClipToPadding(false);
        //viewPager.setPageMargin(12);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        final ImageButton imageButtonL = (ImageButton) findViewById(R.id.arrow_left_thin);
        final ImageButton imageButtonR = (ImageButton) findViewById(R.id.arrow_right_thin);

        customAnimationView.setmViewPager(mViewPager, 5);
        customAnimationView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.e("TAG", "onPageScrolled");
            }

            @Override
            public void onPageSelected(int position) {
                Log.i("TAG", "onPageSelected");
                if (position == 0) {
                    imageButtonL.setVisibility(View.GONE);
                } else {
                    imageButtonL.setVisibility(View.VISIBLE);
                }
                if (position == 4) {
                    imageButtonR.setVisibility(View.GONE);
                } else {
                    imageButtonR.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Log.e("TAG", "onPageScrollStateChanged");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothMgr == null) {
            mBluetoothMgr = new BluetoothMgr(this);
            mBluetoothMgr.instantiate();
        }
    }

    @Override
    protected void onStop() {
        if (mBluetoothMgr != null) {
            mBluetoothMgr.destroy();
            mBluetoothMgr = null;
        }
        super.onStop();
    }

    protected void kill() {
        if (mBluetoothMgr != null) {
            mBluetoothMgr.destroy();
            mBluetoothMgr = null;
        }
    }

    protected BluetoothMgr getBluetoothMgr() {
        return mBluetoothMgr;
    }

    public void navigatePager(View view) {
        switch (view.getId()) {
            case R.id.arrow_left_thin:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem()-1);
                break;
            case R.id.arrow_right_thin:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
                break;
        }
    }

    protected void next() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
    }

    protected void previous() {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
    }

    public void enableRightArrow() {
        final ImageButton b = (ImageButton) findViewById(R.id.arrow_right_thin);
        b.setEnabled(true);
        b.setAlpha(1.0f);
    }

    public void enableLeftArrow() {
        final ImageButton b = (ImageButton) findViewById(R.id.arrow_left_thin);
        b.setEnabled(true);
        b.setAlpha(1.0f);
    }

    public void disableRightArrow() {
        final ImageButton b = (ImageButton) findViewById(R.id.arrow_right_thin);
        b.setEnabled(false);
        b.setAlpha(0.3f);
    }

    public void disableLeftArrow() {
        final ImageButton b = (ImageButton) findViewById(R.id.arrow_left_thin);
        b.setEnabled(false);
        b.setAlpha(0.3f);
    }

    public class MyFragmentStatePager extends FragmentStatePagerAdapter {

        public MyFragmentStatePager(FragmentManager fm) {
            super(fm);
        }

        private int PAGES = 5;

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new WelcomeFragment();
                case 1:
                    return new DeviceListFragment();
                case 2:
                    return new CalibrateNearFragment();
                case 3:
                    return new CalibrateFarFragment();
                case 4:
                    return new CompleteFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return PAGES;
        }
    }

}
