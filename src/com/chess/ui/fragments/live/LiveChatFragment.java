package com.chess.ui.fragments.live;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.entity.api.ChatItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ChatMessagesAdapter;
import com.chess.ui.fragments.LiveBaseFragment;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.05.13
 * Time: 5:04
 */
public class LiveChatFragment extends LiveBaseFragment implements LccChatMessageListener {

	private EditText sendEdt;
	private ListView listView;
	private ChatMessagesAdapter messagesAdapter;
	private MessageUpdateListener messageUpdateListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.chat_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		sendEdt = (EditText) view.findViewById(R.id.sendEdt);
		listView = (ListView) view.findViewById(R.id.listView);
		view.findViewById(R.id.sendBtn).setOnClickListener(this);

		messageUpdateListener = new MessageUpdateListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		showKeyBoard(sendEdt);

		if (isLCSBound) {
			LiveConnectionHelper liveHelper;
			try {
				liveHelper = getLiveHelper();
				liveHelper.setLccChatMessageListener(this);
				messagesAdapter = new ChatMessagesAdapter(getActivity(), liveHelper.getMessagesList(), getImageFetcher());
				listView.setAdapter(messagesAdapter);

				showKeyBoard(sendEdt);
				updateList();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
			}
		}
	}

	private void updateList() throws DataNotValidException {
		LiveConnectionHelper liveHelper = getLiveHelper();
		List<ChatItem> chatItems = liveHelper.getMessagesList();
		messagesAdapter.setItemsList(chatItems);
		listView.post(new AppUtils.ListSelector((chatItems.size() - 1), listView));
	}

	@Override
	public void onMessageReceived() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					updateList();
				} catch (DataNotValidException e) {
					logTest(e.getMessage());
				}
			}
		});
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sendBtn) {
			if (isLCSBound) {

				LiveConnectionHelper liveHelper;
				try {
					liveHelper = getLiveHelper();
				} catch (DataNotValidException e) {
					logTest(e.getMessage());
					getActivityFace().showPreviousFragment();
					return;
				}
				liveHelper.sendMessage(getTextFromField(sendEdt), messageUpdateListener);

				sendEdt.setText(Symbol.EMPTY);
			}
		}
	}

	private class MessageUpdateListener extends ActionBarUpdateListener<String> {

		public MessageUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			LiveConnectionHelper liveHelper;
			try {
				liveHelper = getLiveHelper();
			} catch (DataNotValidException e) {
				logTest(e.getMessage());
				return;
			}
			messagesAdapter.setItemsList(liveHelper.getMessagesList());
		}
	}
}
