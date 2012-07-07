package com.chess.ui.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameListItem;
import com.chess.model.MessageItem;
import com.chess.ui.adapters.MessagesAdapter;
import com.chess.utilities.ChessComApiParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ChatOnlineActivity extends LiveBaseActivity {

	private int UPDATE_DELAY = 10000;

	private EditText sendText;
	private ListView chatListView;
	private MessagesAdapter messages = null;
	private ArrayList<MessageItem> chatItems = new ArrayList<MessageItem>();
	private AsyncTask<LoadItem, Void, Integer> getDataTask;
	private ListUpdateListener listUpdateListener;
	private SendUpdateListener sendUpdateListener;
	private View progressBar;
	private Button sendBtn;
	private long gameId;
	private String timeStamp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_screen);

		widgetsInit();

		NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.cancel(R.string.you_got_new_msg);

		gameId = extras.getLong(GameListItem.GAME_ID);
		timeStamp = extras.getString(GameListItem.TIMESTAMP);

		listUpdateListener = new ListUpdateListener();
		sendUpdateListener = new SendUpdateListener();
	}

	protected void widgetsInit(){

		sendText = (EditText) findViewById(R.id.sendText);
		chatListView = (ListView) findViewById(R.id.chatLV);

		progressBar = findViewById(R.id.progressBar);

		sendBtn = (Button) findViewById(R.id.send);
		sendBtn.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateList();
		handler.postDelayed(updateListOrder, UPDATE_DELAY);
	}

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(updateListOrder);
	}

	private Runnable updateListOrder = new Runnable() {
		@Override
		public void run() {
			updateList();
			handler.removeCallbacks(this);
			handler.postDelayed(this, UPDATE_DELAY);
		}
	};

	public void updateList(){
		// submit echess action
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
		loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
		loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);

		getDataTask = new GetStringObjTask(listUpdateListener).executeTask(loadItem);
	}

	private class ListUpdateListener extends ActionBarUpdateListener<String> {
		public ListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			progressBar.setVisibility(show? View.VISIBLE: View.INVISIBLE);
			sendBtn.setEnabled(!show);
		}

		@Override
		public void updateData(String returnedObj) {
			onMessageReceived(returnedObj);
		}
	}

	public void onMessageReceived(String response){
		int before = chatItems.size();
		chatItems.clear();
		chatItems.addAll(ChessComApiParser.receiveMessages(response));
		if (before != chatItems.size()) {
			if (messages == null) {
				messages = new MessagesAdapter(this, R.layout.chat_item, chatItems);
				chatListView.setAdapter(messages);
			} else {
				messages.notifyDataSetChanged();
			}
			chatListView.setSelection(chatItems.size() - 1);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.send) {
			sendMessage();
		}
	}

	private void sendMessage(){
		String message = StaticData.SYMBOL_EMPTY;
		try {
			message = URLEncoder.encode(sendText.getText().toString(), AppConstants.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e("Chat", e.toString());
			// correctly
			showToast(R.string.encoding_unsopported);
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
		loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
		loadItem.addRequestParams(RestHelper.P_MESSAGE, message);
		loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);

		getDataTask = new GetStringObjTask(sendUpdateListener).executeTask(loadItem);
	}

	private class SendUpdateListener extends ActionBarUpdateListener<String> {
		public SendUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			progressBar.setVisibility(show? View.VISIBLE: View.INVISIBLE);
			sendBtn.setEnabled(!show);
		}

		@Override
		public void updateData(String returnedObj) {
			onMessageSent(returnedObj);
		}
	}

	public void onMessageSent(String response){
		chatItems.clear();
		chatItems.addAll(ChessComApiParser.receiveMessages(response));

		if (messages == null) {
			messages = new MessagesAdapter(this, R.layout.chat_item, chatItems);
			chatListView.setAdapter(messages);
		} else {
			messages.notifyDataSetChanged();
		}
		sendText.setText(StaticData.SYMBOL_EMPTY);

		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
		chatListView.setSelection(chatItems.size() - 1);

		updateList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(getDataTask != null)
			getDataTask.cancel(true);
	}
}
