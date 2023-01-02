package com.chess.clock.util;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class ClockUtils {
    @SuppressLint("DefaultLocale")
    public static String twoDecimalPlacesFormat(int value) {
        return String.format("%02d", value);
    }

    public static int getIntOrZero(EditText et) {
        int intValue = 0;
        String textValue = et.getText().toString();
        try {
            intValue = textValue.isEmpty() ? 0 : Integer.parseInt(textValue);
        } catch (NumberFormatException exception) {
            Log.i("ClockUtils", "Cannot parse value:" + textValue + " to Integer.");
        }
        return intValue;
    }

    public static void setClockTextWatcher(EditText editText) {
        setClockTextWatcherWithCallback(editText, () -> {
            // no-op
        });
    }

    public static void setClockTextWatcherWithCallback(EditText editText, OnTimeEditCallback callback) {
        TextWatcher minutesTextWatcher = new TextWatcher() {
            final int MAX = 59;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                editText.removeTextChangedListener(this);
                String minutesAsString = s.toString();
                int minutes = minutesAsString.isEmpty() ? 0 : Integer.parseInt(minutesAsString);
                if (minutes > MAX) {
                    s.clear();
                    s.append(twoDecimalPlacesFormat(MAX));
                }
                editText.addTextChangedListener(this);
                callback.onTimeChange();
            }
        };
        editText.addTextChangedListener(minutesTextWatcher);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                String minutesAsString = v.getText().toString();
                int minutes = minutesAsString.isEmpty() ? 0 : Integer.parseInt(minutesAsString);
                v.setText(twoDecimalPlacesFormat(minutes));
                v.clearFocus();
            }
            return false;
        });
    }

    public static void clearFocusOnActionDone(EditText editText) {
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus();
            }
            return false;
        });
    }

    public static void onTimeChangedAction(EditText editText, OnTimeEditCallback callback) {
        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                callback.onTimeChange();
            }
        };
        editText.addTextChangedListener(textWatcher);
    }


    public static long durationMillis(int hours, int minutes, int seconds) {
        return hours * 60 * 60 * 1000L + minutes * 60 * 1000L + seconds * 1000L;
    }

    public interface OnTimeEditCallback {
        void onTimeChange();
    }

}
