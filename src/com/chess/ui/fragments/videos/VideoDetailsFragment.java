package com.chess.ui.fragments.videos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class VideoDetailsFragment extends CommonLogicFragment implements ItemClickListenerFace {

	public static final String ITEM_ID = "item_id";
	public static final String MODE = "mode";
	public static final int CURRICULUM = 1;
	public static final String GREY_COLOR_DIVIDER = "##";

	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");


	private TextView authorTxt;
	private View loadingView;
	private TextView emptyView;
	private VideosCursorUpdateListener videosCursorUpdateListener;
	private ImageView videoBackImg;
	private View progressBar;
	private TextView titleTxt;
	private ImageView thumbnailAuthorImg;
	private ImageView countryImg;
	private TextView dateTxt;
	private TextView contextTxt;
	private Cursor loadedCursor;
	private ImageButton playBtn;

	public static VideoDetailsFragment createInstance4Curriculum(int videoId) {
		VideoDetailsFragment frag = new VideoDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(ITEM_ID, videoId);
		bundle.putInt(MODE, CURRICULUM);
		frag.setArguments(bundle);
		return frag;
	}

	public static VideoDetailsFragment createInstance(long videoId) {
		VideoDetailsFragment frag = new VideoDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(ITEM_ID, videoId);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_video_details_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.videos);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		videoBackImg = (ImageView) view.findViewById(R.id.videoBackImg);
		progressBar = view.findViewById(R.id.progressBar);
		titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		thumbnailAuthorImg = (ImageView) view.findViewById(R.id.thumbnailAuthorImg);
		countryImg = (ImageView) view.findViewById(R.id.countryImg);
		dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		contextTxt = (TextView) view.findViewById(R.id.contextTxt);
		authorTxt = (TextView) view.findViewById(R.id.authorTxt);

		playBtn = (ImageButton) view.findViewById(R.id.playBtn);
		playBtn.setOnClickListener(this);
		playBtn.setEnabled(false);

		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (getArguments() != null && getArguments().getInt(MODE) == CURRICULUM) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_VIDEO_ID, getArguments().getInt(ITEM_ID));

			new RequestJsonTask<VideoItem>(new VideoDetailsUpdateListener()).executeTask(loadItem);

		} else {
			init();

			loadFromDb();
		}
	}

	private void loadFromDb() {
		long itemId = getArguments().getLong(ITEM_ID);

		new LoadDataFromDbTask(videosCursorUpdateListener, DbHelper.getVideosListParams(),
				getContentResolver()).executeTask(itemId);
	}

	@Override
	public void onStop() {
		super.onStop();
		// TODO release resources
	}

	private void init() {

		videosCursorUpdateListener = new VideosCursorUpdateListener();
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.playBtn) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(DBDataManager.getString(loadedCursor, DBConstants.V_URL)), "video/*");
			startActivity(intent);
		}
	}

	private class VideoDetailsUpdateListener extends ChessUpdateListener<VideoItem> {

		public VideoDetailsUpdateListener() {
			super(VideoItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingProgress(show);
		}

		@Override
		public void updateData(VideoItem returnedObj) {
			super.updateData(returnedObj);

			List<VideoItem.Data> dataList = returnedObj.getData();

			if(dataList.size() > 0) {
				playBtn.setEnabled(true);

				VideoItem.Data videoData = dataList.get(0);

				int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);

				String firstName = videoData.getFirstName();
				CharSequence chessTitle = videoData.getChessTitle();
				String lastName = videoData.getLastName();
				CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
						+ StaticData.SYMBOL_SPACE + firstName + StaticData.SYMBOL_SPACE + lastName;
				authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, new ForegroundColorSpan(lightGrey));
				authorTxt.setText(authorStr);

//			videoBackImg // TODO adjust image loader
//			progressBar // TODO adjust image loader

				titleTxt.setText(videoData.getTitle());
//			thumbnailAuthorImg // TODO adjust image loader
				countryImg.setImageDrawable(AppUtils.getUserFlag(getActivity())); // TODO set flag properly // invent flag resources set system

				int duration = videoData.getMinutes();
				dateTxt.setText(dateFormatter.format(new Date(videoData.getCreateDate()))
						+ StaticData.SYMBOL_SPACE + getString(R.string.min_arg, duration));

				contextTxt.setText(videoData.getDescription());
			}


		}
	}

	private class VideosCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor cursor) {
			super.updateData(cursor);

			loadedCursor = cursor;

			playBtn.setEnabled(true);

			int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
			String firstName = DBDataManager.getString(cursor, DBConstants.V_FIRST_NAME);
			CharSequence chessTitle = DBDataManager.getString(cursor, DBConstants.V_CHESS_TITLE);
			String lastName = DBDataManager.getString(cursor, DBConstants.V_LAST_NAME);
			CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ StaticData.SYMBOL_SPACE + firstName + StaticData.SYMBOL_SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, new ForegroundColorSpan(lightGrey));
			authorTxt.setText(authorStr);

//			videoBackImg // TODO adjust image loader
//			progressBar // TODO adjust image loader

			titleTxt.setText(DBDataManager.getString(cursor, DBConstants.V_TITLE));
//			thumbnailAuthorImg // TODO adjust image loader
			countryImg.setImageDrawable(AppUtils.getUserFlag(getActivity())); // TODO set flag properly // invent flag resources set system

			int duration = DBDataManager.getInt(cursor, DBConstants.V_MINUTES);
			dateTxt.setText(dateFormatter.format(new Date(DBDataManager.getLong(cursor, DBConstants.V_CREATE_DATE)))
			 + StaticData.SYMBOL_SPACE + getString(R.string.min_arg, duration));

			contextTxt.setText(DBDataManager.getString(cursor, DBConstants.V_DESCRIPTION));

		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText("No Videos"); // TODO remove after debug, there should be videos
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	private void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

			emptyView.setVisibility(View.VISIBLE);
//			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
//			listView.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
//			if (videosCursorAdapter.getCount() == 0) {
//				listView.setVisibility(View.GONE);
//
//			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
//			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
