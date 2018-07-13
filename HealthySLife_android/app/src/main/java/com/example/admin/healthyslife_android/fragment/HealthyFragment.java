package com.example.admin.healthyslife_android.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.admin.healthyslife_android.R;

/**
 * @author wu jingji
 */
public class HealthyFragment extends Fragment {

    private TextView mStepsTextView;

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
        return inflater.inflate(R.layout.fragment_healthy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStepsTextView = view.findViewById(R.id.tv_healthy_stepCounter);
    }

    public void updateStepCounterText(int steps) {
        mStepsTextView.setText(String.valueOf(steps));
    }
}
