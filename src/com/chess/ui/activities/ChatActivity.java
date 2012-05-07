package com.chess.ui.activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.ChatItem;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.tasks.GetCustomObjTask;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameListItem;
import com.chess.model.MessageItem;
import com.chess.ui.adapters.MessagesAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MyProgressDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ChatActivity extends LiveBaseActivity implements OnClickListener {
	public static int MESSAGE_RECEIVED = 0;
	public static int MESSAGE_SENT = 1;
	private EditText sendText;
	private ListView chatListView;
	private MessagesAdapter messages = null;
	private ArrayList<MessageItem> chatItems = new ArrayList<MessageItem>();
    private AsyncTask<LoadItem, Void, Integer> submitDataTask;
    private ChatUpdateListener chatUpdateListener;
    private View progressBar;
    private Button sendBtn;
    private long gameId;
    private String timeStamp;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_screen);

        widgetsInit();

        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.cancel(R.string.you_got_new_msg);

        chatUpdateListener = new ChatUpdateListener();
        gameId = extras.getLong(GameListItem.GAME_ID);
        timeStamp = extras.getString(GameListItem.TIMESTAMP);
	}

    @Override
    protected void widgetsInit(){

        sendText = (EditText) findViewById(R.id.sendText);
        chatListView = (ListView) findViewById(R.id.chatLV);
        
        progressBar = findViewById(R.id.progressBar);

        sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initActivity();
    }

    public void initActivity(){
        // submit echess action
//        appService.RunRepeatableTask(MESSAGE_RECEIVED, 0, 60000, "http://www." + LccHolder.HOST
//                + AppConstants.API_SUBMIT_ECHESS_ACTION_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
//                + AppConstants.CHESSID_PARAMETER +
//                + "&command=CHAT&timestamp=" + extras.getString(GameListItem.TIMESTAMP),
//                null);
        LoadItem loadItem = new LoadItem();
        loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
        loadItem.addRequestParams(RestHelper.P_ID, mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY));
        loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameId));
        loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_CHAT);
        loadItem.addRequestParams(RestHelper.P_TIMESTAMP, timeStamp);
        submitDataTask = new GetCustomObjTask<ChatItem>(chatUpdateListener).execute(loadItem);
	}

	public void onMessageReceived(){

	}

	public void onMessageSent(){

	}

    private class ChatUpdateListener extends AbstractUpdateListener<ChatItem>{

        public ChatUpdateListener() {
            super(coreContext);
        }

        @Override
        public void showProgress(boolean show) {
            // TODO show progress on send button
            progressBar.setVisibility(show? View.VISIBLE: View.INVISIBLE);
            sendBtn.setEnabled(!show);
        }


    }

	@Override
	public void update(int code) { // TODO Replace with named methods calls
		if (code == INIT_ACTIVITY) {
			if (appService != null) {
				appService.RunRepeatableTask(MESSAGE_RECEIVED, 0, 60000, "http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
                        + AppConstants.CHESSID_PARAMETER + extras.getLong(GameListItem.GAME_ID)
                        + "&command=CHAT&timestamp=" + extras.getString(GameListItem.TIMESTAMP),
						null);
			}
		} else if (code == MESSAGE_RECEIVED) {
			int before = chatItems.size();
			chatItems.clear();
			chatItems.addAll(ChessComApiParser.receiveMessages(responseRepeatable));
			if (before != chatItems.size()) {
				if (messages == null) {
					messages = new MessagesAdapter(ChatActivity.this, R.layout.chat_item, chatItems);
					chatListView.setAdapter(messages);
				} else {
					messages.notifyDataSetChanged();
				}
				chatListView.setSelection(chatItems.size() - 1);
			}
		} else if (code == MESSAGE_SENT) {
			chatItems.clear();
			chatItems.addAll(ChessComApiParser.receiveMessages(response));

			if (messages == null) {
				messages = new MessagesAdapter(ChatActivity.this, R.layout.chat_item, chatItems);
				chatListView.setAdapter(messages);
			} else {
				messages.notifyDataSetChanged();
			}
			sendText.setText(AppConstants.SYMBOL_EMPTY);
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
			chatListView.setSelection(chatItems.size() - 1);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.send) {
			String message = AppConstants.SYMBOL_EMPTY;
			try {
				message = URLEncoder.encode(sendText.getText().toString(), AppConstants.UTF_8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.e("Chat", e.toString());	 // TODO handle exception
				// correctly
			}

			if (appService != null) {
				// Use rest-helper and async task
//				LoadItem loadItem = new LoadItem();  // TODO
//				loadItem.setLoadPath(RestHelper.SUBMIT_ECHESS_ACTION);
//				loadItem.addRequestParams(RestHelper.P_ID, AppData.getInstance(this).getUserToken());
//				loadItem.addRequestParams(RestHelper.P_CHESSID, AppData.getInstance(this).getUserToken());

				String query = "http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
						+ AppConstants.CHESSID_PARAMETER + extras.getLong(GameListItem.GAME_ID)
						+ "&command=CHAT&message=" + message
						+ AppConstants.TIMESTAMP_PARAMETER + extras.getString(GameListItem.TIMESTAMP);
				appService.RunSingleTask(MESSAGE_SENT, query,
						progressDialog = new MyProgressDialog(ProgressDialog.show(ChatActivity.this, null,
								getString(R.string.sendingmessage), true)));
			}
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        submitDataTask.cancel(true);
    }
}
