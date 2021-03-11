package com.chess.clock.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.chess.clock.compoundviews.StageEditorView;
import com.chess.clock.compoundviews.TimePickerView;

/**
 * A dialog that prompts the user to edit Stage values. This dialog extends TimePickerDialog as it
 * only adds a Stage moves field on top of the time picker, from TimePickerDialog.
 */
public class StageEditorDialog extends TimePickerDialog {

    private static final String MOVES = "moves";
    private OnStageEditListener mCallback;
    private StageEditorView mStageEditorView;
    private int mInitialMoves;

    /**
     * @param context Parent context.
     * @param theme   the theme to apply to this dialog
     */
    public StageEditorDialog(Context context, int theme) {
        super(context, theme);
    }

    public void setOnStageEditListener(OnStageEditListener listener) {
        mCallback = listener;
    }

    public void setInitialMoves(int initialMoves) {
        mInitialMoves = initialMoves;
        if (mStageEditorView != null) {
            mStageEditorView.setCurrentMoves(mInitialMoves);
        }
    }

    @Override
    public void setView(View view) {
        super.setView(view);
        mStageEditorView = (StageEditorView) view;
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
        state.putInt(MOVES, mStageEditorView.getCurrentMoves());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int moves = savedInstanceState.getInt(MOVES);
        mStageEditorView.setCurrentMoves(moves);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
            if (which == BUTTON_POSITIVE) {

                int moves = mStageEditorView.getCurrentMoves();
                int hour = mStageEditorView.getCurrentHour();
                int minute = mStageEditorView.getCurrentMinute();
                int second = mStageEditorView.getCurrentSeconds();

                long newDuration = (hour * 60 * 60 * 1000) + (second * 1000) + (minute * 60 * 1000);
                mCallback.onStageEditDone(moves, newDuration);
            }
        }
    }

    /**
     * The callback interface used to indicate the user is done filling in
     * the stage data (the user clicked on the 'Set' button).
     */
    public interface OnStageEditListener {

        /**
         * @param moves The number of moves that was set.
         * @param time  The time that was set in milliseconds.
         */
        void onStageEditDone(int moves, long time);
    }

    public static class Builder extends TimePickerDialog.Builder {

        private int mMoves;
        private boolean mMovesVisible;
        private OnStageEditListener mOnStageEditListener;

        public Builder(Context context) {
            super(context);
        }

        /**
         * SETTERS
         */

        public Builder setMovesVisible(boolean visible) {
            mMovesVisible = visible;
            return this;
        }

        public Builder setMoves(int moves) {
            mMoves = moves;
            return this;
        }

        public Builder setOnStageEditListener(OnStageEditListener listener) {
            mOnStageEditListener = listener;
            return this;
        }

        @Override
        public StageEditorDialog create() {

            // Create Stage Editor compound view, using HOUR_MINUTE_SECOND type as default.
            StageEditorView stageEditorView = new StageEditorView(mContext,
                    TimePickerView.Type.HOUR_MINUTE_SECOND, mMovesVisible);

            final StageEditorDialog dialog = new StageEditorDialog(mContext, mTheme);

            // setView() mandatory to be called first, as so the following are applied correctly.
            dialog.setView(stageEditorView);
            dialog.setInitialMoves(mMoves);
            dialog.setInitialHour(mHour);
            dialog.setInitialMinute(mMinute);
            dialog.setInitialSecond(mSecond);
            dialog.setTitle(mTitle);
            dialog.setButton(BUTTON_POSITIVE, mPositiveButtonText, dialog);
            dialog.setButton(BUTTON_NEGATIVE, mNegativeButtonText, dialog);
            dialog.setOnStageEditListener(mOnStageEditListener);

            return dialog;
        }
    }
}
