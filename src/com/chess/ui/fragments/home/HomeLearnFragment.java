package com.chess.ui.fragments.home;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.backend.entity.api.LessonsRatingItem;
import com.chess.backend.entity.api.VideoSingleItem;
import com.chess.backend.entity.api.VideosItem;
import com.chess.backend.entity.api.stats.TacticsBasicStatsItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.lessons.GameLessonFragment;
import com.chess.ui.fragments.lessons.GameLessonsFragmentTablet;
import com.chess.ui.fragments.lessons.LessonsFragment;
import com.chess.ui.fragments.lessons.LessonsFragmentTablet;
import com.chess.ui.fragments.stats.StatsGameTacticsFragment;
import com.chess.ui.fragments.tactics.GameTacticsFragment;
import com.chess.ui.fragments.videos.VideoDetailsFragment;
import com.chess.ui.fragments.videos.VideosFragment;
import com.chess.ui.fragments.videos.VideosFragmentTablet;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.04.13
 * Time: 5:43
 */
public class HomeLearnFragment extends CommonLogicFragment {

	public static final String GREY_COLOR_DIVIDER = "##";
	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

	private TextView tacticsRatingTxt;
	private TextView lessonsRatingTxt;
	private TextView avgScoreValueTxt;
	private TextView todaysAttemptsValueTxt;
	private long loadedVideoId;
	private StatsUpdateListener statsUpdateListener;
	private VideosItemUpdateListener videosItemUpdateListener;
	private View headerView;
	private ViewHolder holder;
	private ForegroundColorSpan foregroundSpan;
	private TextView lessonTitleTxt;
	private LessonsRatingUpdateListener lessonsRatingUpdateListener;
	private LessonSingleItem incompleteLesson;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);
		statsUpdateListener = new StatsUpdateListener();
		lessonsRatingUpdateListener = new LessonsRatingUpdateListener();
		videosItemUpdateListener = new VideosItemUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_learn_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.tacticsHeaderView).setOnClickListener(this);
		view.findViewById(R.id.lessonsHeaderView).setOnClickListener(this);
		view.findViewById(R.id.videosHeaderView).setOnClickListener(this);
		view.findViewById(R.id.startTacticsBtn).setOnClickListener(this);
		view.findViewById(R.id.startLessonsBtn).setOnClickListener(this);
		view.findViewById(R.id.videoThumbItemView).setOnClickListener(this);

		tacticsRatingTxt = (TextView) view.findViewById(R.id.tacticsRatingTxt);

		// Tactics
		avgScoreValueTxt = (TextView) view.findViewById(R.id.avgScoreValueTxt);
		todaysAttemptsValueTxt = (TextView) view.findViewById(R.id.todaysAttemptsValueTxt);

		// Lessons
		lessonTitleTxt = (TextView) view.findViewById(R.id.lessonTitleTxt);
		lessonsRatingTxt = (TextView) view.findViewById(R.id.lessonsRatingTxt);
		if (!isNeedToUpgrade()) {
			lessonsRatingTxt.setText(String.valueOf(getAppData().getUserLessonsRating()));
		}


		// Videos
		headerView = view.findViewById(R.id.videoThumbItemView);
		holder = new ViewHolder();
		holder.titleTxt = (TextView) headerView.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) headerView.findViewById(R.id.authorTxt);
		holder.dateTxt = (TextView) headerView.findViewById(R.id.dateTxt);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Get Tactics basic stats
		LoadItem loadItem = LoadHelper.getTacticsBasicStats(getUserToken());
		new RequestJsonTask<TacticsBasicStatsItem>(statsUpdateListener).executeTask(loadItem);

		loadVideoToHeader();

		List<LessonSingleItem> incompleteLessons = DbDataManager.getIncompleteLessons(getContentResolver(), getUsername());
		if (incompleteLessons != null) {
			int last = incompleteLessons.size() - 1;
			incompleteLesson = incompleteLessons.get(last);
			lessonTitleTxt.setText(incompleteLesson.getName());
		} else {
			lessonTitleTxt.setText(R.string.improve_your_chess);
		}
	}

	private void loadVideoToHeader() {
		if (DbDataManager.haveSavedVideos(getActivity())) {
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getVideosList());

			cursor.moveToFirst();
			loadedVideoId = DbDataManager.getLong(cursor, DbScheme.V_ID);
			String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
			String chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
			String lastName = DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
			CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + Symbol.SPACE
					+ firstName + Symbol.SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
			holder.authorTxt.setText(authorStr);
			holder.titleTxt.setText(DbDataManager.getString(cursor, DbScheme.V_TITLE));
			holder.dateTxt.setText(dateFormatter.format(new Date(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE))));

			headerView.invalidate();
		} else {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEOS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_LIMIT, 1);

			new RequestJsonTask<VideosItem>(videosItemUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.tacticsHeaderView) {
			getActivityFace().openFragment(new StatsGameTacticsFragment());
		} else if (id == R.id.lessonsHeaderView) {
			if (!isTablet) {
				getActivityFace().openFragment(new LessonsFragment());
			} else {
				getActivityFace().openFragment(new LessonsFragmentTablet());
			}
		} else if (id == R.id.videosHeaderView) {
			if (!isTablet) {
				getActivityFace().openFragment(new VideosFragment());
			} else {
				getActivityFace().openFragment(new VideosFragmentTablet());
			}
		} else if (id == R.id.startTacticsBtn) {
			getActivityFace().openFragment(new GameTacticsFragment());
		} else if (id == R.id.startLessonsBtn) {
			if (incompleteLesson != null) {
				int lessonId = incompleteLesson.getId();
				long courseId = incompleteLesson.getCourseId();

				if (!isTablet) {
					getActivityFace().openFragment(GameLessonFragment.createInstance(lessonId, courseId));
				} else {
					getActivityFace().openFragment(GameLessonsFragmentTablet.createInstance(lessonId, courseId));
				}
			} else {
				if (!isTablet) {
					getActivityFace().openFragment(new LessonsFragment());
				} else {
					getActivityFace().openFragment(new LessonsFragmentTablet());
				}
			}
		} else if (id == R.id.videoThumbItemView) {
			getActivityFace().openFragment(VideoDetailsFragment.createInstance(loadedVideoId));
		}
	}


	private class StatsUpdateListener extends ChessUpdateListener<TacticsBasicStatsItem> {

		public StatsUpdateListener() {
			super(TacticsBasicStatsItem.class);
		}

		@Override
		public void updateData(TacticsBasicStatsItem returnedObj) {
			super.updateData(returnedObj);

			int currentTacticsRating = returnedObj.getData().getCurrent();
			int tacticsTodaysAttempts = returnedObj.getData().getTodaysAttempts();
			int todaysAverageScore = returnedObj.getData().getTodaysAverageScore();

			tacticsRatingTxt.setText(String.valueOf(currentTacticsRating));
			todaysAttemptsValueTxt.setText(String.valueOf(tacticsTodaysAttempts));
			avgScoreValueTxt.setText(String.valueOf(todaysAverageScore) + Symbol.PERCENT);

			if (getAppData().getUserPremiumStatus() > StaticData.GOLD_USER) {
				// Load lessons ratings
				LoadItem loadItem = LoadHelper.getLessonsRating(getUserToken());
				new RequestJsonTask<LessonsRatingItem>(lessonsRatingUpdateListener).executeTask(loadItem);
			} else {
				lessonsRatingTxt.setText(Symbol.EMPTY);
			}
		}
	}

	private class LessonsRatingUpdateListener extends ChessUpdateListener<LessonsRatingItem> {

		private LessonsRatingUpdateListener() {
			super(LessonsRatingItem.class);
		}

		@Override
		public void updateData(LessonsRatingItem returnedObj) {
			super.updateData(returnedObj);

			LessonsRatingItem.Data lessonsRating = returnedObj.getData();
			lessonsRatingTxt.setText(String.valueOf(lessonsRating.getRating()));

			getAppData().setUserLessonsRating(lessonsRating.getRating());
			getAppData().setUserLessonsCompleteCnt(lessonsRating.getCompletedLessons());
			getAppData().setUserCourseCompleteCnt(lessonsRating.getCompletedCourses());
		}
	}


	private class VideosItemUpdateListener extends ChessUpdateListener<VideosItem> {

		public VideosItemUpdateListener() {
			super(VideosItem.class);
		}

		@Override
		public void updateData(VideosItem returnedObj) {
			VideoSingleItem.Data headerData = returnedObj.getData().get(0);

			// save in Db to open in Details View
			ContentResolver contentResolver = getContentResolver();

			Uri uri = DbScheme.uriArray[DbScheme.Tables.VIDEOS.ordinal()];
			String[] arguments = new String[1];
			arguments[0] = String.valueOf(headerData.getTitle());
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_TITLE,
					DbDataManager.SELECTION_TITLE, arguments, null);

			ContentValues values = DbDataManager.putVideoItemToValues(headerData);

			if (cursor != null && cursor.moveToFirst()) {
				loadedVideoId = DbDataManager.getId(cursor);
				contentResolver.update(ContentUris.withAppendedId(uri, loadedVideoId), values, null, null);
			} else {
				Uri savedUri = contentResolver.insert(uri, values);
				loadedVideoId = Long.parseLong(savedUri.getPathSegments().get(1));
			}

			String firstName = headerData.getFirstName();
			String chessTitle = headerData.getChessTitle();
			String lastName = headerData.getLastName();
			CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + Symbol.SPACE
					+ firstName + Symbol.SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
			holder.authorTxt.setText(authorStr);
			holder.titleTxt.setText(headerData.getTitle());
			holder.dateTxt.setText(dateFormatter.format(new Date(headerData.getCreateDate())));

			headerView.invalidate();
		}
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView dateTxt;
	}
}
