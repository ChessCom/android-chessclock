package com.chess.ui.fragments.videos;

import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.VideoItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.utilities.AppUtils;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.07.13
 * Time: 20:41
 */
public class VideoDetailsCurriculumFragment extends VideoDetailsFragment {

	public static VideoDetailsCurriculumFragment createInstance4Curriculum(int videoId) {
		VideoDetailsCurriculumFragment frag = new VideoDetailsCurriculumFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(ITEM_ID, videoId);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	protected void updateData() {
		boolean videoViewed = DbDataManager.isVideoViewed(getActivity(), getUsername(), itemId);
		if (videoViewed) {
			playBtnTxt.setText(R.string.ic_check);
		} else {
			playBtnTxt.setText(R.string.ic_play);
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VIDEO_BY_ID(itemId));
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<VideoItem>(new VideoDetailsUpdateListener()).executeTask(loadItem);
	}

	private class VideoDetailsUpdateListener extends ChessLoadUpdateListener<VideoItem> {

		public VideoDetailsUpdateListener() {
			super(VideoItem.class);
		}

		@Override
		public void updateData(VideoItem returnedObj) {
			super.updateData(returnedObj);

			List<VideoItem.Data> dataList = returnedObj.getData();

			if(dataList.size() > 0) {
				playBtnTxt.setEnabled(true);

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
				videoUrl = videoData.getUrl();

				// Save to DB
				DbDataManager.saveVideoItem(getContentResolver(), videoData);

				currentPlayingId = (int) videoData.getVideoId();
			}
		}
	}

}
