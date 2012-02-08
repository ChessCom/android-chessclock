package com.chess.activities;

import actionbarcompat.ActionBarActivityHome;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.chess.R;
import com.chess.views.BackgroundChessDrawable;

public class DashBoardTestActivity extends ActionBarActivityHome implements OnClickListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main3);

		View mainView = findViewById(R.id.mainView);
		mainView.setBackgroundDrawable(new BackgroundChessDrawable(this));
		int padding = getResources().getDrawable(R.drawable.chess_cells).getIntrinsicWidth() / 2;
		mainView.setPadding(padding, padding, padding, padding);

//		findViewById(R.id.playLive).setTouchDelegate(findViewById(R.id.playLiveFrame).getTouchDelegate());
		findViewById(R.id.playLiveFrame).setOnClickListener(this);
		findViewById(R.id.playOnlineFrame).setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
			break;

		case R.id.menu_settings:
			Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.playLiveFrame) {
			Toast.makeText(this, "playLiveFrame", Toast.LENGTH_SHORT).show();
		} else if (v.getId() == R.id.playOnlineFrame) {
			Toast.makeText(this, "playOnlineFrame", Toast.LENGTH_SHORT).show();
		}
	}


}