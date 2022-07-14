package com.chess.clock.compoundviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import com.chess.clock.R;

public class TimePickerView extends FrameLayout {

    private static final NumberPicker.Formatter TWO_DIGIT_FORMATTER = value -> String.format("%02d", value);
    protected Type mType;

    /**
     * state
     */
    protected int mCurrentHour = 0; // 0-23

    protected int mCurrentMinute = 0; // 0-59

    protected int mCurrentSecond = 0; // 0-59

    /**
     * ui components
     */
    private NumberPicker mHourPicker;
    private NumberPicker mMinutePicker;
    private NumberPicker mSecondPicker;

    public TimePickerView(Context context, Type type) {
        this(context, type, null);
    }

    public TimePickerView(Context context, Type type, AttributeSet attrs) {
        this(context, type, attrs, 0);
    }

    public TimePickerView(Context context, Type type, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mType = type;

        // Check xml attrs if inflated from xml resource
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.TimePickerCompoundView, defStyle, 0);
            int typeInt = t.getInt(R.styleable.TimePickerCompoundView_type, 0);
            mType = Type.fromInteger(typeInt);

            mCurrentHour = t.getInt(R.styleable.TimePickerCompoundView_hour, 0);
            mCurrentMinute = t.getInt(R.styleable.TimePickerCompoundView_minute, 0);
            mCurrentSecond = t.getInt(R.styleable.TimePickerCompoundView_second, 0);

            t.recycle();
        }

        //Inflate time picker compound view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_time_picker, this, true);


        // hour visibility dependent on picker type
        setupHourPicker(mType == Type.HOUR_MINUTE_SECOND);

        // digits of minute always visible
        setupMinutePicker(true);

        // digits of seconds always visible
        setupSecondPicker(true);

        setCurrentHour(mCurrentHour);
        setCurrentMinute(mCurrentMinute);
        setCurrentSecond(mCurrentSecond);
    }

    /**
     * Constructor ready for custom subclasses which have different layout
     */
    public TimePickerView(Context context, Type type, AttributeSet attrs, int defStyle, int layoutId) {
        super(context, attrs, defStyle);
        mType = type;

        // Check xml attrs if inflated from xml resource
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.TimePickerCompoundView, defStyle, 0);
            int typeInt = t.getInt(R.styleable.TimePickerCompoundView_type, 0);
            mType = Type.fromInteger(typeInt);

            mCurrentHour = t.getInt(R.styleable.TimePickerCompoundView_hour, 0);
            mCurrentMinute = t.getInt(R.styleable.TimePickerCompoundView_minute, 0);
            mCurrentSecond = t.getInt(R.styleable.TimePickerCompoundView_second, 0);

            t.recycle();
        }

        //Inflate time picker compound view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutId, this, true);

        // hour visibility dependent on picker type
        setupHourPicker(mType == Type.HOUR_MINUTE_SECOND);

        // digits of minute always visible
        setupMinutePicker(true);

        // digits of seconds always visible
        setupSecondPicker(true);

        setCurrentHour(mCurrentHour);
        setCurrentMinute(mCurrentMinute);
        setCurrentSecond(mCurrentSecond);
    }

    /**
     * @return The current hour (0-23).
     */
    public Integer getCurrentHour() {
        return mCurrentHour;
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        if (mType != null && mType == Type.HOUR_MINUTE_SECOND) {
            this.mCurrentHour = currentHour;

            updateHourPickerDisplay();
        }
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mCurrentMinute;
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinute = currentMinute;

        updateMinutePickerDisplay();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentSeconds() {
        return mCurrentSecond;
    }

    /**
     * Set the current second (0-59).
     */
    public void setCurrentSecond(Integer currentSecond) {
        this.mCurrentSecond = currentSecond;

        updateSecondsPickerDisplay();

    }

    @Override
    public int getBaseline() {
        return mHourPicker.getBaseline();
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateHourPickerDisplay() {
        if (mType != null && mType == Type.HOUR_MINUTE_SECOND) {
            int currentHour = mCurrentHour;
            mHourPicker.setValue(currentHour);
        }
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateMinutePickerDisplay() {
        mMinutePicker.setValue(mCurrentMinute);
    }
    /**
     * Set the state of the spinners appropriate to the current second.
     */
    private void updateSecondsPickerDisplay() {
        mSecondPicker.setValue(mCurrentSecond);
    }

    /**
     * Set the reference of seconds picker, its digit format and register value change listener.
     *
     * @param pickerVisible if false, removes the spinner widget.
     */
    protected void setupSecondPicker(boolean pickerVisible) {
        mSecondPicker = findViewById(R.id.seconds);
        if (pickerVisible) {
            mSecondPicker.setMinValue(0);
            mSecondPicker.setMaxValue(59);
            mSecondPicker.setFormatter(TWO_DIGIT_FORMATTER);
            mSecondPicker.setOnValueChangedListener((picker, oldVal, newVal) -> mCurrentSecond = newVal);
        } else {
            mHourPicker.setVisibility(View.GONE);
        }
    }

    /**
     * Set the reference of minute picker, its digit format and register value change listener.
     *
     * @param pickerVisible if false, removes the spinner widget.
     */
    protected void setupMinutePicker(boolean pickerVisible) {
        mMinutePicker = findViewById(R.id.minute);
        if (pickerVisible) {
            mMinutePicker.setMinValue(0);
            mMinutePicker.setMaxValue(59);
            mMinutePicker.setFormatter(TWO_DIGIT_FORMATTER);
            mMinutePicker.setOnValueChangedListener((spinner, oldVal, newVal) -> mCurrentMinute = newVal);
        } else {
            mMinutePicker.setVisibility(View.GONE);
            findViewById(R.id.minute_divider).setVisibility(View.GONE);
        }
    }

    /**
     * Set the reference of hour picker, its digit format and register value change listener.
     *
     * @param pickerVisible if false, removes the spinner widget.
     */
    protected void setupHourPicker(boolean pickerVisible) {

        mHourPicker = findViewById(R.id.hour);
        if (pickerVisible) {
            mHourPicker.setMinValue(0);
            mHourPicker.setMaxValue(10);
            mHourPicker.setFormatter(TWO_DIGIT_FORMATTER);
            mHourPicker.setOnValueChangedListener((picker, oldVal, newVal) -> mCurrentHour = newVal);
        } else {
            mHourPicker.setVisibility(View.GONE);
            findViewById(R.id.hour_divider).setVisibility(View.GONE);
        }
    }

    /**
     * Type of picker. Used to remove unwanted hour picker.
     */
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
}
