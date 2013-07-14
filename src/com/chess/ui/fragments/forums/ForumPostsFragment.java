package com.chess.ui.fragments.forums;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ForumPostItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.SaveForumPostsTask;
import com.chess.ui.adapters.ForumPostsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 6:43
 */
public class ForumPostsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {


	private static final String TOPIC_ID = "topic_id";
	private int topicId;
	private ForumPostsCursorAdapter postsCursorAdapter;
	private SavePostsListener savePostsListener;
	private boolean need2update = true;
	private PostsUpdateListener postsUpdateListener;

	public ForumPostsFragment() {

	}

	public static ForumPostsFragment createInstance(int topicId){
		ForumPostsFragment fragment = new ForumPostsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(TOPIC_ID, topicId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		postsCursorAdapter = new ForumPostsCursorAdapter(getActivity(), null);
		savePostsListener = new SavePostsListener();
		postsUpdateListener = new PostsUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_forum_posts_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.forums);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(postsCursorAdapter);
		listView.setOnItemClickListener(this);


		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null) {
			topicId = getArguments().getInt(TOPIC_ID);
		} else {
			topicId = savedInstanceState.getInt(TOPIC_ID);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (need2update) {
			LoadItem loadItem = LoadHelper.getForumPostssForTopic(getUserToken(), topicId, 0);

			new RequestJsonTask<ForumPostItem>(postsUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(TOPIC_ID, topicId);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	private class PostsUpdateListener extends ChessLoadUpdateListener<ForumPostItem>{

		private PostsUpdateListener() {
			super(ForumPostItem.class);
		}

		@Override
		public void updateData(ForumPostItem returnedObj) {
			new SaveForumPostsTask(savePostsListener, returnedObj.getData().getPosts(),
					getContentResolver(), topicId).executeTask();
		}
	}

	private class SavePostsListener extends ChessLoadUpdateListener<ForumPostItem.Post> {

		@Override
		public void updateData(ForumPostItem.Post returnedObj) {
			super.updateData(returnedObj);

			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getForumPostsParams());
			if (cursor.moveToFirst()) {
				postsCursorAdapter.changeCursor(cursor);
				postsCursorAdapter.notifyDataSetChanged();
			} else {
				showToast("Internal error");
			}

			need2update= false;
		}
	}

}
