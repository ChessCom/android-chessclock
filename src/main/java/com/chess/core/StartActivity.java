package com.chess.core;

import com.chess.R;
import com.chess.activities.Singin;
import com.chess.utilities.Notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends CoreActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        //defaults
        App.LoadBoard(App.res_boards[App.sharedData.getInt(App.sharedData.getString("username", "")+"board", 0)]);
        App.LoadPieces(App.res_pieces[App.sharedData.getInt(App.sharedData.getString("username", "")+"pieces", 0)]);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);

        LoadNext(0);
    }
	@Override
	public void LoadPrev(int code) {
		finish();
	}
	@Override
	public void LoadNext(int code) {
    System.out.println("!!!!!!!!!!! StartActivity 1");
		if(App.sharedData.getString("user_token", "").equals("")){
			startActivity(new Intent(this, Singin.class));
      System.out.println("!!!!!!!!!!! StartActivity 2");
      App.guest = true;
		} else{
      System.out.println("!!!!!!!!!!! StartActivity 3");
			if(App.sharedData.getBoolean(App.sharedData.getString("username", "")+"notifE", true))
	        	startService(new Intent(this, Notifications.class));

			boolean fromnotif = false;
			if(extras != null && extras.getBoolean("fromnotif"))	fromnotif = true;

			startActivity(new Intent(this, Tabs.class).putExtra("fromnotif", fromnotif));
			App.guest = false;
		}
		finish();
	}
	@Override
	public void Update(int code) {}
}
