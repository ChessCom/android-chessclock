package com.chess.clock.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.appcompat.widget.AppCompatCheckedTextView;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.AppTheme;

import java.util.ArrayList;


public class TimeControlAdapter extends ArrayAdapter<TimeControlWrapper> {

    private ArrayList<TimeControlWrapper> data;
    private ColorStateList radioButtonColors;

    public TimeControlAdapter(
            Context context,
            ArrayList<TimeControlWrapper> timeControls,
            AppTheme theme
    ) {
        super(context, R.layout.list_time_control_item_single_choice, R.id.time_control_text,
                timeControls);
        data = timeControls;
        if(theme!= null){
            radioButtonColors = theme.colorStateListChecked(getContext());
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public TimeControlWrapper getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AppCompatCheckedTextView row =
                (AppCompatCheckedTextView) super.getView(position, convertView, parent);
        TimeControlWrapper tc = getItem(position);
        if (tc != null && tc.getTimeControlPlayerOne() != null) {
            row.setText(tc.getTimeControlPlayerOne().getName());
        } else {
            row.setText(R.string.time_control);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            row.setCheckMarkTintList(radioButtonColors);
        }
        return row;
    }

    public void updateTheme(AppTheme theme) {
        radioButtonColors = theme.colorStateListChecked(getContext());
        notifyDataSetChanged();
    }

    public void updateTimeControls(ArrayList<TimeControlWrapper> currentTimeControls) {
        data = currentTimeControls;
        notifyDataSetChanged();
    }
}
