package com.chess.clock.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

import com.chess.clock.R;
import com.chess.clock.dialog.TimePickerDialog;

public class TimePickerDialogFragment extends DialogFragment {

    /**
     * Save Instance keys
     */
    protected static final String KEY_TYPE = "key_type";
    protected int mTypeValue;
    protected int mHour;
    protected int mMinute;
    protected int mSecond;
    protected Context mContext;

    // Mandatory
    public TimePickerDialogFragment() {

    }

    /**
     * Dialog Fragment constructor for a custom TimePicker compound view.
     *
     * @param context Dialog Fragment Context.
     * @param type    Type of TimePicker used.
     * @param hour    Initial hour value.
     * @param minute  Initial minute value.
     * @param second  Initial second value.
     */
    public TimePickerDialogFragment(Context context, Type type, int hour, int minute, int second) {
        mContext = context;
        mTypeValue = type.getValue();
        mHour = hour;
        mMinute = minute;
        mSecond = second;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mContext = getActivity();
            mTypeValue = savedInstanceState.getInt(KEY_TYPE);
        }

        return new TimePickerDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.time_picker_dialog_title))
                .setPositiveButton(mContext.getString(R.string.time_picker_dialog_set))
                .setNegativeButton(mContext.getString(R.string.time_picker_dialog_cancel))
                .setHour(mHour)
                .setMinute(mMinute)
                .setSecond(mSecond)
                .setType(TimePickerDialog.Type.fromInteger(mTypeValue))
                .setOnTimeSetListener((TimePickerDialog.OnTimeSetListener) getTargetFragment())
                .create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_TYPE, mTypeValue);
        super.onSaveInstanceState(outState);
    }

    public enum Type {
        MINUTE_SECOND(0),
        HOUR_MINUTE_SECOND(1);

        private final int value;

        private Type(int value) {
            this.value = value;
        }

        public static Type fromInteger(int type) {
            switch (type) {
                case 0:
                    return MINUTE_SECOND;
                case 1:
                    return HOUR_MINUTE_SECOND;
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }
}