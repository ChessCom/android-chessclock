package com.chess.ui.fragments.videos;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.VideoViewedItem;
import com.chess.backend.statics.StaticData;
import com.chess.db.DbScheme;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.QueryParams;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class VideoDetailsFragment extends CommonLogicFragment {

	public static final String ITEM_ID = "item_id";

	public static final String GREY_COLOR_DIVIDER = "##";
	private static final int WATCH_VIDEO_REQUEST = 9897;

	// 11/15/12 | 27 min
	protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");

	protected View loadingView;
	protected TextView emptyView;

	protected TextView authorTxt;
	protected ImageView videoBackImg;
	protected ImageView thumbnailAuthorImg;
	protected TextView titleTxt;
	protected ImageView countryImg;
	protected TextView dateTxt;
	protected TextView contextTxt;
	protected TextView playBtnTxt;
	protected long itemId;
	protected String videoUrl;
	protected int currentPlayingId;

	public static VideoDetailsFragment createInstance(long videoId) {
		VideoDetailsFragment frag = new VideoDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(ITEM_ID, (int) videoId);
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
//		progressBar = view.findViewById(R.id.progressBar);
		titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		thumbnailAuthorImg = (ImageView) view.findViewById(R.id.thumbnailAuthorImg);
		countryImg = (ImageView) view.findViewById(R.id.countryImg);
		dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		contextTxt = (TextView) view.findViewById(R.id.contextTxt);
		authorTxt = (TextView) view.findViewById(R.id.authorTxt);

		playBtnTxt = (TextView) view.findViewById(R.id.playBtn);
		playBtnTxt.setOnClickListener(this);
		playBtnTxt.setEnabled(false);

		// adjust action bar
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null) {
			itemId = getArguments().getLong(ITEM_ID);
		} else {
			itemId = savedInstanceState.getLong(ITEM_ID);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		updateData();
	}



	protected void updateData() {
		QueryParams queryParams = DbHelper.getVideosList();
		Uri uri = ContentUris.withAppendedId(queryParams.getUri(), itemId);
		queryParams.setUri(uri);
		Cursor cursor = DbDataManager.executeQuery(getContentResolver(), queryParams);
		cursor.moveToFirst();

		playBtnTxt.setEnabled(true);

		int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
		String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
		CharSequence chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
		String lastName = DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
		CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
				+ StaticData.SYMBOL_SPACE + firstName + StaticData.SYMBOL_SPACE + lastName;
		authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, new ForegroundColorSpan(lightGrey));
		authorTxt.setText(authorStr);

//			videoBackImg // TODO adjust image loader
//			progressBar // TODO adjust image loader

		titleTxt.setText(DbDataManager.getString(cursor, DbScheme.V_TITLE));
//			thumbnailAuthorImg // TODO adjust image loader
		countryImg.setImageDrawable(AppUtils.getUserFlag(getActivity())); // TODO set flag properly // invent flag resources set system

		int duration = DbDataManager.getInt(cursor, DbScheme.V_MINUTES);
		dateTxt.setText(dateFormatter.format(new Date(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE)))
				+ StaticData.SYMBOL_SPACE + getString(R.string.min_arg, duration));

		contextTxt.setText(DbDataManager.getString(cursor, DbScheme.V_DESCRIPTION));
		videoUrl = DbDataManager.getString(cursor, DbScheme.V_URL);

		currentPlayingId =  DbDataManager.getInt(cursor, DbScheme.V_ID);

		boolean videoViewed = DbDataManager.isVideoViewed(getActivity(), getUsername(), currentPlayingId);
		if (videoViewed) {
			playBtnTxt.setText(R.string.ic_check);
		} else {
			playBtnTxt.setText(R.string.ic_play);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == WATCH_VIDEO_REQUEST) {

			VideoViewedItem item = new VideoViewedItem(currentPlayingId, getUsername(), true);
			DbDataManager.updateVideoViewedState(getContentResolver(), item);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(ITEM_ID, itemId);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.playBtn) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(videoUrl), "video/*");
			startActivityForResult(Intent.createChooser(intent, getString(R.string.select_player)), WATCH_VIDEO_REQUEST);
		}
	}

}
