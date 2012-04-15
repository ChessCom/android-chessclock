package com.chess.ui.activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameListItem;
import com.chess.model.MessageItem;
import com.chess.ui.adapters.MessagesAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MyProgressDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ChatActivity extends CoreActivityActionBar implements OnClickListener {
	public static int MESSAGE_RECEIVED = 0;
	public static int MESSAGE_SENT = 1;
	private EditText sendText;
	private ListView chatListView;
	private MessagesAdapter messages = null;
	private ArrayList<MessageItem> chatItems = new ArrayList<MessageItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_screen);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		sendText = (EditText) findViewById(R.id.sendText);
		chatListView = (ListView) findViewById(R.id.chatLV);
		findViewById(R.id.send).setOnClickListener(this);

        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.cancel(R.string.you_got_new_msg);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);
	}


	public void initActivity(){

	}

	public void onMessageReceived(){

	}

	public void onMessageSent(){

	}

	@Override
	public void update(int code) { // TODO Replace with named methods calls
		if (code == INIT_ACTIVITY) {
			if (appService != null) {
				appService.RunRepeatableTask(MESSAGE_RECEIVED, 0, 60000, "http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY) + AppConstants.CHESSID_PARAMETER
						+ extras.getLong(GameListItem.GAME_ID) + "&command=CHAT&timestamp=" + extras.getString(GameListItem.TIMESTAMP),
						null);
			}
		} else if (code == MESSAGE_RECEIVED) {
			int before = chatItems.size();
			chatItems.clear();
			chatItems.addAll(ChessComApiParser.receiveMessages(responseRepeatable));
			if (before != chatItems.size()) {
				if (messages == null) {
					messages = new MessagesAdapter(ChatActivity.this, R.layout.chat_item, chatItems);
					chatListView.setAdapter(messages);
				} else {
					messages.notifyDataSetChanged();
				}
				chatListView.setSelection(chatItems.size() - 1);
			}
		} else if (code == MESSAGE_SENT) {
			chatItems.clear();
			chatItems.addAll(ChessComApiParser.receiveMessages(response));

			if (messages == null) {
				messages = new MessagesAdapter(ChatActivity.this, R.layout.chat_item, chatItems);
				chatListView.setAdapter(messages);
			} else {
				messages.notifyDataSetChanged();
			}
			sendText.setText(AppConstants.SYMBOL_EMPTY);
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
			chatListView.setSelection(chatItems.size() - 1);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.send) {
			String message = AppConstants.SYMBOL_EMPTY;
			try {
				message = URLEncoder.encode(sendText.getText().toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e("Chat", e.toString());	 // TODO handle exception
				// correctly
			}

			if (appService != null) {
				// Use rest-helper and async task
//				LoadItem loadItem = new LoadItem();  // TODO
//				loadItem.setLoadPath(RestHelper.SUBMIT_ECHESS_ACTION);
//				loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance(this).getUserToken());
//				loadItem.addRequestParams(RestHelper.P_CHESSID, AppData.getInstance(this).getUserToken());

				String query = "http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
						+ AppConstants.CHESSID_PARAMETER + extras.getLong(GameListItem.GAME_ID)
						+ "&command=CHAT&message=" + message
						+ AppConstants.TIMESTAMP_PARAMETER + extras.getString(GameListItem.TIMESTAMP);
				appService.RunSingleTask(MESSAGE_SENT, query,
						progressDialog = new MyProgressDialog(ProgressDialog.show(ChatActivity.this, null,
								getString(R.string.sendingmessage), true)));
			}
		}
	}
}
