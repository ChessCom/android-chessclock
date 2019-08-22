package com.chess.clock.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.chess.clock.compoundviews.TimePickerView;

/**
 * A dialog that prompts the user for the time valueInMilliseconds.
 */
public class TimePickerDialog extends AlertDialog implements OnClickListener {

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECONDS = "seconds";
    protected TimePickerView mTimePickerView;
    int mInitialHour;
    int mInitialMinute;
    int mInitialSecond;
    private OnTimeSetListener mCallback;

    /**
     * @param context Parent.
     * @param theme   the theme to apply to this dialog
     */
    public TimePickerDialog(Context context, int theme) {
        super(context, theme);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public void setOnTimeSetListener(OnTimeSetListener listener) {
        mCallback = listener;
    }

    public void setInitialHour(int initialHour) {
        mInitialHour = initialHour;
        if (mTimePickerView != null) {
            mTimePickerView.setCurrentHour(initialHour);
        }
    }

    public void setInitialMinute(int initialMinute) {
        mInitialMinute = initialMinute;
        if (mTimePickerView != null) {
            mTimePickerView.setCurrentMinute(initialMinute);
        }
    }

    public void setInitialSecond(int initialSecond) {
        mInitialSecond = initialSecond;
        if (mTimePickerView != null) {
            mTimePickerView.setCurrentSecond(initialSecond);
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
            mTimePickerView.clearFocus();
            if (which == BUTTON_POSITIVE) {
                mCallback.onTimeSet(mTimePickerView.getCurrentHour(),
                        mTimePickerView.getCurrentMinute(), mTimePickerView.getCurrentSeconds());
            }
        }
    }

    @Override
    public void setView(View view) {
        super.setView(view);
        mTimePickerView = (TimePickerView) view;
    }

    /**
     * We save Dialog state as the user might have change the time picker values,
     * and do configuration change. The thing is that these values were not saved
     * by the wrapper Fragment (because the user didn't pressed 'Set' button yet),
     * thus their initial values, which are set again in configuration change are
     * deprecated, and we must restore the updated ones.
     */
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, mTimePickerView.getCurrentHour());
        state.putInt(MINUTE, mTimePickerView.getCurrentMinute());
        state.putInt(SECONDS, mTimePickerView.getCurrentSeconds());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        int seconds = savedInstanceState.getInt(SECONDS);
        mTimePickerView.setCurrentHour(hour);
        mTimePickerView.setCurrentMinute(minute);
        mTimePickerView.setCurrentSecond(seconds);
    }

    public static enum Type {

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
                default:
                    return HOUR_MINUTE_SECOND;
            }
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {

        /**
         * @param hour   The hour that was set.
         * @param minute The minute that was set.
         * @param second The second that was set.
         */
        void onTimeSet(int hour, int minute, int second);
    }

    public static class Builder {

        protected Context mContext;
        protected int mTheme;

        protected Type mType = Type.HOUR_MINUTE_SECOND;
        protected String mTitle;

        protected CharSequence mPositiveButtonText;
        protected CharSequence mNegativeButtonText;

        protected int mHour;
        protected int mMinute;
        protected int mSecond;

        private OnTimeSetListener mOnTimeSetListener;

        /**
         * CONSTRUCTOR
         */

        public Builder(Context context) {
            mContext = context;
            mTheme = 0;
        }

        /**
         * SETTERS
         */

        public Builder setType(Type type) {
            mType = type;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setPositiveButton(CharSequence text) {
            mPositiveButtonText = text;
            return this;
        }

        public Builder setNegativeButton(CharSequence text) {
            mNegativeButtonText = text;
            return this;
        }

        public Builder setHour(int hour) {
            mHour = hour;
            return this;
        }

        public Builder setMinute(int minute) {
            mMinute = minute;
            return this;
        }

        public Builder setSecond(int second) {
            mSecond = second;
            return this;
        }

        public Builder setOnTimeSetListener(OnTimeSetListener listener) {
            mOnTimeSetListener = listener;
            return this;
        }

        public TimePickerDialog create() {

            // Create timer compound view, using HOUR_MINUTE_SECOND type as default.
            TimePickerView tp = new TimePickerView(mContext, TimePickerView.Type.fromInteger(mType.getValue()));

            final TimePickerDialog dialog = new TimePickerDialog(mContext, mTheme);

            // setView() mandatory to be called first, as so the following are applied correctly.
            dialog.setView(tp);
            dialog.setInitialHour(mHour);
            dialog.setInitialMinute(mMinute);
            dialog.setInitialSecond(mSecond);
            dialog.setTitle(mTitle);
            dialog.setOnTimeSetListener(mOnTimeSetListener);
            dialog.setButton(BUTTON_POSITIVE, mPositiveButtonText, dialog);
            dialog.setButton(BUTTON_NEGATIVE, mNegativeButtonText, dialog);

            return dialog;
        }
    }
}