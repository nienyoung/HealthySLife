package com.example.admin.healthyslife_android.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.admin.healthyslife_android.R;

import java.util.Locale;

/**
 * @author wu jingji
 */
public class HealthyFragment extends Fragment {

    public static final String TAG = "HealthyFragment";

    private TextView mStepsTextView;
    private TextView mStepFrequencyTextView;
    private TextView mSpeedTextView;
    private TextView mCalorieTextView;

    public HealthyFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HealthyFragment.
     */
    public static HealthyFragment newInstance() {
        HealthyFragment fragment = new HealthyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_healthy, container, false);

        FragmentManager fm = getFragmentManager();
        if (fm == null) {
            Log.i(TAG, "onCreateView: cannot get fragment manager");
        } else {
            fm.beginTransaction().add(R.id.healthy_heartRateContainer, HeartRateMonitorFragment.newInstance()).commit();
        }
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStepsTextView = view.findViewById(R.id.tv_healthy_stepCounter);
        mStepFrequencyTextView = view.findViewById(R.id.tv_healthy_stepFrequency);
        mSpeedTextView = view.findViewById(R.id.tv_healthy_speed);
        mCalorieTextView = view.findViewById(R.id.tv_healthy_calorie_data);
    }

    public void updateStepCounterText(int steps) {
        mStepsTextView.setText(String.valueOf(steps));
    }

    public void updateStepFrequencyText(double frequency) {
        mStepFrequencyTextView.setText(getString(R.string.main_healthy_stepFrequencyFormat, frequency));
    }

    public void updateSpeedText(double speed) {
        mSpeedTextView.setText(getString(R.string.main_healthy_avgSpeedFormat, speed));
    }

    public void updateCalorie(float calorie) {
        mCalorieTextView.setText(String.format(Locale.CHINA, "%.2f", calorie));
    }
}
