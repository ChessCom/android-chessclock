package com.chess.ui.fragments.daily;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ChatItem;
import com.chess.backend.entity.new_api.DailyChatItem;
import com.chess.backend.entity.new_api.DailyCurrentGameData;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.ChatMessagesAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 11:23
 */
public class DailyChatFragment extends CommonLogicFragment{

	private static final String GAME_ID = "game_id";
	private static final String OPPONENT_NAME = "opponent_name";
	private static final String OPPONENT_AVATAR = "opponent_avatar";
	private int UPDATE_DELAY = 10000;

	private EditText sendEdt;
	private ListView listView;
	private ChatMessagesAdapter messagesAdapter;
	private List<ChatItem> chatItems;
	private ChatItemsUpdateListener receiveUpdateListener;
	private ChatItemsUpdateListener sendUpdateListener;
	private View progressBar;
	private RoboButton sendBtn;
//	private String opponentName;
	private long gameId;
	private long timeStamp;
	private TimeStampForListUpdateListener timeStampForListUpdateListener;
	private TimeStampForSendMessageListener timeStampForSendMessageListener;
	private String myAvatar;
	private String opponentAvatar;

	public static DailyChatFragment createInstance(long gameId, String opponentAvatar) {
		DailyChatFragment fragment = new DailyChatFragment();

		Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, gameId);
		bundle.putString(OPPONENT_AVATAR, opponentAvatar);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myAvatar = getAppData().getUserAvatar();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_chat_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.chat);

		widgetsInit(view);

		chatItems = new ArrayList<ChatItem>();
		receiveUpdateListener = new ChatItemsUpdateListener(ChatItemsUpdateListener.RECEIVE);
		sendUpdateListener = new ChatItemsUpdateListener(ChatItemsUpdateListener.SEND);
		timeStampForListUpdateListener = new TimeStampForListUpdateListener();
		timeStampForSendMessageListener = new TimeStampForSendMessageListener();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null) {
			gameId = getArguments().getLong(GAME_ID);
//			opponentName = getArguments().getString(OPPONENT_NAME);
			opponentAvatar = getArguments().getString(OPPONENT_AVATAR);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
//			opponentName = savedInstanceState.getString(OPPONENT_NAME);
			opponentAvatar = savedInstanceState.getString(OPPONENT_AVATAR);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(GAME_ID, gameId);
//		outState.putString(OPPONENT_NAME, opponentName);
		outState.putString(OPPONENT_AVATAR, opponentAvatar);
	}

	protected void widgetsInit(View view){

		sendEdt = (EditText) view.findViewById(R.id.sendEdt);
		listView = (ListView) view.findViewById(R.id.listView);

		progressBar = view.findViewById(R.id.progressBar);

		sendBtn = (RoboButton) view.findViewById(R.id.sendBtn);
		sendBtn.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		showKeyBoard(sendEdt);

		updateList();
		handler.postDelayed(updateListOrder, UPDATE_DELAY);
	}

	@Override
	public void onPause() {
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

	private class ChatItemsUpdateListener extends ChessUpdateListener<DailyChatItem> {
		public static final int SEND = 0;
		public static final int RECEIVE = 1;
		private int listenerCode;

		public ChatItemsUpdateListener(int listenerCode) {
			super(DailyChatItem.class);
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
					// manually add avatar urls
					for (ChatItem chatItem : chatItems) {
						if (chatItem.isMine()) {
							chatItem.setAvatar(myAvatar);
						} else {
							chatItem.setAvatar(opponentAvatar);
						}
					}

					if (messagesAdapter == null) {
						messagesAdapter = new ChatMessagesAdapter(getContext(), chatItems);
						listView.setAdapter(messagesAdapter);
					} else {
						messagesAdapter.setItemsList(chatItems);
					}
					sendEdt.setText(StaticData.SYMBOL_EMPTY);

					listView.setSelection(chatItems.size() - 1);
					updateList();
					break;
				case RECEIVE:
					if (before != chatItems.size()) {

						// manually add avatar urls
						for (ChatItem chatItem : chatItems) {
							if (chatItem.isMine()) {
								chatItem.setAvatar(myAvatar);
							} else {
								chatItem.setAvatar(opponentAvatar);
							}
						}
						if (messagesAdapter == null) {
							messagesAdapter = new ChatMessagesAdapter(getContext(), chatItems);
							listView.setAdapter(messagesAdapter);
						} else {
							messagesAdapter.notifyDataSetChanged();
						}
						listView.setSelection(chatItems.size() - 1);
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

	private LoadItem createGetTimeStampLoadItem() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_GAME_ID, gameId);
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
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);

			new RequestJsonTask<DailyChatItem>(receiveUpdateListener).executeTask(loadItem);
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
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
			loadItem.addRequestParams(RestHelper.P_MESSAGE, message);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);

			new RequestJsonTask<DailyChatItem>(sendUpdateListener).executeTask(loadItem);
		}
	}

}
