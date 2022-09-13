package com.chess.clock.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import com.chess.clock.R;
import com.chess.clock.engine.TimeControlWrapper;

import java.util.ArrayList;


public class TimeControlAdapter extends ArrayAdapter<TimeControlWrapper> {

    ArrayList<TimeControlWrapper> mTimeControls;

    public TimeControlAdapter(Context context, ArrayList<TimeControlWrapper> timeControls) {
        super(context, R.layout.list_time_control_item_single_choice, R.id.time_control_text,
                timeControls);
        mTimeControls = timeControls;
    }

    @Override
    public int getCount() {
        return mTimeControls.size();
    }

    @Override
    public TimeControlWrapper getItem(int position) {
        return mTimeControls.get(position);
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
            row.setText(R.string.title_activity_time_control);
        }

        return row;
    }
}
