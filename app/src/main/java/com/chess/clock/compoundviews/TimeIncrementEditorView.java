package com.chess.clock.compoundviews;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.chess.clock.R;
import com.chess.clock.engine.TimeIncrement;

public class TimeIncrementEditorView extends TimePickerView {

    /**
     * state
     */
    private int mTimeIncrementTypePosition = 0;
    private Context mContext;

    /**
     * UI components
     */
    private TextView mTimeIncrementSubtitleText;
    private RadioGroup mRadioGroup;
    private RadioButton mDelayBtn;
    private RadioButton mBronsteinBtn;
    private RadioButton mFischerBtn;

    /**
     * Constructors
     */
    public TimeIncrementEditorView(Context context, Type type) {
        this(context, type, null);
    }

    public TimeIncrementEditorView(Context context, Type type, AttributeSet attrs) {
        this(context, type, attrs, 0);
    }

    public TimeIncrementEditorView(Context context, Type type, AttributeSet attrs, int defStyle) {
        super(context, type, attrs, defStyle, R.layout.widget_time_increment_editor);
        mContext = context;
        init();
    }

    /**
     * Save used widgets internal references
     */
    private void init() {

        mRadioGroup = findViewById(R.id.radio_time_increment_type);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button was clicked
                switch (checkedId) {
                    case R.id.radio_delay:
                        mTimeIncrementTypePosition = 0;
                        break;
                    case R.id.radio_bronstein:
                        mTimeIncrementTypePosition = 1;
                        break;
                    case R.id.radio_fischer:
                        mTimeIncrementTypePosition = 2;
                        break;
                }
                updateSubtitle();
            }
        });

        mDelayBtn = findViewById(R.id.radio_delay);
        mBronsteinBtn = findViewById(R.id.radio_bronstein);
        mFischerBtn = findViewById(R.id.radio_fischer);

        // Setup subtitle text
        mTimeIncrementSubtitleText = findViewById(R.id.time_increment_type_subtitle);

        // Remove subtitle text view from UI on hdpi and landscapes
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(metrics);
            switch (metrics.densityDpi) {
                case DisplayMetrics.DENSITY_HIGH:
                case DisplayMetrics.DENSITY_MEDIUM:
                case DisplayMetrics.DENSITY_LOW:
                    findViewById(R.id.time_increment_type_subtitle).setVisibility(GONE);
                    break;
            }
        }
    }

    /**
     * @return current increment type
     */
    public int getCurrentIncrementType() {
        return mTimeIncrementTypePosition;
    }

    public void setCurrentTimeIncrementType(int type) {
        mTimeIncrementTypePosition = type;
        switch (type) {
            case 0:
                mDelayBtn.setChecked(true);
                break;
            case 1:
                mBronsteinBtn.setChecked(true);
                break;
            case 2:
                mFischerBtn.setChecked(true);
                break;
        }
        updateSubtitle();
    }

    /**
     * Update subtitle text of time increment type
     */
    private void updateSubtitle() {

        if (mTimeIncrementSubtitleText != null) {
            // Set spinner subtitle text
            String subtitle = "";
            switch (TimeIncrement.Type.fromInteger(mTimeIncrementTypePosition)) {
                case DELAY:
                    subtitle = getResources().getString(R.string.delay_option_subtitle);
                    break;
                case BRONSTEIN:
                    subtitle = getResources().getString(R.string.bronstein_option_subtitle);
                    break;
                case FISCHER:
                    subtitle = getResources().getString(R.string.fischer_option_subtitle);
                    break;
            }
            mTimeIncrementSubtitleText.setText(subtitle);
        }
    }
}
