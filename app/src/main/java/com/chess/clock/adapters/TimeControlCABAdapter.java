package com.chess.clock.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.fragments.SettingsFragment;

import java.util.ArrayList;

/**
 * TimeControl Adapter used when in Context Action Bar mode is activated.
 */
public class TimeControlCABAdapter extends ArrayAdapter<TimeControlWrapper> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<TimeControlWrapper> data;
    private Fragment mTargetFragment;

    public TimeControlCABAdapter(Context context, ArrayList<TimeControlWrapper> objects, Fragment targetFragment) {
        super(context, R.layout.list_time_control_item_multi_choice, objects);
        this.layoutResourceId = R.layout.list_time_control_item_multi_choice;
        this.context = context;
        this.data = objects;
        this.mTargetFragment = targetFragment;
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

        View row = convertView;
        final TimeControlHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new TimeControlHolder();
            holder.textView = (TextView) row.findViewById(R.id.time_control_text);
            holder.checkBox = (CheckBox) row.findViewById(R.id.time_control_checkbox);

            // Note: ImageButton must have the following attributes set to false to parent behave correctly: focusable.
            holder.editImgBtn = (ImageButton) row.findViewById(R.id.time_control_edit_image_btn);
            holder.editImgBtn.setFocusable(false);
            holder.editImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (Integer) holder.editImgBtn.getTag();
                    ((SettingsFragment) mTargetFragment).loadTimeControl(position);
                }
            });

            row.setTag(holder);
        } else {
            holder = (TimeControlHolder) row.getTag();
        }

        holder.editImgBtn.setTag(position);

        TimeControl tc = data.get(position).getTimeControlPlayerOne();
        holder.textView.setText(tc.getName());

        return row;
    }

    static class TimeControlHolder {
        CheckBox checkBox;
        TextView textView;
        ImageButton editImgBtn;
    }
}
