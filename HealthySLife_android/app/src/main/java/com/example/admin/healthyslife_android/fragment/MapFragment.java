package com.example.admin.healthyslife_android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.example.admin.healthyslife_android.R;
import com.example.admin.healthyslife_android.music.MusicActivity;


/**
 * @author wu jingji
 */
public class MapFragment extends Fragment {

    public static final int STATE_STOP = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_START = 2;

    private int mState;
    private Button mStartButton;
    private Button mStopButton;

    private TextView mStepsTextView;
    private TextView mStepFrequencyTextView;
    private TextView mSpeedTextView;

    private OnExerciseStateChangeListener onExerciseStateChangeListener;

    private void setOnExerciseStateChangeListener(OnExerciseStateChangeListener onExerciseStateChangeListener) {
        this.onExerciseStateChangeListener = onExerciseStateChangeListener;
    }

    public MapFragment() {}

    public static MapFragment newInstance(OnExerciseStateChangeListener onExerciseStateChangeListener) {
        MapFragment mapFragment = new MapFragment();
        mapFragment.setOnExerciseStateChangeListener(onExerciseStateChangeListener);
        return mapFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mState = STATE_STOP;
        final Animation fadeInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        final Animation fadeOutAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        mStopButton = view.findViewById(R.id.btn_map_stopRun);
        mStopButton.setVisibility(View.GONE);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mState == STATE_START) {
                    mStartButton.setText(R.string.main_map_startExercise);
                    onExerciseStateChangeListener.onStop();
                    mState = STATE_STOP;
                    mStopButton.startAnimation(fadeOutAnim);
                    mStopButton.setVisibility(View.GONE);
                }
            }
        });
        mStartButton = view.findViewById(R.id.btn_map_startRun);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if (mState == STATE_STOP) {
                    mState = STATE_START;
                    button.setText(getTimeText(0));
                    onExerciseStateChangeListener.onStart();
                    mStopButton.setVisibility(View.VISIBLE);
                    mStopButton.startAnimation(fadeInAnim);
                }
            }
        });
        view.findViewById(R.id.btn_map_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MusicActivity.class);
                startActivity(intent);
            }
        });
        mStepsTextView = view.findViewById(R.id.tv_map_stepCounter);
        mStepFrequencyTextView = view.findViewById(R.id.tv_map_stepFrequency);
        mSpeedTextView = view.findViewById(R.id.tv_map_speed);
    }

    public void updateTimeText(long time) {
        mStartButton.setText(getTimeText(time));
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

    private String getTimeText(long time) {
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        minutes = minutes % 60;
        seconds = seconds % 60;
        return getString(R.string.main_map_timeFormat, hours, minutes, seconds);
    }

    public interface OnExerciseStateChangeListener {
        /**
         * It will be execute after user clicks the start button
         */
        void onStart();

        /**
         * It will be execute after user clicks the stop button
         */
        void onStop();
    }
}
