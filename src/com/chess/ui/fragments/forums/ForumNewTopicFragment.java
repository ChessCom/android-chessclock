package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.SuccessItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbScheme;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.ui.adapters.WhiteSpinnerAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.07.13
 * Time: 14:31
 */
public class ForumNewTopicFragment extends CommonLogicFragment implements TextView.OnEditorActionListener {

	private ArrayList<String> categoriesList;
	private TopicsCreateListener topicsCreateListener;
	private EditText topicNameEdt;
	private Spinner categorySpinner;
	private EditText topicBodyEdt;
	private SparseArray<String> categoriesMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		topicsCreateListener = new TopicsCreateListener();

		categoriesList = new ArrayList<String>();
		categoriesMap = new SparseArray<String>();
		Cursor cursor = DbDataManager.executeQuery(getContentResolver(), DbHelper.getForumCategories());
		if (cursor.moveToFirst()) {
			do {
				categoriesList.add(DbDataManager.getString(cursor, DbScheme.V_NAME));
				categoriesMap.put(DbDataManager.getInt(cursor, DbScheme.V_ID), DbDataManager.getString(cursor, DbScheme.V_NAME));
			} while (cursor.moveToNext());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_new_forum_topic_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.new_topic);

		topicNameEdt = (EditText) view.findViewById(R.id.topicNameEdt);
		categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);
		categorySpinner.setAdapter(new WhiteSpinnerAdapter(getActivity(), categoriesList));
		categorySpinner.setSelection(0);
		topicBodyEdt = (EditText) view.findViewById(R.id.topicBodyEdt);
		topicBodyEdt.setOnEditorActionListener(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_cancel, true);
		getActivityFace().showActionMenu(R.id.menu_accept, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	private void createTopic() {
		String category = (String) categorySpinner.getSelectedItem();
		int categoryId = 0;
		for (int i= 0; i < categoriesMap.size(); i++){
			if (categoriesMap.valueAt(i).equals(category)){
				categoryId = categoriesMap.keyAt(i);
				break;
			}
		}


		String subject = getTextFromField(topicNameEdt);
		if (TextUtils.isEmpty(subject)) {
			topicNameEdt.requestFocus();
			topicNameEdt.setError(getString(R.string.can_not_be_empty));
			return;
		}

		String body = getTextFromField(topicBodyEdt);
		if (TextUtils.isEmpty(body)) {
			topicBodyEdt.requestFocus();
			topicBodyEdt.setError(getString(R.string.can_not_be_empty));
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_FORUMS_TOPICS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_FORUM_CATEGORY_ID, categoryId);
		loadItem.addRequestParams(RestHelper.P_SUBJECT, subject);
		loadItem.addRequestParams(RestHelper.P_BODY, body);

		new RequestJsonTask<SuccessItem>(topicsCreateListener).executeTask(loadItem);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.FLAG_EDITOR_ACTION
				|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			if (!AppUtils.isNetworkAvailable(getActivity())) { // check only if live
				popupItem.setPositiveBtnId(R.string.wireless_settings);
				showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
			} else {
				createTopic();
			}
		}
		return false;
	}

	private class TopicsCreateListener extends ChessLoadUpdateListener<SuccessItem> {

		private TopicsCreateListener() {
			super(SuccessItem.class);
		}

		@Override
		public void updateData(SuccessItem returnedObj) {
			if(returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
				showToast(R.string.topic_created);
			} else {
				showToast(R.string.error);
			}
		}
	}

}

