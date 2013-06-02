package com.chess.ui.activities.old;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.entity.new_api.ChatItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.adapters.ChatMessagesAdapter;
import com.chess.utilities.AppUtils;

import java.util.List;

public class ChatLiveActivity extends LiveBaseActivity implements LccChatMessageListener {

	private EditText sendEdt;
	private ListView listView;
	private ChatMessagesAdapter messagesAdapter;

	private MessageUpdateListener messageUpdateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_screen);

		sendEdt = (EditText) findViewById(R.id.sendEdt);
		listView = (ListView) findViewById(R.id.chatLV);
		findViewById(R.id.sendBtn).setOnClickListener(this);

		messageUpdateListener = new MessageUpdateListener();
	}

	@Override
	protected void onResume() {
		super.onResume();

		showKeyBoard(sendEdt);

		if (isLCSBound) {
			liveService.setLccChatMessageListener(this);
			updateList();
		}
	}

	@Override
	protected void onLiveServiceConnected() {
		super.onLiveServiceConnected();

		messagesAdapter = new ChatMessagesAdapter(ChatLiveActivity.this, liveService.getMessagesList());
		listView.setAdapter(messagesAdapter);


		showKeyBoard(sendEdt);
		liveService.setLccChatMessageListener(this);
		updateList();
	}

	private void updateList() {
		List<ChatItem> chatItems = liveService.getMessagesList();
		messagesAdapter.setItemsList(chatItems);
		listView.post(new AppUtils.ListSelector((chatItems.size() - 1), listView));
	}

	@Override
	public void onMessageReceived(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateList();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				updateList();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sendBtn) {
			if (isLCSBound) {
			// todo: refactor with new LCC
				if(!liveService.isConnected() || liveService.getClient() == null){ // TODO should leave that screen on connection lost or when LCC is become null
					liveService.logout();
//				backToHomeActivity();
					unBindLiveService();
				return;
			}

				liveService.sendMessage(getTextFromField(sendEdt),messageUpdateListener );

//			updateList();

			sendEdt.setText(StaticData.SYMBOL_EMPTY);
			}
		}
	}

	private class MessageUpdateListener extends ActionBarUpdateListener<String> {

		public MessageUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			messagesAdapter.setItemsList(liveService.getMessagesList());
		}
	}

}
