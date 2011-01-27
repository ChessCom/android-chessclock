package com.chess.views;

import java.util.ArrayList;

import com.chess.R;
import com.chess.model.VideoItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class VideosAdapter extends ArrayAdapter<VideoItem> {
	
	public ArrayList<VideoItem> items;
	private LayoutInflater vi;
	private int resource;
	private Context CTX;

    public VideosAdapter(Context context, int textViewResourceId, ArrayList<VideoItem> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.vi = LayoutInflater.from(context);
        this.resource = textViewResourceId;
        this.CTX = context;
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
					new AlertDialog.Builder(CTX)
		            .setTitle(el.values.get("title"))
		            .setMessage(el.values.get("description"))
		            .setPositiveButton(CTX.getString(R.string.ok), null)
		            .create().show();
				}
			});
            convertView.findViewById(R.id.play).setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(Intent.ACTION_VIEW);
		            i.setDataAndType(Uri.parse(el.values.get("view_url").trim()), "video/*");
		            CTX.startActivity(i);
				}
			});
            
            CharSequence date = DateFormat.format("dd.MM.yyyy", 1000*Long.parseLong(el.values.get("publish_timestamp")));
                    
            if(title != null)	title.setText(el.values.get("title"));
            if(times != null)	times.setText(CTX.getString(R.string.duration)+" "+el.values.get("minutes")+"min "+CTX.getString(R.string.published)+" "+date);
            if(desc != null)	desc.setText(el.values.get("description"));
            if(addinfo != null)	addinfo.setText(el.values.get("author_username"));
            
        }
        return convertView;
    }
}
