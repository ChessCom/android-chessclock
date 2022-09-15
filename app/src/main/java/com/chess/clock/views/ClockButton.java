package com.chess.clock.views;

import static com.chess.clock.views.ViewUtils.showView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;

import com.chess.clock.R;
import com.chess.clock.entities.AppTheme;

public class ClockButton extends FrameLayout {

    private final TextView timeTv;
    private final TextView movesTv;
    private final View stageOne;
    private final View stageTwo;
    private final View stageThree;

    public ClockButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_clock_button, this, true);
        timeTv = view.findViewById(R.id.clockTimeTv);
        movesTv = view.findViewById(R.id.movesTv);
        stageOne = view.findViewById(R.id.stageOne);
        stageTwo = view.findViewById(R.id.stageTwo);
        stageThree = view.findViewById(R.id.stageThree);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(R.style.AppTheme, new int[]{R.attr.selectableItemBackground});
        int attributeResourceId = a.getResourceId(0, 0);
        Drawable drawable = ContextCompat.getDrawable(getContext(), attributeResourceId);
        setForeground(drawable);
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
        setOnClickListener(listener);
    }

    public void updateUi(
            AppTheme theme,
            State state
    ) {
        switch (state) {
            case IDLE:
            case LOCKED:
                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_light));
                timeTv.setTextColor(getResources().getColor(R.color.clock_timer_idle_textColor));
                break;
            case RUNNING:
                setBackgroundColor(ContextCompat.getColor(getContext(), theme.primaryColorRes));
                timeTv.setTextColor(getResources().getColor(R.color.white));
                break;
            case FINISHED:
                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
                timeTv.setTextColor(getResources().getColor(R.color.clock_timer_idle_textColor));
                break;
        }
        setClickable(state != State.LOCKED);
    }

    private void setStageBg(View stage, Boolean active) {
        if (active) {
            stage.setBackgroundResource(R.drawable.shape_stage_fill);
        } else {
            stage.setBackgroundResource(R.drawable.shape_stage_empty);
        }
    }

    public void updateStage(int stageId) {
        //stage one is always filled if visible
        setStageBg(stageTwo, stageId > 0);
        setStageBg(stageThree, stageId > 1);
    }

    public void setStages(int stagesNumber) {
        // no stages indicators visible for 1 stage game
        showView(stageOne, stagesNumber > 1);
        showView(stageTwo, stagesNumber > 1);
        showView(stageThree, stagesNumber > 2);
    }

    public enum State {
        IDLE, LOCKED, RUNNING, FINISHED;
    }
}