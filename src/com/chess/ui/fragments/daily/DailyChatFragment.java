package com.chess.ui.fragments.daily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ChatItem;
import com.chess.backend.entity.api.GamesChatItem;
import com.chess.backend.entity.api.daily_games.DailyChatItem;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameData;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.statics.IntentConstants;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ChatMessagesAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;
import com.chess.widgets.RoboButton;

import java.util.ArrayList;
import java.util.List;

import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 11:23
 */
public class DailyChatFragment extends CommonLogicFragment implements View.OnTouchListener {

	private static final String GAME_ID = "game_id";
	private static final String OPPONENT_NAME = "opponent_name";
	private static final String OPPONENT_AVATAR = "opponent_avatar";

	private EditText sendEdt;
	private ListView listView;
	private ChatMessagesAdapter messagesAdapter;
	private List<ChatItem> chatItems;
	private ReceiveChatItemsUpdateListener receiveUpdateListener;
	private SendChatItemsUpdateListener sendUpdateListener;
	private View progressBar;
	private RoboButton sendBtn;
	private long gameId;
	private long timeStamp;
	private TimeStampForSendMessageListener timeStampForSendMessageListener;
	private String myAvatar;
	private String opponentAvatar;
	private NewChatUpdateReceiver newChatUpdateReceiver;
	private IntentFilter newChatUpdateFilter;
	private long myUserId;

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

		if (getArguments() != null) {
			gameId = getArguments().getLong(GAME_ID);
			opponentAvatar = getArguments().getString(OPPONENT_AVATAR);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
			opponentAvatar = savedInstanceState.getString(OPPONENT_AVATAR);
		}

		myAvatar = getAppData().getUserAvatar();
		newChatUpdateFilter = new IntentFilter(IntentConstants.NOTIFICATIONS_UPDATE);

		myUserId = getAppData().getUserId();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.chat_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.chat);

		widgetsInit(view);

		chatItems = new ArrayList<ChatItem>();
		receiveUpdateListener = new ReceiveChatItemsUpdateListener();
		sendUpdateListener = new SendChatItemsUpdateListener();
		timeStampForSendMessageListener = new TimeStampForSendMessageListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		updateList();

		newChatUpdateReceiver = new NewChatUpdateReceiver();
		registerReceiver(newChatUpdateReceiver, newChatUpdateFilter);

		DbDataManager.deleteNewChatMessageNotification(getContentResolver(), getUsername(), gameId);
		updateNotificationBadges();
	}

	@Override
	public void onPause() {
		super.onPause();

		unRegisterMyReceiver(newChatUpdateReceiver);

		// change softInputMode back
		if (isTablet) {
			AppUtils.changeSoftInputToResize(getActivity());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(GAME_ID, gameId);
		outState.putString(OPPONENT_AVATAR, opponentAvatar);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isTablet) {
			AppUtils.changeSoftInputToPan(getActivity());
		}
		return false;
	}

	private class NewChatUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			DbDataManager.deleteNewChatMessageNotification(getContentResolver(), getUsername(), gameId);
			updateNotificationBadges();

			updateList();
		}
	}

	protected void widgetsInit(View view) {
		sendEdt = (EditText) view.findViewById(R.id.sendEdt);
		sendEdt.setOnTouchListener(this);
		listView = (ListView) view.findViewById(R.id.listView);

		progressBar = view.findViewById(R.id.progressBar);

		sendBtn = (RoboButton) view.findViewById(R.id.sendBtn);
		sendBtn.setOnClickListener(this);
	}

	public void updateList() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAME_CHAT(gameId));
		loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<GamesChatItem>(receiveUpdateListener).executeTask(loadItem);
	}

	private class SendChatItemsUpdateListener extends ChessUpdateListener<DailyChatItem> {

		public SendChatItemsUpdateListener() {
			super(DailyChatItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
			sendBtn.setEnabled(!show);
		}

		@Override
		public void updateData(DailyChatItem returnedObj) {
			chatItems.clear();
			chatItems.addAll(returnedObj.getData());

			// manually add avatar urls
			for (ChatItem chatItem : chatItems) {
				if (chatItem.isMine()) {
					chatItem.setAvatar(myAvatar);
				} else {
					chatItem.setAvatar(opponentAvatar);
				}
			}

			if (messagesAdapter == null) {
				messagesAdapter = new ChatMessagesAdapter(getContext(), chatItems, getImageFetcher());
				listView.setAdapter(messagesAdapter);
			} else {
				messagesAdapter.setItemsList(chatItems);
			}
			sendEdt.setText(Symbol.EMPTY);
		}
	}

	private class ReceiveChatItemsUpdateListener extends ChessUpdateListener<GamesChatItem> {

		public ReceiveChatItemsUpdateListener() {
			super(GamesChatItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
			sendBtn.setEnabled(!show);
		}

		@Override
		public void updateData(GamesChatItem returnedObj) {
			chatItems.clear();
			for (GamesChatItem.Data data : returnedObj.getData()) {

				ChatItem chatItem = new ChatItem();

				if (myUserId == data.getUserId()) {
					chatItem.setAvatar(myAvatar);
					chatItem.setIsMine(true);
				} else {
					chatItem.setAvatar(opponentAvatar);
				}
				chatItem.setContent(data.getMessage());

				chatItems.add(chatItem);
			}

			if (messagesAdapter == null) {
				messagesAdapter = new ChatMessagesAdapter(getContext(), chatItems, getImageFetcher());
				listView.setAdapter(messagesAdapter);
			} else {
				messagesAdapter.setItemsList(chatItems);
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sendBtn) {
			LoadItem loadItem = LoadHelper.getGameById(getUserToken(), gameId);
			new RequestJsonTask<DailyCurrentGameItem>(timeStampForSendMessageListener).executeTask(loadItem);
		}
	}

	private class GetTimeStampListener extends ChessUpdateListener<DailyCurrentGameItem> { // TODO use batch API

		public GetTimeStampListener() {
			super(DailyCurrentGameItem.class);
		}

		@Override
		public void updateData(DailyCurrentGameItem returnedObj) {
			final DailyCurrentGameData currentGame = returnedObj.getData();
			timeStamp = currentGame.getTimestamp();
		}
	}

	private class TimeStampForSendMessageListener extends GetTimeStampListener {
		@Override
		public void updateData(DailyCurrentGameItem returnedObj) {
			super.updateData(returnedObj);

			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameId, RestHelper.V_CHAT, timeStamp);
			loadItem.addRequestParams(RestHelper.P_MESSAGE, getTextFromField(sendEdt));
			new RequestJsonTask<DailyChatItem>(sendUpdateListener).executeTask(loadItem);
		}
	}

}
