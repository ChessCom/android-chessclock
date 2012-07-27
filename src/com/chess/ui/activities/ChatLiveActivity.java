package com.chess.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.SendLiveMessageTask;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.model.GameListItem;
import com.chess.model.MessageItem;
import com.chess.ui.adapters.MessagesAdapter;

import java.util.ArrayList;

public class ChatLiveActivity extends LiveBaseActivity implements LccChatMessageListener{

	private EditText sendEdt;
	private ListView chatListView;
	private MessagesAdapter messagesAdapter = null;
	private ArrayList<MessageItem> chatItems = new ArrayList<MessageItem>();
	private Long gameId;
	private MessageUpdateListener messageUpdateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_screen);

		sendEdt = (EditText) findViewById(R.id.sendText);
		chatListView = (ListView) findViewById(R.id.chatLV);
		findViewById(R.id.send).setOnClickListener(this);

		gameId = getIntent().getExtras().getLong(GameListItem.GAME_ID);

		messageUpdateListener = new MessageUpdateListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getLccHolder().setLccChatMessageListener(this);
	}

	@Override
	public void onMessageReceived(){
		int before = chatItems.size();
		chatItems.clear();
		chatItems.addAll(LccHolder.getInstance(this).getMessagesList(gameId));
		if (before != chatItems.size()) {
			if (messagesAdapter == null) {
				messagesAdapter = new MessagesAdapter(this, R.layout.chat_item, chatItems);
				chatListView.setAdapter(messagesAdapter);
			} else {
				messagesAdapter.notifyDataSetChanged();
			}
			chatListView.setSelection(chatItems.size() - 1);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.send) {
			new SendLiveMessageTask(messageUpdateListener, getTextFromField(sendEdt)).execute(gameId);

            chatItems.clear();
            chatItems.addAll(LccHolder.getInstance(this).getMessagesList(gameId));

            if (messagesAdapter == null) {
                messagesAdapter = new MessagesAdapter(this, R.layout.chat_item, chatItems);
                chatListView.setAdapter(messagesAdapter);
            } else {
                messagesAdapter.notifyDataSetChanged();
            }

            sendEdt.setText(StaticData.SYMBOL_EMPTY);

            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(sendEdt.getWindowToken(), 0);
            chatListView.setSelection(chatItems.size() - 1);
		}
	}

	private class MessageUpdateListener extends ActionBarUpdateListener<String> {

		public MessageUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			messagesAdapter.notifyDataSetChanged();
		}
	}

}
