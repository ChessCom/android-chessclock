package com.chess.clock.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;

import com.chess.clock.R;

public class ClockButton extends FrameLayout {

    private Button button;
    private TextView timeTv;
    private TextView movesTv;

    public ClockButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_clock_button, this, true);
        button = view.findViewById(R.id.clockButton);
        timeTv = view.findViewById(R.id.clockTimeTv);
        movesTv = view.findViewById(R.id.movesTv);
    }

    public void setTimeAndTextSize(String time, @DimenRes int textSizeRes) {
        timeTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(textSizeRes));
        timeTv.setText(time);
    }

    public void setTime(String time) {
        timeTv.setText(time);
    }

    @SuppressLint("DefaultLocale")
    public void setMoves(int moves) {
        movesTv.setText(String.format("%2d", moves));
    }

    public CharSequence getTimeText() {
        return timeTv.getText();
    }

    public void setClockButtonClickListener(OnClickListener listener) {
        button.setOnClickListener(listener);
    }

    public void updateUi(
            @DrawableRes int btnBgRes,
            @ColorRes int textColorRes
    ) {
        button.setBackgroundDrawable(getResources().getDrawable(btnBgRes));
        timeTv.setTextColor(getResources().getColor(textColorRes));

    }
}