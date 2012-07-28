package com.chess.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.SendLiveMessageTask;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.model.GameListItem;
import com.chess.model.MessageItem;
import com.chess.ui.adapters.MessagesAdapter;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;

public class ChatLiveActivity extends LiveBaseActivity implements LccChatMessageListener{

	private EditText sendEdt;
	private ListView chatListView;
	private MessagesAdapter messagesAdapter;
	private ArrayList<MessageItem> chatItems;
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
		chatItems = new ArrayList<MessageItem>();
		messageUpdateListener = new MessageUpdateListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getLccHolder().setLccChatMessageListener(this);
		updateList();
	}

	private void updateList(){
		chatItems.clear();
		chatItems.addAll(getLccHolder().getMessagesList(gameId));

		if (messagesAdapter == null) {
			messagesAdapter = new MessagesAdapter(ChatLiveActivity.this, R.layout.chat_item, chatItems);
			chatListView.setAdapter(messagesAdapter);
		} else {
			messagesAdapter.notifyDataSetInvalidated();
		}
		chatListView.post(new AppUtils.ListSelector((chatItems.size() - 1), chatListView));
	}

	@Override
	public void onMessageReceived(){
		int before = chatItems.size();
		if (before != chatItems.size()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateList();
				}
			});
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.send) {
			new SendLiveMessageTask(messageUpdateListener, getTextFromField(sendEdt)).execute(gameId);
			updateList();

			sendEdt.setText(StaticData.SYMBOL_EMPTY);
			hideKeyBoard(sendEdt);
		}
	}

	private class MessageUpdateListener extends ActionBarUpdateListener<String> {

		public MessageUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			messagesAdapter.notifyDataSetInvalidated();
		}
	}

}
