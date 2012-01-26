package com.chess.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.model.VideoItem;

import java.util.ArrayList;

public class VideosAdapter extends ArrayAdapter<VideoItem> {

	private ArrayList<VideoItem> items;
	private LayoutInflater vi;
	private int resource;
	private Context context;

	public VideosAdapter(Context context, int textViewResourceId, ArrayList<VideoItem> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.vi = LayoutInflater.from(context);
		this.resource = textViewResourceId;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = vi.inflate(resource, null);
		}
		final VideoItem el = items.get(position);
		if (el != null) {
			TextView title = (TextView) convertView.findViewById(R.id.title);
			TextView times = (TextView) convertView.findViewById(R.id.times);
			TextView desc = (TextView) convertView.findViewById(R.id.desc);
			TextView addinfo = (TextView) convertView.findViewById(R.id.addinfo);

			convertView.findViewById(R.id.fulldesc).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(context)
							.setTitle(el.values.get(AppConstants.TITLE))
							.setMessage(el.values.get("description"))
							.setPositiveButton(context.getString(R.string.ok), null)
							.create().show();
				}
			});
			convertView.findViewById(R.id.play).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setDataAndType(Uri.parse(el.values.get("view_url").trim()), "video/*");
					context.startActivity(i);
				}
			});

			CharSequence date = DateFormat.format("dd.MM.yyyy", 1000 * Long.parseLong(el.values.get("publish_timestamp")));

			if (title != null) title.setText(el.values.get(AppConstants.TITLE));
			if (times != null)
				times.setText(context.getString(R.string.duration) + " " + el.values.get("minutes") + "min " + context.getString(R.string.published) + " " + date);
			if (desc != null) desc.setText(el.values.get("description"));
			if (addinfo != null) addinfo.setText(el.values.get("author_username"));

		}
		return convertView;
	}
}
