package com.chess.clock.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.chess.clock.compoundviews.TimeIncrementEditorView;
import com.chess.clock.compoundviews.TimePickerView;
import com.chess.clock.engine.TimeIncrement;

/**
 * A dialog that prompts the user to edit Stage values. This dialog extends TimePickerDialog as it
 * only adds a Stage moves field on top of the time picker, from TimePickerDialog.
 */
public class TimeIncrementEditorDialog extends TimePickerDialog {

    private static final String INCREMENT_TYPE = "increment_type";
    private EditTimeIncrementDialogFragment.OnTimeIncrementEditListener mCallback;
    private TimeIncrementEditorView mTimeIncrementEditorView;
    private int mInitialTimeIncrementType;

    /**
     * @param context Parent context.
     * @param theme   Theme to apply to this dialog.
     */
    public TimeIncrementEditorDialog(Context context, int theme) {
        super(context, theme);
    }

    public void setOnStageEditListener(EditTimeIncrementDialogFragment.OnTimeIncrementEditListener listener) {
        mCallback = listener;
    }

    public void setInitialType(int type) {
        mInitialTimeIncrementType = type;
        if (mTimeIncrementEditorView != null) {
            mTimeIncrementEditorView.setCurrentTimeIncrementType(mInitialTimeIncrementType);
        }
    }

    @Override
    public void setView(View view) {
        super.setView(view);
        mTimeIncrementEditorView = (TimeIncrementEditorView) view;
    }

    /**
     * We save Dialog state as the user might have change the fields values, and do
     * configuration change. The thing is that these values were not saved by the
     * wrapper Fragment (because the user didn't pressed 'Set' button yet), thus
     * their initial values, which are set again in configuration change are
     * deprecated, and we must restore the updated ones.
     */
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(INCREMENT_TYPE, mTimeIncrementEditorView.getCurrentIncrementType());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int moves = savedInstanceState.getInt(INCREMENT_TYPE);
        mTimeIncrementEditorView.setCurrentTimeIncrementType(moves);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
            if (which == BUTTON_POSITIVE) {

                TimeIncrement.Type type = TimeIncrement.Type.fromInteger(mTimeIncrementEditorView.getCurrentIncrementType());
                int hour = mTimeIncrementEditorView.getCurrentHour();
                int minute = mTimeIncrementEditorView.getCurrentMinute();
                int second = mTimeIncrementEditorView.getCurrentSeconds();

                long newDuration = (hour * 60 * 60 * 1000) + (second * 1000) + (minute * 60 * 1000);
                mCallback.onTimeIncrementEditDone(type, newDuration);
            }
        }
    }



    public static class Builder extends TimePickerDialog.Builder {

        private int mType;
        private EditTimeIncrementDialogFragment.OnTimeIncrementEditListener mOnTimeIncrementEditListener;

        public Builder(Context context) {
            super(context);
        }

        /**
         * SETTERS
         */
        public Builder setTimeIncrementType(int type) {
            mType = type;
            return this;
        }

        public Builder setOnTimeIncrementEditListener(EditTimeIncrementDialogFragment.OnTimeIncrementEditListener listener) {
            mOnTimeIncrementEditListener = listener;
            return this;
        }

        @Override
        public TimeIncrementEditorDialog create() {

            // Create Stage Editor compound view, using HOUR_MINUTE_SECOND type as default.
            TimeIncrementEditorView stageEditorView = new TimeIncrementEditorView(mContext,
                    TimePickerView.Type.MINUTE_SECOND);

            final TimeIncrementEditorDialog dialog = new TimeIncrementEditorDialog(mContext, mTheme);

            // setView() mandatory to be called first, as so the following are applied correctly.
            dialog.setView(stageEditorView);
            dialog.setInitialType(mType);
            dialog.setInitialHour(mHour);
            dialog.setInitialMinute(mMinute);
            dialog.setInitialSecond(mSecond);
            dialog.setTitle(mTitle);
            dialog.setButton(BUTTON_POSITIVE, mPositiveButtonText, dialog);
            dialog.setButton(BUTTON_NEGATIVE, mNegativeButtonText, dialog);
            dialog.setOnStageEditListener(mOnTimeIncrementEditListener);

            return dialog;
        }
    }
}
