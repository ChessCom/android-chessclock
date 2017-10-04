package com.chess.clock.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.chess.clock.R;

public class TimeControlCheckBox extends CheckBox {

    public TimeControlCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        this.setTextColor(checked ?
                getResources().getColor(R.color.list_item_text_color_checked) :
                getResources().getColor(R.color.list_item_text_color_normal));
    }
}
