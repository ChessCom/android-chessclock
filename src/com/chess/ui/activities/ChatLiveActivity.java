package com.chess.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.live.client.ChatMessage;
import com.chess.model.GameItem;
import com.chess.model.MessageItem;
import com.chess.ui.adapters.MessagesAdapter;
import com.chess.ui.core.IntentConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ChatLiveActivity extends LiveBaseActivity implements OnClickListener {
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
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);
	}

	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY || code == MESSAGE_RECEIVED) {
			int before = chatItems.size();
			chatItems.clear();
			chatItems.addAll(getMessagesList());
			if (before != chatItems.size()) {
				if (messages == null) {
					messages = new MessagesAdapter(ChatLiveActivity.this, R.layout.chat_item, chatItems);
					chatListView.setAdapter(messages);
				} else {
					messages.notifyDataSetChanged();
				}
				chatListView.setSelection(chatItems.size() - 1);
			}
		} else if (code == MESSAGE_SENT) {
			chatItems.clear();
			chatItems.addAll(getMessagesList());
			if (messages == null) {
				messages = new MessagesAdapter(ChatLiveActivity.this, R.layout.chat_item, chatItems);
				chatListView.setAdapter(messages);
			} else {
				messages.notifyDataSetChanged();
			}
			sendText.setText("");
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
			chatListView.setSelection(chatItems.size() - 1);
		}
	}

	private ArrayList<MessageItem> getMessagesList() {
		ArrayList<MessageItem> output = new ArrayList<MessageItem>();
		Long currentGameId = new Long(mainApp.getCurrentGameId() );
		com.chess.live.client.Chat chat = lccHolder.getGameChat(currentGameId);
		if (chat != null) {
			LinkedHashMap<Long, ChatMessage> chatMessages = lccHolder.getChatMessages(chat.getId());
			if (chatMessages != null) {
				for (ChatMessage message : chatMessages.values()) {
					output.add(new MessageItem(message.getAuthor().getUsername()
							.equals(lccHolder.getUser().getUsername()) ? "0" : "1", message.getMessage()));
				}
			}
		}
		return output;
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(chatMessageReceiver, new IntentFilter(IntentConstants.ACTION_GAME_CHAT_MSG));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(chatMessageReceiver);
	}

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
			mainApp.getCurrentGame().values.put(GameItem.HAS_NEW_MESSAGE, "0");
			update(ChatLiveActivity.MESSAGE_RECEIVED);
		}
	};

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.send) {
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... voids) {
					System.out.println("LCCLOG: SEND");
					lccHolder.getClient().sendChatMessage(lccHolder.getGameChat(
							new Long(mainApp.getCurrentGameId() )),
							sendText.getText().toString());
					return null;
				}
			}.execute();

			update(MESSAGE_SENT);
		}
	}
}
