package com.devmobility.killerpresence.bluetooth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devmobility.killerpresence.R;
import com.devmobility.killerpresence.circleprogress.CircularProgressButton;

public class CompleteFragment extends Fragment {

    public CompleteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.complete_frag, container, false);

        CircularProgressButton circularButton1 = (CircularProgressButton) view.findViewById(R.id.circularButton1);
        circularButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO store off data collected and set Prefs
                ((CalibrateActivity)getActivity()).kill();
                getActivity().finish();
            }
        });

        return view;
    }

}