package com.chess.ui.fragments.daily;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.EditText;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.NotesItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.widgets.RoboButton;


/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.01.14
 * Time: 6:38
 */
public class DailyNotesFragment extends CommonLogicFragment implements View.OnTouchListener {

	private static final String GAME_ID = "game_id";
	private long gameId;
	private EditText noteEdt;
	private View progressBar;
	private RoboButton sendBtn;
	private NotesItemUpdateListener notesItemUpdateListener;

	public DailyNotesFragment() {

	}

	public static DailyNotesFragment createInstance(long gameId) {
		DailyNotesFragment fragment = new DailyNotesFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, gameId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			gameId = getArguments().getLong(GAME_ID);
		} else {
			gameId = savedInstanceState.getLong(GAME_ID);
		}
		notesItemUpdateListener = new NotesItemUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.notes_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.notes);

		noteEdt = (EditText) view.findViewById(R.id.noteEdt);
		noteEdt.setOnTouchListener(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_cancel, true);
		getActivityFace().showActionMenu(R.id.menu_accept, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		showKeyBoard(noteEdt);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAME_NOTES(gameId));
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_CONTENT, getTextFromField(noteEdt));

		new RequestJsonTask<NotesItem>(notesItemUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onPause() {
		super.onPause();

		// change softInputMode back
		if (isTablet) {
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(GAME_ID, gameId);
	}

	public void updateNote() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAME_NOTES(gameId));
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_CONTENT, getTextFromField(noteEdt));

		new RequestJsonTask<NotesItem>(notesItemUpdateListener).executeTask(loadItem);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_cancel:
				// clear fields
				noteEdt.setText(Symbol.EMPTY);
				return true;
			case R.id.menu_accept:
				updateNote();
				hideKeyBoard(noteEdt);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (isTablet) {
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		}
		return false;
	}

	private class NotesItemUpdateListener extends ChessLoadUpdateListener<NotesItem> {

		public NotesItemUpdateListener() {
			super(NotesItem.class);
		}

		@Override
		public void updateData(NotesItem returnedObj) {
			String note = returnedObj.getData().getNotes();
			if (!TextUtils.isEmpty(note)) {
				noteEdt.setText(note);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.RESOURCE_NOT_FOUND) {
					// do nothing
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sendBtn) {

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_GAME_NOTES(gameId));
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

			new RequestJsonTask<NotesItem>(notesItemUpdateListener).executeTask(loadItem);
		}
	}

}
