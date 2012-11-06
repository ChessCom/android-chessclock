package com.chess.ui.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.BaseGameItem;
import com.chess.model.GameOnlineItem;
import com.chess.model.MessageItem;
import com.chess.ui.adapters.MessagesAdapter;
import com.chess.utilities.ChessComApiParser;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ChatOnlineActivity extends LiveBaseActivity {

	private int UPDATE_DELAY = 10000;

	private EditText sendEdt;
	private ListView chatListView;
	private MessagesAdapter messagesAdapter;
	private ArrayList<MessageItem> chatItems;
	private AsyncTask<LoadItem, Void, Integer> getDataTask;
	private ListUpdateListener listUpdateListener;
	private SendUpdateListener sendUpdateListener;
	private View progressBar;
	private ImageButton sendBtn;
	private long gameId;
	private long timeStamp;
	private GetTimeStampForListUpdateListener getTimeStampForListUpdateListener;
	private GetTimeStampForSendMessageListener getTimeStampForSendMessageListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_screen);

		widgetsInit();

		NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.cancel(R.string.you_got_new_msg);

		gameId = extras.getLong(BaseGameItem.GAME_ID);
		chatItems = new ArrayList<MessageItem>();
		listUpdateListener = new ListUpdateListener();
		sendUpdateListener = new SendUpdateListener();
		getTimeStampForListUpdateListener = new GetTimeStampForListUpdateListener();
		getTimeStampForSendMessageListener = new GetTimeStampForSendMessageListener();

		showActionRefresh = true;
	}

	protected void widgetsInit(){

		sendEdt = (EditText) findViewById(R.id.sendEdt);
		chatListView = (ListView) findViewById(R.id.chatLV);

		progressBar = findViewById(R.id.progressBar);

		sendBtn = (ImageButton) findViewById(R.id.sendBtn);
		sendBtn.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		showKeyBoard(sendEdt);

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

	public void updateList() {
		LoadItem loadItem = createGetTimeStampLoadItem();
		new GetStringObjTask(getTimeStampForListUpdateListener).executeTask(loadItem);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu_refresh:
				updateList();
				break;
		}
		return super.onOptionsItemSelected(item);
	}


	private void onMessageReceived(String response){
		int before = chatItems.size();
		chatItems.clear();
		chatItems.addAll(ChessComApiParser.receiveMessages(response));
		if (before != chatItems.size()) {
			if (messagesAdapter == null) {
				messagesAdapter = new MessagesAdapter(this, chatItems);
				chatListView.setAdapter(messagesAdapter);
			} else {
				messagesAdapter.notifyDataSetChanged();
			}
			chatListView.setSelection(chatItems.size() - 1);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sendBtn) {
			sendMessage();
		}
	}

	private void sendMessage() {
		LoadItem loadItem = createGetTimeStampLoadItem();
		new GetStringObjTask(getTimeStampForSendMessageListener).executeTask(loadItem);
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

		if (messagesAdapter == null) {
			messagesAdapter = new MessagesAdapter(this, chatItems);
			chatListView.setAdapter(messagesAdapter);
		} else {
			messagesAdapter.setItemsList(chatItems);
		}
		sendEdt.setText(StaticData.SYMBOL_EMPTY);

		chatListView.setSelection(chatItems.size() - 1);
		updateList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(getDataTask != null)
			getDataTask.cancel(true);
	}

	private LoadItem createGetTimeStampLoadItem() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_GAME_V5);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_GID, gameId);
		return loadItem;
	}

	private class GetTimeStampListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			final GameOnlineItem currentGame = ChessComApiParser.getGameParseV3(returnedObj);
			timeStamp = currentGame.getTimestamp();
		}
	}

	private class GetTimeStampForListUpdateListener extends GetTimeStampListener {
		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(ChatOnlineActivity.this));
			loadItem.addRequestParams(RestHelper.P_CHESSID, gameId);
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);

			getDataTask = new GetStringObjTask(listUpdateListener).executeTask(loadItem);
		}
	}

	private class GetTimeStampForSendMessageListener extends GetTimeStampListener {
		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);

			String message = StaticData.SYMBOL_EMPTY;
			try {
				message = URLEncoder.encode(sendEdt.getText().toString(), HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e("Chat", e.toString());
				// correctly
				showToast(R.string.encoding_unsupported);
			}

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(ChatOnlineActivity.this));
			loadItem.addRequestParams(RestHelper.P_CHESSID, gameId);
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
			loadItem.addRequestParams(RestHelper.P_MESSAGE, message);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);

			getDataTask = new GetStringObjTask(sendUpdateListener).executeTask(loadItem);
		}
	}
}
