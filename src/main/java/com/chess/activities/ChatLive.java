package com.chess.activities;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.chess.R;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.*;
import com.chess.model.Message;
import com.chess.views.MessagesAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

public class ChatLive extends CoreActivity {
	public static int MESSAGE_RECEIVED = 0;
  public static int MESSAGE_SENT = 1;
  private EditText sendText;
	private ListView chatLV;
	private MessagesAdapter messages = null;
	private ArrayList<com.chess.model.Message> chatItems = new ArrayList<com.chess.model.Message>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		sendText = (EditText)findViewById(R.id.sendText);
		chatLV = (ListView)findViewById(R.id.chatLV);
		findViewById(R.id.send).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lccHolder.getClient().sendChatMessage(lccHolder.getGameChat(new Long(App.OnlineGame.values.get("game_id"))), sendText.getText().toString());
        Update(MESSAGE_SENT);
			}
		});
	}
	@Override
	public void LoadNext(int code) {
	}
	@Override
	public void LoadPrev(int code) {
		finish();
	}
	@Override
	public void Update(int code) {
		if (code == -1 || code == MESSAGE_RECEIVED)
    {
			int before = chatItems.size();
			chatItems.clear();
			chatItems.addAll(getMessagesList());
			if(before != chatItems.size()){
				if(messages == null){
					messages = new MessagesAdapter(ChatLive.this, R.layout.chat_item, chatItems);
					chatLV.setAdapter(messages);
				} else{
					messages.notifyDataSetChanged();
				}
				chatLV.setSelection(chatItems.size()-1);
			}
		}
    else if(code == MESSAGE_SENT)
    {
			chatItems.clear();
			chatItems.addAll(getMessagesList());
			if(messages == null){
				messages = new MessagesAdapter(ChatLive.this, R.layout.chat_item, chatItems);
				chatLV.setAdapter(messages);
			} else{
				messages.notifyDataSetChanged();
			}
			sendText.setText("");
			InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
	    chatLV.setSelection(chatItems.size()-1);
		}
	}

  private ArrayList<Message> getMessagesList(){
	  ArrayList<Message> output = new ArrayList<Message>();
    Long currentGameId = new Long(App.OnlineGame.values.get("game_id"));
    com.chess.live.client.Chat chat = lccHolder.getGameChat(currentGameId);
    if (chat == null)
    {
      throw new NullPointerException("game_id=" + currentGameId +
                                     " " + lccHolder.getCurrentGameId() +
                                     " " + lccHolder.getGame(currentGameId) +
                                     " " + lccHolder.getCurrentGameChatId());
    }
    LinkedHashMap<Long, ChatMessage> chatMessages = lccHolder.getChatMessages(chat.getId());
    if (chatMessages != null)
    {
      for (ChatMessage message : chatMessages.values())
      {
        output.add(new Message(message.getAuthor().getUsername().equals(lccHolder.getUser().getUsername()) ? "0" : "1", message.getMessage()));
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

  private BroadcastReceiver chatMessageReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      //LccHolder.LOG.info("ANDROID: receive broadcast intent, action=" + intent.getAction());
      App.OnlineGame.values.put("has_new_message", "0");
      Update(ChatLive.MESSAGE_RECEIVED);
    }
  };
}
