package com.chess.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.ui.adapters.MessagesAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.live.client.ChatMessage;
import com.chess.model.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ChatLiveActivity extends CoreActivityActionBar implements OnClickListener {
	public static int MESSAGE_RECEIVED = 0;
	public static int MESSAGE_SENT = 1;
	private EditText sendText;
	private ListView chatLV;
	private MessagesAdapter messages = null;
	private ArrayList<Message> chatItems = new ArrayList<Message>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		sendText = (EditText) findViewById(R.id.sendText);
		chatLV = (ListView) findViewById(R.id.chatLV);
		findViewById(R.id.send).setOnClickListener(this);
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
					chatLV.setAdapter(messages);
				} else {
					messages.notifyDataSetChanged();
				}
				chatLV.setSelection(chatItems.size() - 1);
			}
		} else if (code == MESSAGE_SENT) {
			chatItems.clear();
			chatItems.addAll(getMessagesList());
			if (messages == null) {
				messages = new MessagesAdapter(ChatLiveActivity.this, R.layout.chat_item, chatItems);
				chatLV.setAdapter(messages);
			} else {
				messages.notifyDataSetChanged();
			}
			sendText.setText("");
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
			chatLV.setSelection(chatItems.size() - 1);
		}
	}

	private ArrayList<Message> getMessagesList() {
		ArrayList<Message> output = new ArrayList<Message>();
		Long currentGameId = new Long(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID));
		com.chess.live.client.Chat chat = lccHolder.getGameChat(currentGameId);
		if (chat != null) {
			LinkedHashMap<Long, ChatMessage> chatMessages = lccHolder.getChatMessages(chat.getId());
			if (chatMessages != null) {
				for (ChatMessage message : chatMessages.values()) {
					output.add(new Message(message.getAuthor().getUsername()
							.equals(lccHolder.getUser().getUsername()) ? "0" : "1", message.getMessage()));
				}
			}
		}
		return output;
	}

	protected void onResume() {
		super.onResume();
		registerReceiver(chatMessageReceiver, new IntentFilter("com.chess.lcc.android-game-chat-message"));
	}

	protected void onPause() {
		super.onPause();
		unregisterReceiver(chatMessageReceiver);
	}

	private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
			mainApp.getCurrentGame().values.put("has_new_message", "0");
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
							new Long(mainApp.getCurrentGame().values.get(AppConstants.GAME_ID))),
							sendText.getText().toString());
					return null;
				}
			}.execute();

			update(MESSAGE_SENT);
		}
	}
}
