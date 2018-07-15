package com.example.admin.healthyslife_android.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.example.admin.healthyslife_android.R;


/**
 * @author wu jingji
 */
public class MapFragment extends Fragment {

    private TextView mTimeTextView;

    private Button musicButton;

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
        view.findViewById(R.id.btn_map_startRun).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if (button.getText().equals(getString(R.string.main_map_startExercise))) {
                    button.setText(R.string.main_map_stopExercise);
                    onExerciseStateChangeListener.onStart();
                } else {
                    button.setText(R.string.main_map_startExercise);
                    onExerciseStateChangeListener.onStop();
                }
            }
        });
        mTimeTextView = view.findViewById(R.id.tv_map_exerciseTime);
    }

    public void updateTimeText(long time) {
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        minutes = minutes % 60;
        seconds = seconds % 60;

        mTimeTextView.setText(getString(R.string.main_map_timeFormat, hours, minutes, seconds));
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
