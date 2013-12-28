package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.SuccessItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.utilities.AppUtils;
import com.chess.widgets.RoboButton;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.11.13
 * Time: 22:26
 */
public class ThemeManagerFragment extends CommonLogicFragment {

	public static final String CLOUD_FRONT = "http://d1xrj4tlyewhek.cloudfront.net/";
	private static final String PNG = ".png";
	private static final String ZIP = ".zip";
	public static final String BACKGROUNDS = "backgrounds/";
	public static final String BOARDS = "boards/";
	public static final String PIECES = "pieces/";
	public static final String SOUNDS = "sounds/";
	public static final String ORIGINALS = "originals/";
	public static final String PREVIEWS = "_previews_/";
	static final String MP3 = "_MP3_/";
	static final String BACKGROUND_ORIGINALS = CLOUD_FRONT + BACKGROUNDS + ORIGINALS;
	static final String BACKGROUND_PREVIEWS = CLOUD_FRONT + BACKGROUNDS + PREVIEWS;
	static final String LINE = "line/";
	static final String SQUARE = "square/";
	static final String BOARD_PREVIEW_LINE = CLOUD_FRONT + BOARDS + PREVIEWS + LINE;
	static final String BOARD_PREVIEW_SQUARE = CLOUD_FRONT + BOARDS + PREVIEWS + SQUARE;
	static final String PIECE_PREVIEW_LINE = CLOUD_FRONT + PIECES + PREVIEWS + LINE;
	static final String PIECE_PREVIEW_SQUARE = CLOUD_FRONT + PIECES + PREVIEWS + SQUARE;
	static final String SOUND_PACK = CLOUD_FRONT + SOUNDS + MP3;
	public static final int RENAME_DELAY = 20 * 1000;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_lessons_upgrade_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		TextView lessonsUpgradeMessageTxt = (TextView) view.findViewById(R.id.lessonsUpgradeMessageTxt);
		lessonsUpgradeMessageTxt.setText("Upload Resources!");
		RoboButton upgradeBtn = (RoboButton) view.findViewById(R.id.upgradeBtn);
		upgradeBtn.setText("Upload");
		upgradeBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		uploadResources();
	}

	private void uploadResources() {

		final String userToken = getUserToken();

		// -------------------------------------
		// Before usage uncomment needed parts!
		// -------------------------------------
		updateBackgrounds(userToken);
//		updateBoards(userToken);
//		updatePieces(userToken);
//		updateSounds(userToken);
//		updateThemes(userToken);

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				changeNamesForResources(userToken);
			}
		}, RENAME_DELAY);
	}

	private void changeNamesForResources(String userToken) {
		{// manually change name for Game Room
			LoadItem loadItem = new LoadItem();

			loadItem.setLoadPath(RestHelper.getInstance().CMD_THEME_BY_ID(themeIds[0]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_THEME_NAME, "Game Room");

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
		// ---------------------- Pieces ----------------------------------------
		{ // Game_room
			LoadItem loadItem = new LoadItem();

			loadItem.setLoadPath(RestHelper.getInstance().CMD_PIECES_BY_ID(piecesIds[5]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, "Game Room");

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
		{// 3d_staunton
			LoadItem loadItem = new LoadItem();

			loadItem.setLoadPath(RestHelper.getInstance().CMD_PIECES_BY_ID(piecesIds[11]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, "3D Staunton");

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
		{// 3d_plastic
			LoadItem loadItem = new LoadItem();

			loadItem.setLoadPath(RestHelper.getInstance().CMD_PIECES_BY_ID(piecesIds[12]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, "3D Wood");

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
		{// 3d_wood
			LoadItem loadItem = new LoadItem();

			loadItem.setLoadPath(RestHelper.getInstance().CMD_PIECES_BY_ID(piecesIds[13]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, "3D Plastic");

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
		{// 3d_chesskid
			LoadItem loadItem = new LoadItem();

			loadItem.setLoadPath(RestHelper.getInstance().CMD_PIECES_BY_ID(piecesIds[31]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, "3D Chesskid");

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
		// ---------------------- Boards ----------------------------------------
		{// burled_wood
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_BOARD_BY_ID(boardIds[3]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, "Burled Wood");

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
		{// dark_wood
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_BOARD_BY_ID(boardIds[4]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, "Dark Wood");

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
	}

	private void updateThemes(String userToken) {
		for (int i = 0; i < themes.length; i++) {
			LoadItem loadItem = new LoadItem();
			ThemeUploadItem uploadItem = new ThemeUploadItem(i);

			loadItem.setLoadPath(RestHelper.getInstance().CMD_THEME_BY_ID(themeIds[i]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_THEME_NAME, uploadItem.themeName);
			loadItem.addRequestParams(P_PIECES_PREVIEW_URL, uploadItem.piecesPreviewUrl);
			loadItem.addRequestParams(P_BACKGROUND_PREVIEW_URL, uploadItem.backgroundPreviewUrl);
			loadItem.addRequestParams(P_BOARD_BACKGROUND_URL, uploadItem.boardBackgroundUrl);
			loadItem.addRequestParams(P_BOARD_PREVIEW_URL, uploadItem.boardPreviewUrl);
			loadItem.addRequestParams(P_BACKGROUND_ID, uploadItem.backgroundId);
			loadItem.addRequestParams(P_BOARD_ID, uploadItem.boardId);
			loadItem.addRequestParams(P_PIECES_ID, uploadItem.piecesId);
			loadItem.addRequestParams(P_SOUNDS_ID, uploadItem.soundsId);

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
	}

	private void updateSounds(String userToken) {
		for (int i = 0; i < sounds.length; i++) {
			LoadItem loadItem = new LoadItem();
			SoundUploadItem uploadItem = new SoundUploadItem(i);

			loadItem.setLoadPath(RestHelper.getInstance().CMD_SOUND_BY_ID(soundIds[i]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, uploadItem.name);
			loadItem.addRequestParams(P_SOUNDPACK_ZIP, uploadItem.soundPackZip);
			loadItem.addRequestParams(P_THEME_ID, uploadItem.themeId);

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
	}

	private void updatePieces(String userToken) {
		for (int i = 0; i < pieces.length; i++) {
			LoadItem loadItem = new LoadItem();
			PieceUploadItem uploadItem = new PieceUploadItem(i);

			loadItem.setLoadPath(RestHelper.getInstance().CMD_PIECES_BY_ID(piecesIds[i]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, uploadItem.name);
			loadItem.addRequestParams(P_PIECE_PREVIEW_URL, uploadItem.piecePreviewUrl);
			loadItem.addRequestParams(P_THEME_DIR, uploadItem.themeDir);
			loadItem.addRequestParams(P_THEME_ID, uploadItem.themeId);

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
	}

	private void updateBoards(String userToken) {
		for (int i = 0; i < boards.length; i++) {
			LoadItem loadItem = new LoadItem();
			BoardUploadItem uploadItem = new BoardUploadItem(i);

			loadItem.setLoadPath(RestHelper.getInstance().CMD_BOARD_BY_ID(boardIds[i]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, uploadItem.name);
			loadItem.addRequestParams(P_BOARD_PREVIEW_URL, uploadItem.boardPreviewUrl);
			loadItem.addRequestParams(P_LINE_BOARD_PREVIEW, uploadItem.lineBoardPreview);
			loadItem.addRequestParams(P_COORDINATE_COLOR_LIGHT, uploadItem.coordinateColorLight);
			loadItem.addRequestParams(P_COORDINATE_COLOR_DARK, uploadItem.coordinateColorDark);
			loadItem.addRequestParams(P_HIGHLIGHT_COLOR, uploadItem.highlightColor);
			loadItem.addRequestParams(P_THEME_DIR, uploadItem.themeDir);
			loadItem.addRequestParams(P_THEME_ID, uploadItem.themeId);

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
	}

	private void updateBackgrounds(String userToken) {
		for (int i = 0; i < backgrounds.length; i++) {
			LoadItem loadItem = new LoadItem();
			BackgroundUploadItem uploadItem = new BackgroundUploadItem(i);

			loadItem.setLoadPath(RestHelper.getInstance().CMD_BACKGROUND_BY_ID(backgroundIds[i]));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, userToken);
			loadItem.addRequestParams(P_NAME, uploadItem.name);
			loadItem.addRequestParams(P_BACKGROUND_PREVIEW_URL, uploadItem.backgroundPreviewUrl);
			loadItem.addRequestParams(P_FONT_COLOR, uploadItem.fontColor);
			loadItem.addRequestParams(P_ORIGINAL_HANDSET, uploadItem.originalHandset);
			loadItem.addRequestParams(P_ORIGINAL_IPHONE, uploadItem.originalIphone);
			loadItem.addRequestParams(P_ORIGINAL_IPAD, uploadItem.originalIpad);
			loadItem.addRequestParams(P_ORIGINAL_IPAD_PORT, uploadItem.originalIpadPort);
			loadItem.addRequestParams(P_ORIGINAL_TABLET, uploadItem.originalTablet);
			loadItem.addRequestParams(P_THEME_ID, uploadItem.themeId);

			new RequestJsonTask<SuccessItem>(new CommentPostListener()).executeTask(loadItem);
		}
	}

	private class CommentPostListener extends ChessLoadUpdateListener<SuccessItem> {

		private CommentPostListener() {
			super(SuccessItem.class);
		}

		@Override
		public void updateData(SuccessItem returnedObj) {
			if (!returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
				showToast(R.string.error);
			}
		}
	}

	private static class BackgroundUploadItem {
		String name;
		String backgroundPreviewUrl;
		String fontColor;
		String originalHandset;
		String originalIphone;
		String originalIpad;
		String originalIpadPort;
		String originalTablet;
		int themeId;

		public BackgroundUploadItem(int position) {
			String themeName = backgrounds[position];
			name = AppUtils.upCaseFirst(themes[position]);
			backgroundPreviewUrl = BACKGROUND_PREVIEWS + themeName + PNG;
			fontColor = backgroundFontColors[position];
			originalHandset = BACKGROUND_ORIGINALS + backgroundsFolders[HANDSET] + themeName + PNG;
			originalIphone = BACKGROUND_ORIGINALS + backgroundsFolders[IPHONE] + themeName + PNG;
			originalIpad = BACKGROUND_ORIGINALS + backgroundsFolders[IPAD_LAND] + themeName + PNG;
			originalIpadPort = BACKGROUND_ORIGINALS + backgroundsFolders[IPAD_PORT] + themeName + PNG;
			originalTablet = BACKGROUND_ORIGINALS + backgroundsFolders[TABLET] + themeName + PNG;
			themeId = themeIds[position];
		}
	}

	private static class BoardUploadItem {
		String name;
		String boardPreviewUrl;
		String lineBoardPreview;
		String coordinateColorLight;
		String coordinateColorDark;
		String highlightColor;
		String themeDir;
		int themeId;

		public BoardUploadItem(int position) {
			name = AppUtils.upCaseFirst(boards[position]);
			themeDir = boards[position];
			boardPreviewUrl = BOARD_PREVIEW_SQUARE + boards[position] + PNG;
			lineBoardPreview = BOARD_PREVIEW_LINE + boards[position] + PNG;
			coordinateColorLight = boardColors[position][C_LIGHT];
			coordinateColorDark = boardColors[position][C_DARK];
			highlightColor = boardColors[position][C_HIGHLIGHT];
//			themeId = themeIds[position];
		}
	}


	/*
		name					false	Board name.
		piecePreviewUrl			false	Piece preview url.
		themeId					false	Theme id.
		themeDir				false	Theme Dir
	*/
	private static class PieceUploadItem {
		String name;
		String piecePreviewUrl;
		String themeDir;
		int themeId;

		public PieceUploadItem(int position) {
			name = AppUtils.upCaseFirst(pieces[position]);
			themeDir = pieces[position];
			piecePreviewUrl = PIECE_PREVIEW_LINE + pieces[position] + PNG;
//			themeId = themeIds[position];
		}
	}

	private static class SoundUploadItem {
		String name;
		String soundPackZip;
		int themeId;

		public SoundUploadItem(int position) {
			name = AppUtils.upCaseFirst(sounds[position]);
			soundPackZip = SOUND_PACK + sounds[position] + ZIP;
//			themeId = themeIds[position];
		}
	}

	private static class ThemeUploadItem {
		String piecesPreviewUrl;
		String boardBackgroundUrl;
		String backgroundPreviewUrl;
		String boardPreviewUrl;
		String themeName;
		int backgroundId;
		int boardId;
		int piecesId;
		int soundsId;


		public ThemeUploadItem(int position) {
			themeName = AppUtils.upCaseFirst(themes[position]);
			piecesPreviewUrl = PIECE_PREVIEW_SQUARE + resourceIdMap[position][T_SOUND_NAME] + PNG;
			backgroundPreviewUrl = BACKGROUND_PREVIEWS + resourceIdMap[position][T_THEME_NAME] + PNG;
			boardBackgroundUrl = BOARD_PREVIEW_SQUARE + resourceIdMap[position][T_BOARD_NAME] + PNG;
			boardPreviewUrl = BOARD_PREVIEW_SQUARE + resourceIdMap[position][T_BOARD_NAME] + PNG;
			backgroundId = Integer.parseInt(resourceIdMap[position][T_BACKGROUND_ID]);
			boardId = Integer.parseInt(resourceIdMap[position][T_BOARD_ID]);
			piecesId = Integer.parseInt(resourceIdMap[position][T_PIECES_ID]);
			soundsId = Integer.parseInt(resourceIdMap[position][T_SOUNDS_ID]);

		}
	}

	private static final int T_THEME_NAME = 0;
	private static final int T_BOARD_NAME = 1;
	private static final int T_SOUND_NAME = 2;
	private static final int T_BACKGROUND_ID = 3;
	private static final int T_BOARD_ID = 4;
	private static final int T_PIECES_ID = 5;
	private static final int T_SOUNDS_ID = 6;

	// theme name, board, piece,          backgroundId,boardId,pieceId, soundId
	private static final String[][] resourceIdMap = new String[][]{
			{"game_room", 	"dark_wood", 	"game_room", "5", "6",   "8", "-1"},
			{"dark", 		"green", 		"dark", 	 "6", "2",   "11", "-1"},
			{"light", 		"light", 		"light", 	 "7", "21",  "10", "-1"},
			{"wood", 		"dark_wood", 	"game_room", "8", "6",   "8", "-1"},
			{"glass", 		"glass", 		"glass", 	 "9", "14",  "9", "-1"},
			{"tournament", 	"tournament", 	"tournament","10", "8",  "7", "-1"},
			{"staunton", 	"burled_wood", 	"3d_staunton","11", "5",  "12", "-1"},
			{"newspaper", 	"newspaper", 	"newspaper", "12", "13", "15", "-1"},
			{"tigers", 		"parchment", 	"tigers", 	 "13", "9",  "16", "-1"},
			{"nature", 		"translucent", 	"nature", 	 "14", "10", "17", "2"},
			{"sky", 		"sky", 			"sky", 		 "15", "11", "19", "-1"},
			{"space", 		"translucent", 	"space", 	 "16", "10", "20", "5"},
			{"ocean", 		"sand", 		"ocean", 	 "17", "12", "22", "-1"},
			{"metal", 		"metal", 		"metal", 	 "18", "16", "24", "3"},
			{"gothic", 		"stone", 		"gothic", 	 "19", "17", "26", "-1"},
			{"marble", 		"marble", 		"marble", 	 "20", "7",  "28", "4"},
			{"neon", 		"neon", 		"neon", 	 "21", "15", "30", "-1"},
			{"graffiti", 	"graffiti", 	"graffiti",  "22", "18", "32", "6"},
			{"bubblegum", 	"bubblegum", 	"bubblegum", "23", "19", "34", "-1"},
			{"lolz", 		"lolz", 		"lolz", 	 "24", "20", "36", "8"}
	};


	static final int HANDSET = 0;
	static final int IPAD_LAND = 1;
	static final int IPAD_PORT = 2;
	static final int IPHONE = 3;
	static final int TABLET = 4;

	static String[] backgroundsFolders = new String[]{
			"Android_720x1280/",
			"iPad_Landscape_2x/",
			"iPad_Portrait_2x/",
			"iPhone_5_2x/",
			"Web_1440x900/"
	};

/*

	This is correct order for Themes and Backgrounds
	------------
	Game Room
	Dark
	Light
	Wood
	Glass
	Tournament
	Staunton
	Newspaper
	Tigers
	Nature
	Sky
	Space
	Ocean
	Metal
	Gothic
	Marble
	Neon
	Graffiti
	Bubblegum
	Lolz
*/

	static int[] themeIds = new int[]{
			2,
			5,
			6,
			7,
			8,
			9,
			10,
			11,
			12,
			13,
			14,
			15,
			16,
			17,
			18,
			19,
			20,
			21,
			22,
			23,
	};

	static String[] themes = new String[]{
			"game room",
			"dark",
			"light",
			"wood",
			"glass",
			"tournament",
			"staunton",
			"newspaper",
			"tigers",
			"nature",
			"sky",
			"space",
			"ocean",
			"metal",
			"gothic",
			"marble",
			"neon",
			"graffiti",
			"bubblegum",
			"lolz"
	};


	static int[] backgroundIds = new int[]{
			5,
			6,
			7,
			8,
			9,
			10,
			11,
			12,
			13,
			14,
			15,
			16,
			17,
			18,
			19,
			20,
			21,
			22,
			23,
			24
	};

	static int[] boardIds = new int[]{
			2,    // "green",          0
			3,    // "brown",          1
			4,    // "blue",           2
			5,    // "burled_wood",    3
			6,    // "dark_wood",      4
			7,    // "marble",         5
			8,    // "tournament",     6
			9,    // "parchment",      7
			10,   // "translucent",    8
			11,   // "sky",            9
			12,   // "sand",           10
			13,   // "newspaper",      11
			14,   // "glass",          12
			15,   // "neon",           13
			16,   // "metal",          14
			17,   // "stone",          15
			18,   // "graffiti",       16
			19,   // "bubblegum",      17
			20,   // "lolz",           18
			21,   // "light",          19
			22,   // "tan",            20
			24,   // "purple",         21
			26,   // "red",            22
			28    // "orange",         23
	};

	static int[] piecesIds = new int[]{
			1,  // 0
			2,  // 1
			3,  // 2
			4,  // 3
			5,  // 4
			6,  // 5
			7,  // 6
			8,  // 7
			9,  // 8
			10,  // 9
			11,  // 10
			12,  // 11
			13,  // 12
			14,  // 13
			15,  // 14
			16,  // 15
			17,  // 16
			19,  // 17
			20,  // 18
			22,  // 19
			24,  // 20
			26,  // 21
			28,  // 22
			30,  // 23
			32,  // 24
			34,  // 25
			36,  // 26
			38,  // 27
			40,  // 28
			42,  // 29
			44,  // 30
			46,  // 31
			48,  // 32
	};


	private static final int C_LIGHT = 0;
	private static final int C_DARK = 1;
	private static final int C_HIGHLIGHT = 2;

	//       light     dark      highlight
	static String[][] boardColors = new String[][]{
			{"edeed1", "779952", "ffff33"},  // Green         2
			{"edeed1", "779952", "ffff33"},  // Brown         3
			{"edeed1", "779952", "73bbee"},  // Blue          4
			{"d9b088", "895132", "ee9016"},  // Burled Wood   5
			{"ccb07a", "765331", "e7af4e"},  // Dark Wood     6
			{"c7bdaa", "706b66", "f0db86"},  // Marble        7
			{"ebece8", "316549", "a4c25b"},  // Tournament    8
			{"e5e1cb", "b59d64", "d8cc66"},  // Parchment     9
			{"282f3f", "667188", "5b91b3"},  // Translucent   10
			{"efefef", "c2d7e2", "65daf7"},  // Sky           11
			{"e5d3c4", "b8a590", "e2bc87"},  // Sand          12
			{"5a5956", "5a5956", "99976e"},  // Newspaper     13
			{"282f3f", "667188", "5b91b3"},  // Glass         14
			{"b9b9b9", "636363", "6d90a6"},  // Neon          15
			{"c9c9c9", "6e6e6e", "a3becd"},  // Metal         16
			{"c8c3bd", "666463", "36525f"},  // Stone         17
			{"aeaeae", "b96f18", "f39011"},  // Graffiti      18
			{"fff3f3", "f9cdd3", "de5d6f"},  // Bubblegum     19
			{"e0e9e9", "909898", "a3becd"},  // Lolz          20
			{"dcdcdc", "aaaaaa", "a4b8c4"},  // Light         21
			{"edeed1", "779952", "f7d84a"},  // Tan           24
			{"edeed1", "779952", "a4b8c4"},  // Purple        25
			{"edeed1", "779952", "f8f893"},  // Red           26
			{"edeed1", "779952", "ffff33"}   // Orange        27

	};


/*
			highlight

grdefault - ffff33
girlie - 	de5d6f
sky - 		65daf7

			dark    light
green/def - 779952, edeed1
girlie: 	f9cdd3, fff3f3
sky - 		c2d7e2, efefef


olive - 	ffff33
wood mid - 	eecf40
wood light -f0e464
wood dark - ec9f44
winboard - 	ffffff





*/

	static String[] backgrounds = new String[]{
			"game_room",
			"dark",
			"light",
			"wood",
			"glass",
			"tournament",
			"staunton",
			"newspaper",
			"tigers",
			"nature",
			"sky",
			"space",
			"ocean",
			"metal",
			"gothic",
			"marble",
			"neon",
			"graffiti",
			"bubblegum",
			"lolz"
	};

	static String[] backgroundFontColors = new String[]{
			"FFFFFFBF",   // "game_room",
			"FFFFFFBF",        // "dark",
			"606060BF",       // "light",
			"FFFFFFBF",        // "wood",
			"FFFFFFBF",       // "glass",
			"FFFFFFBF",  // "tournament",
			"FFFFFFBF",    // "staunton",
			"FFFFFFBF",   // "newspaper",
			"FFFFFFBF",      // "tigers",
			"FFFFFFBF",      // "nature",
			"FFFFFFBF",         // "sky",
			"FFFFFFBF",       // "space",
			"FFFFFFBF",       // "ocean",
			"FFFFFFBF",       // "metal",
			"FFFFFFBF",      // "gothic",
			"FFFFFFBF",      // "marble",
			"FFFFFFBF",        // "neon",
			"FFFFFFBF",    // "graffiti",
			"FFFFFFBF",   // "bubblegum",
			"FFFFFFBF"         // "lolz"
	};


	static String[] boards = new String[]{
			"green",         // 2,     0
			"brown",         // 3,     1
			"blue",          // 4,     2
			"burled_wood",   // 5,     3
			"dark_wood",     // 6,     4
			"marble",        // 7,     5
			"tournament",    // 8,     6
			"parchment",     // 9,     7
			"translucent",   // 10,    8
			"sky",           // 11,    9
			"sand",          // 12,    10
			"newspaper",     // 13,    11
			"glass",         // 14,    12
			"neon",          // 15,    13
			"metal",         // 16,    14
			"stone",         // 17,    15
			"graffiti",      // 18,    16
			"bubblegum",     // 19,    17
			"lolz",          // 20,    18
			"light",         // 21,    19
			"tan",           // 22,    20
			"purple",        // 24,    21
			"red",           // 26,    22
			"orange",        // 28     23
	};

	static String[] pieces = new String[]{
			"classic",        // 1
			"alpha",          // 2
			"modern",         // 3
			"book",           // 4
			"club",           // 5
			"game_room",      // 6
			"tournament",     // 7
			"wood",           // 8
			"glass",          // 9
			"light",          // 10
			"dark",           // 11
			"3d_staunton",    // 12
			"3d_wood",        // 13
			"3d_plastic",     // 14
			"newspaper",      // 15
			"tigers",         // 16
			"nature",         // 17
			"sky",            // 19
			"space",          // 20
			"ocean",          // 22
			"metal",          // 24
			"gothic",         // 26
			"marble",         // 28
			"neon",           // 30
			"graffiti",       // 32
			"bubblegum",      // 34
			"lolz",           // 36
			"cases",          // 38
			"condal",         // 40
			"vintage",        // 42
			"maya",           // 44
			"3d_chesskid",    // 46
			"blindfold"       // 48
	};


	static int[] soundIds = new int[]{
			1,  // "default",
			2,  // "nature",
			3,  // "metal",
			4,  // "marble",
			5,  // "space",
			6,  // "beat",
			7,  // "silly",
			8   // "cat"
	};

	static String[] sounds = new String[]{
			"default",
			"nature",
			"metal",
			"marble",
			"space",
			"beat",
			"silly",
			"lolz"
	};

/*
Pieces

	Classic
	Alpha
	Modern
	Book
	Club
	Game Room
	Tournament
	Wood
	Glass
	Light
	Dark
	3D - Wood
	3D - Plastic
	Newspaper
	Tigers
	Nature
	Sky
	Space
	Ocean
	Metal
	Gothic
	Marble
	Neon
	Graffiti
	Bubblegum
	Lolz
	Cases
	Condal
	Vintage
	Maya
	3D - ChessKid
	Blindfold
*/

/*
Boards

	Green 		  2
	Brown         3
	Blue          4
	Burled Wood   5
	Dark Wood     6
	Marble        7
	Tournament    8
	Parchment     9
	Translucent   10
	Sky           11
	Sand          12
	Newspaper     13
	Glass         14
	Neon          15
	Metal         16
	Stone         17
	Graffiti      18
	Bubblegum     19
	Lolz          20
	Light         21
	Light Wood    22
	Olive Wood    23
	Tan           24
	Purple        25
	Red           26
	Orange        27

*/

/*
Backgrounds

	Game Room
	Dark
	Light
	Wood
	Glass
	Tournament
	Staunton
	Newspaper
	Tigers
	Nature
	Sky
	Space
	Ocean
	Metal
	Gothic
	Marble
	Neon
	Graffiti
	Bubblegum
	Lolz

*/

/*
	Sounds list order
	------------------
	Default
	Nature
	Metal
	Marble
	Space
	Beat
	Silly
	Cat
*/

/*

	// --------------------------------------------
	//	Backgrounds
	// --------------------------------------------
	Name				Requirement	Type	Description

	backgroundId		\d+

	Parameters

	Parameter			Type	Required?	Description

	name						false	Background name.
	backgroundPreviewUrl		false	Background preview url.
	fontColor					false	Font color.
	originalHandset				false	Original handset image.
	originalIphone				false	Original iphone image.
	originalIpad				false	Original ipad image.
	originalIpadPort			false	Original ipad portrait image.
	originalTablet				false	Original tablet image.
	themeId						true	Theme Id.

*/

	// Backgrounds params
	public static final String P_NAME = "name";
	public static final String P_BACKGROUND_PREVIEW_URL = "backgroundPreviewUrl";
	public static final String P_FONT_COLOR = "fontColor";
	public static final String P_ORIGINAL_HANDSET = "originalHandset";
	public static final String P_ORIGINAL_IPHONE = "originalIphone";
	public static final String P_ORIGINAL_IPAD = "originalIpad";
	public static final String P_ORIGINAL_IPAD_PORT = "originalIpadPort";
	public static final String P_ORIGINAL_TABLET = "originalTablet";
	public static final String P_THEME_ID = "themeId";

/*
	//----------------------------------
	// Boards
	//----------------------------------
	Requirements
	Name				Requirement	Type	Description

	boardId	\d+

	Parameters

	Parameter			Type	Required?	Description

	name						false	Board name.
	boardPreviewUrl				false	Board preview background theme url.
	lineBoardPreview			false	Line board preview.
	coordinateColorLight		false	Coordinate color.
	coordinateColorDark			false	Coordinate color.
	highlightColor				false	Highlight color.
	themeId						false	Theme id.
	themeDir					false	Theme Dir

*/


	// Boards params
	public static final String P_BOARD_PREVIEW_URL = "boardPreviewUrl";
	public static final String P_LINE_BOARD_PREVIEW = "lineBoardPreview";
	public static final String P_COORDINATE_COLOR_LIGHT = "coordinateColorLight";
	public static final String P_COORDINATE_COLOR_DARK = "coordinateColorDark";
	public static final String P_HIGHLIGHT_COLOR = "highlightColor";
	public static final String P_THEME_DIR = "themeDir";

/*

	//----------------------------------
	// Pieces
	//----------------------------------
	Requirements

	Name	Requirement	Type	Description

	pieceId	\d+

	Parameters

	Parameter		Type	Required?	Description

	name					false	Board name.
	piecePreviewUrl			false	Piece preview url.
	themeId					false	Theme id.
	themeDir				false	Theme Dir
*/

	// Themes params
	public static final String P_PIECE_PREVIEW_URL = "piecePreviewUrl";


/*
	//----------------------------------
	// Sounds
	//----------------------------------
	Requirements
	Name	Requirement	Type	Description
	soundId				\d+

	Parameters

	Parameter	Type	Required?	Description
	name				false	Sound name.
	soundPackZip		false	Sound pack zip.
	themeId				false	Theme Id.

*/

	public static final String P_SOUNDPACK_ZIP = "soundPackZip";

/*

	//----------------------------------
	// Themes
	//----------------------------------

	Requirements

	Name				Requirement	Type	Description

	themeId	\d+

	Parameters

	Parameter			Type	Required?	Description

	piecesPreviewUrl			false	Pieces preview url.
	boardBackgroundUrl			false	Board background theme url.
	backgroundPreviewUrl		false	Background preview url.
	boardPreviewUrl				false	Board preview background theme url.
	themeName					false	Theme name.
	backgroundId				false	Background Id.
	boardId						false	Board Id.
	piecesId					false	Pieces Id.
	soundsId					false	Sounds Id.

*/


	// Themes params
	public static final String P_PIECES_PREVIEW_URL = "piecesPreviewUrl";
	public static final String P_BOARD_BACKGROUND_URL = "boardBackgroundUrl";
	public static final String P_THEME_NAME = "themeName";
	public static final String P_BACKGROUND_ID = "backgroundId";
	public static final String P_BOARD_ID = "boardId";
	public static final String P_PIECES_ID = "piecesId";
	public static final String P_SOUNDS_ID = "soundsId";

}
