package com.chess.clock.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.chess.clock.app.R;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.views.TimeControlCheckedTextView;

import java.util.ArrayList;


public class TimeControlAdapter extends ArrayAdapter<TimeControl> {

	ArrayList<TimeControl> mTimeControls;

	public TimeControlAdapter(Context context, ArrayList<TimeControl> timeControls) {
		super(context, R.layout.list_time_control_item_single_choice, R.id.time_control_text,
				timeControls);
		mTimeControls = timeControls;
	}

	@Override
	public int getCount() {
		return mTimeControls.size();
	}

	@Override
	public TimeControl getItem(int position) {
		return mTimeControls.get(position);
	}

	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TimeControlCheckedTextView row =
				(TimeControlCheckedTextView) super.getView(position, convertView, parent);

		TimeControl tc = getItem(position);
		row.setText(tc.getName());

		return row;
	}
}
