package com.chess.ui.fragments.daily;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.entity.api.daily_games.DailyChallengeItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.DataHolder;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.interfaces.GameFaceHelper;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardDailyView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsDailyView;
import com.chess.utilities.AppUtils;
import com.chess.widgets.ProfileImageView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.06.13
 * Time: 13:56
 */
public class DailyInviteFragment extends CommonLogicFragment {

	private static final String ERROR_TAG = "send request failed popup";
	protected static final String GAME_ITEM = "game item";
	protected int AVATAR_SIZE = 48;

	protected ControlsDailyView controlsView;
	protected PanelInfoGameView topPanelView;
	protected PanelInfoGameView bottomPanelView;
	private String[] countryNames;
	private int[] countryCodes;
	private ImageDownloaderToListener imageDownloader;
	private DailyChallengeItem.Data challengeItem;
	private GameBaseFragment.LabelsConfig labelsConfig;
	protected ProfileImageView topAvatarImg;
	protected ProfileImageView bottomAvatarImg;
	protected ChessBoardDailyView boardView;
	protected TextView inviteDetails1Txt;
	protected TextView inviteTitleTxt;
	private int successToastMsgId;
	private DailyUpdateListener challengeInviteUpdateListener;
	protected InviteGameFaceHelper gameFaceHelper;

	public DailyInviteFragment() {
	}

