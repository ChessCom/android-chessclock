package com.chess.ui.activities.old;

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
import com.chess.backend.entity.new_api.ChatItem;
import com.chess.backend.entity.new_api.DailyChatItem;
import com.chess.backend.entity.new_api.DailyCurrentGameData;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.BaseGameItem;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.adapters.ChatMessagesAdapter;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ChatOnlineActivity extends LiveBaseActivity {

	private int UPDATE_DELAY = 10000;

	private EditText sendEdt;
	private ListView chatListView;
	private ChatMessagesAdapter messagesAdapter;
	private List<ChatItem> chatItems;
	private AsyncTask<LoadItem, Void, Integer> getDataTask;
	private ChatItemsUpdateListener receiveUpdateListener;
	private ChatItemsUpdateListener sendUpdateListener;
	private View progressBar;
	private ImageButton sendBtn;
	private long gameId;
	private long timeStamp;
	private TimeStampForListUpdateListener timeStampForListUpdateListener;
	private TimeStampForSendMessageListener timeStampForSendMessageListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_screen);

		widgetsInit();

		NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.cancel(R.string.you_got_new_msg);

		gameId = extras.getLong(BaseGameItem.GAME_ID);
		chatItems = new ArrayList<ChatItem>();
		receiveUpdateListener = new ChatItemsUpdateListener(ChatItemsUpdateListener.RECEIVE);
		sendUpdateListener = new ChatItemsUpdateListener(ChatItemsUpdateListener.SEND);
		timeStampForListUpdateListener = new TimeStampForListUpdateListener();
		timeStampForSendMessageListener = new TimeStampForSendMessageListener();

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
		new RequestJsonTask<DailyCurrentGameData>(timeStampForListUpdateListener).executeTask(loadItem);
	}

	private class ChatItemsUpdateListener extends ActionBarUpdateListener<DailyChatItem> {
		public static final int SEND = 0;
		public static final int RECEIVE = 1;
		private int listenerCode;
		public ChatItemsUpdateListener(int listenerCode) {
			super(getInstance(),DailyChatItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			progressBar.setVisibility(show? View.VISIBLE: View.INVISIBLE);
			sendBtn.setEnabled(!show);
		}

		@Override
		public void updateData(DailyChatItem returnedObj) {
			int before = chatItems.size();
			chatItems.clear();
			chatItems.addAll(returnedObj.getData());
			switch (listenerCode) {
				case SEND:

					if (messagesAdapter == null) {
						messagesAdapter = new ChatMessagesAdapter(getContext(), chatItems);
						chatListView.setAdapter(messagesAdapter);
					} else {
						messagesAdapter.setItemsList(chatItems);
					}
					sendEdt.setText(StaticData.SYMBOL_EMPTY);

					chatListView.setSelection(chatItems.size() - 1);
					updateList();
					break;
				case RECEIVE:
					if (before != chatItems.size()) {
						if (messagesAdapter == null) {
							messagesAdapter = new ChatMessagesAdapter(getContext(), chatItems);
							chatListView.setAdapter(messagesAdapter);
						} else {
							messagesAdapter.notifyDataSetChanged();
						}
						chatListView.setSelection(chatItems.size() - 1);
					}
					break;
			}
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

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sendBtn) {
			sendMessage();
		}
	}

	private void sendMessage() {
		LoadItem loadItem = createGetTimeStampLoadItem();
		new RequestJsonTask<DailyCurrentGameData>(timeStampForSendMessageListener).executeTask(loadItem);
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
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_GID, gameId);
		return loadItem;
	}

	private class GetTimeStampListener extends ActionBarUpdateListener<DailyCurrentGameData> { // TODO use batch API

		public GetTimeStampListener() {
			super(getInstance(), DailyCurrentGameData.class);
		}

		@Override
		public void updateData(DailyCurrentGameData returnedObj) {
			final DailyCurrentGameData currentGame = returnedObj;
			timeStamp = currentGame.getTimestamp();
		}
	}

	private class TimeStampForListUpdateListener extends GetTimeStampListener {
		@Override
		public void updateData(DailyCurrentGameData returnedObj) {
			super.updateData(returnedObj);

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameId));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(ChatOnlineActivity.this));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);

			getDataTask = new RequestJsonTask<DailyChatItem>(receiveUpdateListener).executeTask(loadItem);
		}
	}

	private class TimeStampForSendMessageListener extends GetTimeStampListener {
		@Override
		public void updateData(DailyCurrentGameData returnedObj) {
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
			loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameId));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(ChatOnlineActivity.this));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
			loadItem.addRequestParams(RestHelper.P_MESSAGE, message);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);

			getDataTask = new RequestJsonTask<DailyChatItem>(sendUpdateListener).executeTask(loadItem);
		}
	}
}
