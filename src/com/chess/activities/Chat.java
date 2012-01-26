package com.chess.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MyProgressDialog;
import com.chess.views.MessagesAdapter;

public class Chat extends CoreActivity {
	private EditText sendText;
	private ListView chatListView;
	private MessagesAdapter messages = null;
	private final ArrayList<com.chess.model.Message> chatItems = new ArrayList<com.chess.model.Message>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		sendText = (EditText) findViewById(R.id.sendText);
		chatListView = (ListView) findViewById(R.id.chatLV);
		findViewById(R.id.send).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = "";
				try {
					message = URLEncoder.encode(sendText.getText().toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					Log.e("Chat", e.toString()); 	// TODO handle exception
													// correctly
				}

				if (appService != null) {
					appService.RunSingleTask(
							1,
							"http://www." + LccHolder.HOST + "/api/submit_echess_action?id="
									+ mainApp.getSharedData().getString("user_token", "") + "&chessid="
									+ extras.getString(AppConstants.GAME_ID) + "&command=CHAT&message=" + message + "&timestamp="
									+ extras.getString(AppConstants.TIMESTAMP),
							progressDialog = new MyProgressDialog(ProgressDialog.show(Chat.this, null,
									getString(R.string.sendingmessage), true)));
				}
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
		if (code == -1) {
			if (appService != null) {
				appService.RunRepeatbleTask(0, 0, 60000, "http://www." + LccHolder.HOST
						+ "/api/submit_echess_action?id=" + mainApp.getSharedData().getString("user_token", "") + "&chessid="
						+ extras.getString(AppConstants.GAME_ID) + "&command=CHAT&timestamp=" + extras.getString(AppConstants.TIMESTAMP),
						null/*
							 * progressDialog = MyProgressDialog.show(Chat.this, null,
							 * getString(R.string.gettingmessages), true)
							 */
				);
			}
		} else if (code == 0) {
			int before = chatItems.size();
			chatItems.clear();
			chatItems.addAll(ChessComApiParser.ReciveMessages(responseRepeatable));
			if (before != chatItems.size()) {
				if (messages == null) {
					messages = new MessagesAdapter(Chat.this, R.layout.chat_item, chatItems);
					chatListView.setAdapter(messages);
				} else {
					messages.notifyDataSetChanged();
				}
				chatListView.setSelection(chatItems.size() - 1);
			}
		} else if (code == 1) {
			chatItems.clear();
			chatItems.addAll(ChessComApiParser.ReciveMessages(response));
			if (messages == null) {
				messages = new MessagesAdapter(Chat.this, R.layout.chat_item, chatItems);
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
}