	public static DailyInviteFragment createInstance(DailyChallengeItem.Data challengeItem) {
		DailyInviteFragment fragment = new DailyInviteFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(GAME_ITEM, challengeItem);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			challengeItem = getArguments().getParcelable(GAME_ITEM);
		} else {
			challengeItem = savedInstanceState.getParcelable(GAME_ITEM);
		}

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.daily_invite_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (challengeItem.getGameTypeId() == RestHelper.V_GAME_CHESS_960) {
			setTitle(R.string.chess_960);
		} else {
			setTitle(R.string.daily);
		}

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		init();
		adjustBoardForGame();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(GAME_ITEM, challengeItem);
	}

	public void init() {
		Resources resources = getResources();
		gameFaceHelper = new InviteGameFaceHelper(getActivity());

		labelsConfig = new GameBaseFragment.LabelsConfig();
		challengeInviteUpdateListener = new DailyUpdateListener();

		imageDownloader = new ImageDownloaderToListener(getActivity());

		countryNames = resources.getStringArray(R.array.new_countries);
		countryCodes = resources.getIntArray(R.array.new_country_ids);
	}

	private void adjustBoardForGame() {
		DailyChallengeItem.Data currentGame = challengeItem;
		challengeItem.getOpponentDrawCount();
		challengeItem.getOpponentWinCount();
		challengeItem.getOpponentLossCount();
		challengeItem.isRated();
		challengeItem.getColor(); // TODO tell server to rename the same
		int daysPerMove = challengeItem.getDaysPerMove();

		{ // overlay fill
			inviteTitleTxt.setText(getString(R.string.vs) + Symbol.SPACE + challengeItem.getOpponentUsername()
					+ Symbol.SPACE + Symbol.wrapInPars(challengeItem.getOpponentRating()));

			String detailsStr1;
			if (challengeItem.isRated()) {
				detailsStr1 = getString(R.string.rated);
			} else {
				detailsStr1 = getString(R.string.unrated);
			}

			detailsStr1 += Symbol.wrapInPars("W +" + challengeItem.getOpponentWinCount()
					+ "/L -" + challengeItem.getOpponentLossCount() + "/D -"
					+ challengeItem.getOpponentDrawCount());

			detailsStr1 += Symbol.NEW_STR + daysPerMove + Symbol.SPACE + getString(R.string.days_move);
			String color = getString(R.string.random);
			if (challengeItem.getColor() == RestHelper.P_BLACK) {
				color = getString(R.string.black);
			} else if (challengeItem.getColor() == RestHelper.P_WHITE) {
				color = getString(R.string.white);
			}
			detailsStr1 += Symbol.NEW_STR + getString(R.string.i_play_as_) + Symbol.SPACE + color;

			inviteDetails1Txt.setText(detailsStr1);
		}

		labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		labelsConfig.topPlayerName = currentGame.getOpponentUsername();
		labelsConfig.topPlayerRating = String.valueOf(currentGame.getOpponentRating());
		labelsConfig.bottomPlayerName = getAppData().getUsername();
		labelsConfig.bottomPlayerRating = String.valueOf(getAppData().getUserDailyRating());
		labelsConfig.topPlayerAvatar = currentGame.getOpponentAvatar();
		labelsConfig.bottomPlayerAvatar = getAppData().getUserAvatar();

		DataHolder.getInstance().setInDailyGame(currentGame.getGameId(), true);

		controlsView.enableGameControls(true);

		topPanelView.setTimeRemain(AppUtils.getDaysString(daysPerMove, getActivity()));
		bottomPanelView.setTimeRemain(AppUtils.getDaysString(daysPerMove, getActivity()));
		topPanelView.showTimeLeftIcon(false);
		bottomPanelView.showTimeLeftIcon(false);

		bottomPanelView.setPlayerPremiumIcon(AppUtils.getPremiumIcon(getAppData().getUserPremiumStatus()));

		// set opponent data info
		topPanelView.setPlayerName(challengeItem.getOpponentUsername());
		topPanelView.setPlayerRating(String.valueOf(challengeItem.getOpponentRating()));

		// set user data info
		bottomPanelView.setPlayerName(getUsername());

		{ // get users info // TODO check info from invite item
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken());
			new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.CURRENT_USER)).executeTask(loadItem);
		}

		{ // get opponent info
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), labelsConfig.topPlayerName);
			new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.OPPONENT)).executeTask(loadItem);
		}

		imageDownloader.download(labelsConfig.topPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
		imageDownloader.download(labelsConfig.bottomPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);

		boardView.lockBoard(false);

	}


	private class GetUserUpdateListener extends ChessUpdateListener<UserItem> {

		static final int CURRENT_USER = 0;
		static final int OPPONENT = 1;

		private int itemCode;

		public GetUserUpdateListener(int itemCode) {
			super(UserItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);
			UserItem.Data userInfo = returnedObj.getData();
			if (itemCode == CURRENT_USER) {
				bottomPanelView.setPlayerRating(String.valueOf(userInfo.getPoints()));
				labelsConfig.bottomPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, userInfo.getCountryId());
				bottomPanelView.setPlayerFlag(labelsConfig.bottomPlayerCountry);
				bottomPanelView.setPlayerPremiumIcon(userInfo.getPremiumStatus());
			} else if (itemCode == OPPONENT) {
				labelsConfig.topPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, userInfo.getCountryId());
				topPanelView.setPlayerFlag(labelsConfig.topPlayerCountry);
				topPanelView.setPlayerPremiumIcon(userInfo.getPremiumStatus());
			}
		}
	}

	private void acceptChallenge() {
		LoadItem loadItem = LoadHelper.acceptChallenge(getUserToken(), challengeItem.getGameId());
		successToastMsgId = R.string.challenge_accepted;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	private void declineChallenge() {
		LoadItem loadItem = LoadHelper.declineChallenge(getUserToken(), challengeItem.getGameId());
		successToastMsgId = R.string.challenge_declined;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	private class DailyUpdateListener extends ChessLoadUpdateListener<BaseResponseItem> {

		public DailyUpdateListener() {
			super(BaseResponseItem.class);
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			showToast(successToastMsgId);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(ERROR_TAG)) {
			backToLoginFragment();
		}
		super.onPositiveBtnClick(fragment);
	}

	protected void widgetsInit(View view) {
		Resources resources = getResources();
		{ // invite overlay setup
			View inviteOverlay = view.findViewById(R.id.inviteOverlay);

			// let's make it to match board properties
			// it should be 2 squares inset from top of border and 4 squares tall + 1 squares from sides
			int squareSize = resources.getDisplayMetrics().widthPixels / 8; // one square size
//			int borderOffset = resources.getDimensionPixelSize(R.dimen.invite_overlay_top_offset);
			int borderOffset = 0;
			// now we add few pixel to compensate shadow addition
//			int shadowOffset = resources.getDimensionPixelSize(R.dimen.overlay_shadow_offset);
			int shadowOffset = 0;
			borderOffset += shadowOffset;
			int overlayHeight = squareSize * 4 + borderOffset + shadowOffset;
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					overlayHeight);
			int topMargin = squareSize * 2 + borderOffset - shadowOffset * 2;

			params.setMargins(squareSize - borderOffset, topMargin, squareSize - borderOffset, 0);
			params.addRule(RelativeLayout.ALIGN_TOP, R.id.boardView);
			params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.boardView);
			inviteOverlay.setLayoutParams(params);

			inviteDetails1Txt = (TextView) view.findViewById(R.id.inviteDetails1Txt);
			inviteTitleTxt = (TextView) view.findViewById(R.id.inviteTitleTxt);
		}
		controlsView = (ControlsDailyView) view.findViewById(R.id.controlsView);

		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ProfileImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ProfileImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

		controlsView.enableChatButton(true);
		controlsView.showSubmitButtons(true);
		boardView = (ChessBoardDailyView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsView);

		boardView.setGameFace(gameFaceHelper);
		boardView.lockBoard(true);
	}

	private class ImageUpdateListener extends ImageReadyListenerLight {

		private static final int TOP_AVATAR = 0;
		private static final int BOTTOM_AVATAR = 1;
		private int code;

		private ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}
			switch (code) {
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topAvatarImg.setUsername(labelsConfig.topPlayerName, DailyInviteFragment.this);
					topPanelView.invalidateMe();
					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomAvatarImg.setUsername(labelsConfig.bottomPlayerName, DailyInviteFragment.this);
					bottomPanelView.invalidateMe();
					break;
			}
		}
	}

	private class InviteGameFaceHelper extends GameFaceHelper {


		public InviteGameFaceHelper(Context context) {
			super(context);
		}

		@Override
		public void playMove() {
			acceptChallenge();
		}

		@Override
		public void cancelMove() {
			declineChallenge();
		}
	}
}
