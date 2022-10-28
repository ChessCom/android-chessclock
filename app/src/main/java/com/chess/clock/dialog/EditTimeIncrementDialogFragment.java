package com.chess.clock.dialog;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.fragment.app.Fragment;

import com.chess.clock.R;
import com.chess.clock.activities.BaseActivity;
import com.chess.clock.engine.TimeIncrement;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.entities.ClockTime;
import com.chess.clock.util.ClockUtils;

public class EditTimeIncrementDialogFragment extends FullScreenDialogFragment {

    public static final String TAG = "EditTimeIncrementDialogFragment";
    private static final String ARG_TIME_INCREMENT_KEY = "arg_time_increment_key";

    private TimeIncrement timeIncrement;

    EditText secondsEt;
    EditText minutesEt;

    AppCompatCheckedTextView delayTv;
    AppCompatCheckedTextView bronsteinTv;
    AppCompatCheckedTextView fischerTv;
    TextView typeDetailsTv;

    @Override
    int layoutRes() {
        return R.layout.dialog_fragment_edit_time_increment;
    }

    public static EditTimeIncrementDialogFragment newInstance(TimeIncrement timeIncrement) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_TIME_INCREMENT_KEY, timeIncrement);

        EditTimeIncrementDialogFragment fragment = new EditTimeIncrementDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState == null) {
            timeIncrement = requireArguments().getParcelable(ARG_TIME_INCREMENT_KEY);
        } else {
            timeIncrement = savedInstanceState.getParcelable(ARG_TIME_INCREMENT_KEY);
        }

        assert view != null;
        secondsEt = view.findViewById(R.id.secondsEt);
        minutesEt = view.findViewById(R.id.minutesEt);
        delayTv = view.findViewById(R.id.delayBtn);
        bronsteinTv = view.findViewById(R.id.bronsteinBtn);
        fischerTv = view.findViewById(R.id.fisherBtn);
        typeDetailsTv = view.findViewById(R.id.typeDetailsTv);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.titleTv)).setText(R.string.increment);
        view.findViewById(R.id.backBtn).setOnClickListener(v -> dismissAllowingStateLoss());

        ClockUtils.clearFocusOnActionDone(minutesEt);
        ClockUtils.setClockTextWatcher(secondsEt);

        if (savedInstanceState == null) {
            ClockTime clockTime = timeIncrement.getDuration();
            minutesEt.setText(ClockUtils.twoDecimalPlacesFormat(clockTime.totalMinutes()));
            secondsEt.setText(ClockUtils.twoDecimalPlacesFormat(clockTime.seconds));
        }

        delayTv.setOnClickListener(v -> setCheckedViews(TimeIncrement.Type.DELAY));
        bronsteinTv.setOnClickListener(v -> setCheckedViews(TimeIncrement.Type.BRONSTEIN));
        fischerTv.setOnClickListener(v -> setCheckedViews(TimeIncrement.Type.FISCHER));

        setCheckedViews(timeIncrement.getType());
    }

    private void setCheckedViews(TimeIncrement.Type type) {
        delayTv.setChecked(type == TimeIncrement.Type.DELAY);
        bronsteinTv.setChecked(type == TimeIncrement.Type.BRONSTEIN);
        fischerTv.setChecked(type == TimeIncrement.Type.FISCHER);

        int subtitleRes;
        switch (type) {
            case DELAY:
                subtitleRes = R.string.delay_option_subtitle;
                break;
            case BRONSTEIN:
                subtitleRes = R.string.bronstein_option_subtitle;
                break;
            default:
                subtitleRes = R.string.fischer_option_subtitle;
                break;
        }
        typeDetailsTv.setText(subtitleRes);

        timeIncrement.setType(type);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppTheme theme = ((BaseActivity) requireActivity()).selectedTheme;
        if (theme != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList tintFocused = theme.colorStateListFocused(requireContext());
                ColorStateList tintChecked = theme.colorStateListChecked(requireContext());
                minutesEt.setBackgroundTintList(tintFocused);
                secondsEt.setBackgroundTintList(tintFocused);
                delayTv.setCheckMarkTintList(tintChecked);
                bronsteinTv.setCheckMarkTintList(tintChecked);
                fischerTv.setCheckMarkTintList(tintChecked);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_TIME_INCREMENT_KEY, timeIncrement);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        int minutes = ClockUtils.getIntOrZero(minutesEt);
        int seconds = ClockUtils.getIntOrZero(secondsEt);
        long duration = minutes * 60 * 1000L + seconds * 1000L;

        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            ((OnTimeIncrementEditListener) parentFragment)
                    .onTimeIncrementEditDone(timeIncrement.getType(), duration);
        }
    }

    /**
     * The callback interface used to indicate the user is done filling in
     * the stage data (the user clicked on the 'Set' button).
     */
    public interface OnTimeIncrementEditListener {

        /**
         * @param type The increment Type that was set.
         * @param time The time that was set in milliseconds.
         */
        void onTimeIncrementEditDone(TimeIncrement.Type type, long time);
    }
}
