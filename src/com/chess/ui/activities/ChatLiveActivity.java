package com.chess.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameListItem;
import com.chess.model.MessageItem;
import com.chess.ui.adapters.MessagesAdapter;

import java.util.ArrayList;

public class ChatLiveActivity extends LiveBaseActivity {

	private EditText sendEdt;
	private ListView chatListView;
	private MessagesAdapter messages = null;
	private ArrayList<MessageItem> chatItems = new ArrayList<MessageItem>();
	private long gameId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_screen);

		sendEdt = (EditText) findViewById(R.id.sendText);
		chatListView = (ListView) findViewById(R.id.chatLV);
		findViewById(R.id.send).setOnClickListener(this);

		gameId = getIntent().getExtras().getLong(GameListItem.GAME_ID);
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
			onMessageReceived();
		}
	};

	public void onMessageReceived(){
		int before = chatItems.size();
		chatItems.clear();
		chatItems.addAll(LccHolder.getInstance(this).getMessagesList(gameId));
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
			new SendMessageTask().execute();

			onMessageSent();
		}
	}

	private void onMessageSent(){
		chatItems.clear();
		chatItems.addAll(LccHolder.getInstance(this).getMessagesList(gameId));
		if (messages == null) {
			messages = new MessagesAdapter(this, R.layout.chat_item, chatItems);
			chatListView.setAdapter(messages);
		} else {
			messages.notifyDataSetChanged();
		}
		sendEdt.setText(StaticData.SYMBOL_EMPTY);
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(sendEdt.getWindowToken(), 0);
		chatListView.setSelection(chatItems.size() - 1);
	}

	private class SendMessageTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			LccHolder.getInstance(getContext()).sendChatMessage(gameId, getTextFromField(sendEdt));
			return null;
		}
	}
}
