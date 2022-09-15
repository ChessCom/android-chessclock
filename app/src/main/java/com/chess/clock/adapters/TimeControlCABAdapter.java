package com.chess.clock.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.Fragment;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.fragments.TimeSettingsFragment;

import java.util.ArrayList;

/**
 * TimeControl Adapter used when in Context Action Bar mode is activated.
 */
public class TimeControlCABAdapter extends ArrayAdapter<TimeControlWrapper> {

    private final Context context;
    private final int layoutResourceId;
    private final ArrayList<TimeControlWrapper> data;
    private final Fragment mTargetFragment;
    private final ColorStateList checkBoxColors;

    public TimeControlCABAdapter(
            Context context,
            ArrayList<TimeControlWrapper> objects,
            Fragment targetFragment,
            AppTheme theme
    ) {
        super(context, R.layout.list_time_control_item_multi_choice, objects);
        this.layoutResourceId = R.layout.list_time_control_item_multi_choice;
        this.context = context;
        this.data = objects;
        this.mTargetFragment = targetFragment;
        // todo NPE on screen rotation
        checkBoxColors = theme.colorStateListChecked(context);
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
            holder.textView = row.findViewById(R.id.time_control_text);
            holder.checkBox = row.findViewById(R.id.time_control_checkbox);

            // Note: ImageButton must have the following attributes set to false to parent behave correctly: focusable.
            holder.editImgBtn = row.findViewById(R.id.time_control_edit_image_btn);
            holder.editImgBtn.setFocusable(false);
            holder.editImgBtn.setOnClickListener(v -> {
                int position1 = (Integer) holder.editImgBtn.getTag();
                ((TimeSettingsFragment) mTargetFragment).loadTimeControl(position1);
            });

            row.setTag(holder);
        } else {
            holder = (TimeControlHolder) row.getTag();
        }

        holder.editImgBtn.setTag(position);

        TimeControl tc = data.get(position).getTimeControlPlayerOne();
        holder.textView.setText(tc.getName());

        CompoundButtonCompat.setButtonTintList(holder.checkBox, checkBoxColors);

        return row;
    }

    static class TimeControlHolder {
        AppCompatCheckBox checkBox;
        TextView textView;
        ImageButton editImgBtn;
    }
}
