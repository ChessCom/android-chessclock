package com.chess.activities.tabs;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

import com.chess.R;
import com.chess.activities.Game;
import com.chess.core.CoreActivity;
import com.flurry.android.FlurryAgent;

public class Computer extends CoreActivity {

	private Spinner Strength;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.computer);

		Strength = (Spinner)findViewById(R.id.PrefStrength);
		Strength.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				try{
					if(App.SDeditor != null && App.sharedData != null && pos >= 0){
						App.SDeditor.putInt(App.sharedData.getString("username", "")+"strength", pos);
						App.SDeditor.commit();
					}
				} catch (Exception e) {}
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});

		findViewById(R.id.start).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LoadNext(0);
			}
		});
	}

	@Override
	protected void onResume() {
		if (App.isLiveChess())
    {
      App.setLiveChess(false);
      new AsyncTask<Void, Void, Void>()
      {
        @Override
        protected Void doInBackground(Void... voids)
        {
          App.getLccHolder().logout();
          return null;
        }
      }.execute();
    }
    super.onResume();
		if(Strength != null && App != null && App.sharedData != null){
			Strength.post(new Runnable() {
	            public void run() {
	            	Strength.setSelection(App.sharedData.getInt(App.sharedData.getString("username", "")+"strength", 0));
	        	}
	        });
			if(!App.sharedData.getString("saving", "").equals("")){
				findViewById(R.id.load).setVisibility(View.VISIBLE);
				findViewById(R.id.load).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
            FlurryAgent.onEvent("New Game VS Computer", null);
						startActivity(new Intent(Computer.this, Game.class).putExtra("mode", Integer.parseInt(App.sharedData.getString("saving", "").substring(0,1))));
					}
				});
			} else{
				findViewById(R.id.load).setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void LoadNext(int code) {
		RadioButton wh, bh;
		wh = (RadioButton)findViewById(R.id.wHuman);
		bh = (RadioButton)findViewById(R.id.bHuman);

		int mode = 0;
		if(!wh.isChecked() && bh.isChecked())
			mode = 1;
		else if(wh.isChecked() && bh.isChecked())
			mode = 2;
		else if(!wh.isChecked() && !bh.isChecked())
			mode = 3;

		App.SDeditor.putString("saving", "");
		App.SDeditor.commit();

    FlurryAgent.onEvent("New Game VS Computer", null);
		startActivity(new Intent(this, Game.class).putExtra("mode", mode));
	}
	@Override
	public void LoadPrev(int code) {
		//finish();
		App.mTabHost.setCurrentTab(0);
	}
	@Override
	public void Update(int code) {

	}
}
