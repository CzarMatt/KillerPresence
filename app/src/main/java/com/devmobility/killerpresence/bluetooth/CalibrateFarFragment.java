package com.devmobility.killerpresence.bluetooth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.devmobility.killerpresence.R;
import com.devmobility.killerpresence.circleprogress.CircularProgressButton;
import com.devmobility.killerpresence.util.BluetoothRssiListener;
import com.devmobility.killerpresence.util.Constants;

import java.util.ArrayList;

public class CalibrateFarFragment extends Fragment implements BluetoothRssiListener {
    private static final String TAG = CalibrateNearFragment.class.getSimpleName();

    private BluetoothMgr mBluetoothMgr;

    private CircularProgressButton circularButton1;

    private ArrayList<Integer> rssiNearList = new ArrayList<>();
    private boolean mCollectData = false;
    private int mRssiValue = 0;

    public CalibrateFarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.calibrate_far, container, false);

        mBluetoothMgr = ((CalibrateActivity) getActivity()).getBluetoothMgr();
        if(!mBluetoothMgr.getInitialized()) {
            mBluetoothMgr.instantiate();
        }

        circularButton1 = (CircularProgressButton) view.findViewById(R.id.circularButton1);
        circularButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gatherData(circularButton1);
                circularButton1.setClickable(false);
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            mBluetoothMgr.setBluetoothRssiListener(this);
            ((CalibrateActivity) getActivity()).disableRightArrow();
        }
    }

    private void gatherData(final CircularProgressButton button) {
        mCollectData = true;
        ValueAnimator anim = ValueAnimator.ofInt(1, 100);
        anim.setDuration(9000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                button.setProgress(value);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCollectData = false;
                ((CalibrateActivity) getActivity()).enableRightArrow();
                // Store off the gathered data for later use
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.APP_PREFS, 0).edit();
                editor.putInt(Constants.PREFS_FAR_THRESHOLD, mRssiValue);
                editor.apply();
            }
        });
        anim.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        circularButton1.setProgress(0);
        circularButton1.setEnabled(true);
        if (mBluetoothMgr == null) {
            mBluetoothMgr = ((CalibrateActivity) getActivity()).getBluetoothMgr();
        }
        if(!mBluetoothMgr.getInitialized()) {
            mBluetoothMgr.instantiate();
        }
        mBluetoothMgr.setBluetoothRssiListener(this);
    }

    @Override
    public void onRssiSignalUpdated(final int rssi) {
        if (mCollectData) {
            mRssiValue += rssi;
        }
    }

    @Override
    public void onRssiSignalInterrupted() {
    }

}
