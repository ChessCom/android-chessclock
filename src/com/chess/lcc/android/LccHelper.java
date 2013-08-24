package com.chess.lcc.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import com.chess.R;
import com.chess.backend.LiveChessService;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ChatItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.lcc.android.interfaces.LiveChessClientEventListener;
import com.chess.live.client.*;
import com.chess.live.rules.GameResult;
import com.chess.live.util.GameTimeConfig;
import com.chess.live.util.GameType;
import com.chess.model.GameLiveItem;
import com.chess.ui.engine.ChessBoardLive;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;

import java.util.*;

import static com.chess.live.rules.GameResult.WIN;

public class LccHelper { // todo: keep LccHelper instance in LiveChessService as well?

	public static final boolean TESTING_GAME = false;
	public static final String[] TEST_MOVES_COORD = {"d2d4", "c7c6", "c2c4", "d7d5", "g1f3", "g8f6", "b1c3", "e7e6",
			"c1g5", "f8e7", "e2e3", "b8d7", "f1d3", "d8b6", "d1c2", "h7h6", "g5h4", "g7g5", "h4g3", "f6h5", "c4d5",
			"h5g3", "h2g3", "e6d5", "a2a3", "g5g4", "f3h4", "e7h4", "h1h4", "d7f6", "e1e2", "c8e6", "a1h1", "h6h5",
			"c3a4", "b6c7", "a4c5", "e8c8", "d3f5", "h8e8", "f5e6", "f7e6", "c2g6", "c7e7", "b2b4", "b7b6", "c5d3",
			"c8b7", "d3e5", "d8c8", "h1c1", "e8g8", "g6d3"
			/*, "g8g5", "e5g6", "e7f7", "g6e5", "g5e5", "d4e5", "f6e4", "h4h1", "f7f2"*/};

	private static final String TAG = "LccLog-LccHelper";
	public static final int OWN_SEEKS_LIMIT = 3;
	public static final int CONNECTION_FAILURE_DELAY = 2000;
	public static final Object LOCK = new Object();

	private final LccChatListener chatListener;
	private final LccConnectionListener connectionListener;
	private final LccGameListener gameListener;
	private final LccChallengeListener challengeListener;
	private final LccSeekListListener seekListListener;
	private final LccFriendStatusListener friendStatusListener;
	private final LccAnnouncementListener announcementListener;
	private final LccAdminEventListener adminEventListener;
	private LiveChessClient lccClient;
	private User user;

	private HashMap<Long, Challenge> challenges = new HashMap<Long, Challenge>();
	private final Hashtable<Long, Challenge> seeks = new Hashtable<Long, Challenge>();
	private HashMap<Long, Challenge> ownChallenges = new HashMap<Long, Challenge>();
	private Collection<? extends User> blockedUsers = new HashSet<User>();
	private Collection<? extends User> blockingUsers = new HashSet<User>();
	private final Hashtable<Long, Game> lccGames = new Hashtable<Long, Game>();
	private final Map<String, User> friends = new HashMap<String, User>();
	private final Map<String, User> onlineFriends = new HashMap<String, User>();
	/*private Map<LiveGameEvent.Event, LiveGameEvent> pausedActivityGameEvents = new HashMap<LiveGameEvent.Event, LiveGameEvent>();*/
	// todo: clear pausedActivityLiveEvents
	private Map<LiveEvent.Event, LiveEvent> pausedActivityLiveEvents = new HashMap<LiveEvent.Event, LiveEvent>();
	private final HashMap<Long, Chat> gameChats = new HashMap<Long, Chat>();
	private LinkedHashMap<Chat, LinkedHashMap<Long, ChatMessage>> receivedChatMessages =
			new LinkedHashMap<Chat, LinkedHashMap<Long, ChatMessage>>();

	private SubscriptionId seekListSubscriptionId;
	private ChessClock whiteClock;
	private ChessClock blackClock;
	private boolean gameActivityPausedMode = true;
	private Integer latestMoveNumber;
	private Long currentGameId;
	private Long lastGameId;
	private Context context;
	private List<String> pendingWarnings;

	private LiveChessClientEventListener liveChessClientEventListener;
	private LccEventListener lccEventListener;
	private LccChatMessageListener lccChatMessageListener;

	boolean liveConnected; // it is better to keep this state inside lccholder/service instead of preferences appdata
	private LiveChessService.LccConnectUpdateListener lccConnectUpdateListener;
	private final LiveChessService liveService;
	private String networkTypeName;

	public LccHelper(Context context, LiveChessService liveService, LiveChessService.LccConnectUpdateListener lccConnectUpdateListener) {
		this.liveService = liveService;
		this.context = context;
		this.lccConnectUpdateListener = lccConnectUpdateListener;

		chatListener = new LccChatListener(this);
		connectionListener = new LccConnectionListener(this);
		gameListener = new LccGameListener(this);
		challengeListener = new LccChallengeListener(this);
		seekListListener = new LccSeekListListener(this);
		friendStatusListener = new LccFriendStatusListener(this);
		announcementListener = new LccAnnouncementListener(this);
		adminEventListener = new LccAdminEventListener();

		pendingWarnings = new ArrayList<String>();
	}

	public void checkGameEvents() {
		final Game game = getCurrentGame();

		if (game != null) {
			if (game.isGameOver()) {
				checkAndProcessEndGame(game);
			} else {
				checkAndProcessDrawOffer(game);
			}
		}
	}

	/*public void executePausedActivityGameEvents() {
		*//*if (gameActivityPausedMode) {*//*

		Log.d(TAG, "executePausedActivityGameEvents size=" + pausedActivityGameEvents.size() + ", events=" + pausedActivityGameEvents);

		setGameActivityPausedMode(false);

		if (pausedActivityGameEvents.size() > 0) {

			LiveGameEvent moveEvent = pausedActivityGameEvents.get(LiveGameEvent.Event.MOVE);
			if (moveEvent != null && (currentGameId == null || currentGameId.equals(moveEvent.getGameId()))) {
				pausedActivityGameEvents.remove(LiveGameEvent.Event.MOVE);
				//lccHolder.getAndroidStuff().processMove(gameEvent.getGameId(), gameEvent.moveIndex);
				GameLiveItem newGame = new GameLiveItem(getGame(moveEvent.getGameId()), getCurrentGame().getMoveCount() - 1*//*moveEvent.getMoveIndex()*//*);
				lccEventListener.onGameRefresh(newGame);
			}

			LiveGameEvent drawEvent = pausedActivityGameEvents.get(LiveGameEvent.Event.DRAW_OFFER);
			if (drawEvent != null && (currentGameId == null || currentGameId.equals(drawEvent.getGameId()))) {
				pausedActivityGameEvents.remove(LiveGameEvent.Event.DRAW_OFFER);
				lccEventListener.onDrawOffered(drawEvent.getDrawOffererUsername());
			}

			LiveGameEvent endGameEvent = pausedActivityGameEvents.get(LiveGameEvent.Event.END_OF_GAME);
			if (endGameEvent != null && (currentGameId == null || currentGameId.equals(endGameEvent.getGameId()))) {
				pausedActivityGameEvents.remove(LiveGameEvent.Event.END_OF_GAME);
				lccEventListener.onGameEnd(endGameEvent.getGameEndedMessage());
			}

			//pausedActivityGameEvents.clear();
		}
		paintClocks();
	}*/

	public void paintClocks() {
		if (whiteClock != null && blackClock != null) {
			whiteClock.paint();
			blackClock.paint();
		}
	}

	public void setLccEventListener(LccEventListener lccEventListener) {
		this.lccEventListener = lccEventListener;
		// todo
		/*if (isGameActivityPausedMode()) {
			executePausedActivityGameEvents(lccEventListener);
			setGameActivityPausedMode(false);
		}*/
	}

	public GameLiveItem getGameItem() {
		if (currentGameId == null) {
			return null;
		} else {
			Game game = getGame(currentGameId);
			return new GameLiveItem(game, game.getMoveCount() - 1);
		}
	}

	public int getResignTitle() {
		if (isFairPlayRestriction()) {
			return R.string.resign;
		} else if (isAbortableBySeq()) {
			return R.string.abort;
		} else {
			return R.string.resign;
		}
	}

	public String getBlackUserName() {
		return currentGameId == null ? null : getGame(currentGameId).getBlackPlayer().getUsername();
	}

	public String getUsername() {
		return user.getUsername();
	}

	public void checkAndReplayMoves() {
		Game game = getGame(currentGameId);
		if (game != null && game.getMoveCount() > 0) {
			doReplayMoves(game);
		}
	}

	public List<ChatItem> getMessagesList() {
		ArrayList<ChatItem> messageItems = new ArrayList<ChatItem>();

		Chat chat = getGameChat(currentGameId);
		if (chat != null) {
			LinkedHashMap<Long, ChatMessage> chatMessages = getChatMessages(chat.getId());
			if (chatMessages != null) {
				for (ChatMessage message : chatMessages.values()) {
					ChatItem chatItem = new ChatItem();
					chatItem.setContent(message.getMessage());
					chatItem.setIsMine(message.getAuthor().getUsername().equals(getUser().getUsername()));
					chatItem.setTimestamp(message.getDateTime().getTime());
					messageItems.add(chatItem);
				}
			}
		}
		return messageItems;
	}

	public void sendChatMessage(Long gameId, String text) {
		lccClient.sendChatMessage(getGameChat(gameId), text);
	}

	public void addPendingWarning(String warning, String... parameters) {
		Log.d(TAG, "warning = " + warning);
		if (warning != null) {
			String messageI18n = AppUtils.getI18nString(context, warning, parameters);
			pendingWarnings.add(messageI18n == null ? warning : messageI18n);
		}
	}

	public List<String> getPendingWarnings() {
		return pendingWarnings;
	}

	public String getLastWarningMessage() {
		return pendingWarnings.get(pendingWarnings.size() - 1);
	}

	/*public void checkAndConnect() {
		if(getAppData().isLiveChess(context) && !connected && lccClient == null){
			LccHelper.getInstance(context).runConnectTask();
		}
	}*/

	/**
	 * Connect live chess client
	 */
	public void performConnect(boolean useCurrentCredentials) {
		AppData appData = new AppData(context);
		String username = appData.getUsername();
		String pass = appData.getPassword();
		boolean emptyPassword = pass.equals(StaticData.SYMBOL_EMPTY);

		if (!useCurrentCredentials) { // todo: rename flag
			if (emptyPassword || RestHelper.IS_TEST_SERVER_MODE) {
				String sessionId = appData.getLiveSessionId();
				connectBySessionId(sessionId);
			} else {
				connectByCreds(username, pass);
			}

		} else {
			if (!emptyPassword && !RestHelper.IS_TEST_SERVER_MODE) {
				connectByCreds(username, pass);
			} else {
				liveChessClientEventListener.onSessionExpired(context.getString(R.string.session_expired));
				//String message = context.getString(R.string.account_error);
				//liveChessClientEventListener.onConnectionFailure(message);
			}
		}
	}

	public void connectByCreds(String username, String pass) {
//		Log.d(TAG, "connectByCreds : user = " + username + " pass = " + pass); // do not post in prod
		Log.d(TAG, "connectByCreds : hidden"); // do not post in pod
		lccClient.connect(username, pass, connectionListener);
		liveChessClientEventListener.onConnecting();
	}

	public void connectBySessionId(String sessionId) {
		Log.d(TAG, "connectBySessionId : sessionId = " + sessionId);
		lccClient.connect(sessionId, connectionListener);
		liveChessClientEventListener.onConnecting();
	}

	public void setLiveChessClientEventListener(LiveChessClientEventListener liveChessClientEventListener) {
		this.liveChessClientEventListener = liveChessClientEventListener;
	}

	public void onOtherClientEntered(String message) {
		liveChessClientEventListener.onConnectionFailure(message);
	}

	public void processConnectionFailure(String reason, String message) {
		String kickMessage = context.getString(R.string.live_chess_server_upgrading);
		liveChessClientEventListener.onConnectionFailure(kickMessage
				+ StaticData.SYMBOL_NEW_STR + context.getString(R.string.reason_) + reason
				+ StaticData.SYMBOL_NEW_STR + context.getString(R.string.message_) + message);
	}

	public void processConnectionFailure(FailureDetails details) {

		if (details == null && !AppUtils.isNetworkAvailable(context)) {
			// handle null-case when user tries to connect when device connection is off, just ignore
			Log.d(TAG, "processConnectionFailure: no active connection, wait for LCC reconnect");
			return;
		}

		setConnected(false);
		cancelServiceNotification();
		logout();

		Log.d(TAG, "processConnectionFailure: details=" + details);

		String detailsMessage;

		if (details != null) {

			switch (details) {
				case USER_KICKED: {
					detailsMessage = context.getString(R.string.live_chess_server_upgrading);
					break;
				}
				case ACCOUNT_FAILED: { // wrong authKey
					try {
						Thread.sleep(CONNECTION_FAILURE_DELAY); // probably should be removed
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					runConnectTask(true);
					return;
				}
				case SERVER_STOPPED: {
					detailsMessage = context.getString(R.string.server_stopped)
							+ context.getString(R.string.live_chess_server_unavailable);
					break;
				}
				case AUTH_URL_FAILED: {
					return;
				}
				default:
					// todo: show login/password popup instead
					detailsMessage = context.getString(R.string.pleaseLoginAgain);
					break;
			}
		} else {
			detailsMessage = context.getString(R.string.pleaseLoginAgain);
		}

		liveChessClientEventListener.onConnectionFailure(detailsMessage);

		// do not do any manual reconnects here
		/*else {
			Log.d(TAG, "processConnectionFailure: details=null");

			try {
				Thread.sleep(CONNECTION_FAILURE_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			runConnectTask(true); // recreate and connect*/

			/*setConnected(false);
			cancelServiceNotification();
			liveChessClientEventListener.onSessionExpired(context.getString(R.string.login_failed));*/
		//}

	}

	public void onObsoleteProtocolVersion() {
		liveChessClientEventListener.onObsoleteProtocolVersion();
	}

	public LccEventListener getLccEventListener() {
		return lccEventListener;
	}

	public void setLccChatMessageListener(LccChatMessageListener lccChatMessageListener) {
		this.lccChatMessageListener = lccChatMessageListener;
	}

	public LccChatMessageListener getLccChatMessageListener() {
		return lccChatMessageListener;
	}

	public void setLiveChessClient(LiveChessClient liveChessClient) {
		lccClient = liveChessClient;
	}

	public Game getCurrentGame() {
		return currentGameId == null ? null : lccGames.get(currentGameId);
	}

	public Game getLastGame() {
		return lastGameId != null ? lccGames.get(lastGameId) : null;
	}

	/*public class LccConnectUpdateListener extends AbstractUpdateListener<LiveChessClient> {
		public LccConnectUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(LiveChessClient returnedObj) {
			Log.d(TAG, "LiveChessClient initialized");
			lccClient = returnedObj;    // duplicate of setter
		}
	}*/

	/*public LccGameListener getGameListener() {
		return gameListener;
	}

	public LccChatListener getChatListener() {
		return chatListener;
	}

	public LccConnectionListener getConnectionListener() {
		return connectionListener;
	}*/

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LiveChessClient getClient() {
		return lccClient;
	}

	public boolean isConnected() {
		return liveConnected;
	}

	public void setConnected(boolean connected) {
		liveConnected = connected;
		if (connected) {
			liveChessClientEventListener.onConnectionEstablished();
			liveService.onLiveConnected(); // vm: why it is here?  // r: because it's used to notify activity about connected state

			lccClient.subscribeToChallengeEvents(challengeListener);
			lccClient.subscribeToGameEvents(gameListener);
			lccClient.subscribeToChatEvents(chatListener);
			lccClient.subscribeToFriendStatusEvents(friendStatusListener);
			lccClient.subscribeToAdminEvents(adminEventListener);
			lccClient.subscribeToAnnounces(announcementListener);

			ConnectivityManager connectivityManager = (ConnectivityManager)
					context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			networkTypeName = activeNetworkInfo.getTypeName();
		}
		liveChessClientEventListener.onConnectionBlocked(!connected);
	}

	public void clearChallenges() {
		challenges.clear();
	}

	public HashMap<Long, Challenge> getChallenges() {
		return challenges;
	}

	public void addOwnChallenge(Challenge challenge) {
		for (Challenge oldChallenge : ownChallenges.values()) {
			if (challenge.getGameTimeConfig().getBaseTime().equals(oldChallenge.getGameTimeConfig().getBaseTime())
					&& challenge.getGameTimeConfig().getTimeIncrement().equals(oldChallenge.getGameTimeConfig().getTimeIncrement())
					&& challenge.isRated() == oldChallenge.isRated()
					&& ((challenge.getTo() == null && oldChallenge.getTo() == null) ||
					(challenge.getTo() != null && challenge.getTo().equals(oldChallenge.getTo())))) {
				Log.d(TAG, "Check for doubled challenges: cancel challenge: " + oldChallenge);
				lccClient.cancelChallenge(oldChallenge);
			}
		}
		ownChallenges.put(challenge.getId(), challenge);
	}

	public void storeBlockedUsers(Collection<? extends User> blockedUsers, Collection<? extends User> blockingUsers) {
		this.blockedUsers = blockedUsers;
		this.blockingUsers = blockingUsers;
	}

	public LccChallengeListener getChallengeListener() {
		return challengeListener;
	}

	public LccSeekListListener getSeekListListener() {
		return seekListListener;
	}

	public LccFriendStatusListener getFriendStatusListener() {
		return friendStatusListener;
	}

	public boolean isUserBlocked(String username) {
		if (blockedUsers != null) {
			for (User user : blockedUsers) {
				if (user.getUsername().equals(username)
						&& user.isModerator() != null && !user.isModerator() && user.isStaff() != null && !user.isStaff()) {
					return true;
				}
			}
		}
		if (blockingUsers != null) {
			for (User user : blockingUsers) {
				if (user.getUsername().equals(username) && !user.isModerator() && !user.isStaff()) {
					return true;
				}
			}
		}
		return false;
	}

	public int getOwnSeeksCount() {
		int ownSeeksCount = 0;
		for (Challenge challenge : ownChallenges.values()) {
			if (challenge.isSeek()) {
				ownSeeksCount++;
			}
		}
		return ownSeeksCount;
	}

	public void clearOwnChallenges() {
		ownChallenges.clear();
		//ownSeeksCount = 0;
	}
	// todo: handle creating own seek
	/*public void issue(UserSeek seek)
	  {
		if(getOwnSeeksCount() >= OWN_SEEKS_LIMIT)
		{
		  showOwnSeeksLimitMessage();
		  return;
		}
		LccUser.Log.d(TAG, "SeekConnection issue seek: " + seek);
		Challenge challenge = mapJinSeekToLccChallenge(seek);
		//outgoingLccSeeks.add(challenge);
		lccUser.getClient().sendChallenge(challenge, lccUser.getChallengeListener());
	  }*/

	public boolean isUserPlaying() {
		for (Game game : lccGames.values()) {
			if (!game.isGameOver()) {
				return true;
			}
		}
		return false;
	}

	public boolean isUserPlayingAnotherGame(Long currentGameId) {
		for (Game game : lccGames.values()) {
			if (!game.getId().equals(currentGameId) && !game.isGameOver()) {
				return true;
			}
		}
		return false;
	}

	public void putChallenge(Long challengeId, Challenge lccChallenge) {
		challenges.put(challengeId, lccChallenge);
	}

	public void removeChallenge(long challengeId) {
		challenges.remove(challengeId);
		ownChallenges.remove(challengeId);
	}

	public void putSeek(Challenge challenge) {
		seeks.put(challenge.getId(), challenge);
	}

	public void setFriends(Collection<? extends User> friends) {
		Log.d(TAG, "CONNECTION: get friends list: " + friends);
		if (friends == null) {
			return;
		}
		for (User friend : friends) {
			putFriend(friend);
		}
	}

	public void putFriend(User friend) {
		if (friend.getStatus() != User.Status.OFFLINE) {
			onlineFriends.put(friend.getUsername(), friend);
			friends.put(friend.getUsername(), friend);
		} else {
			onlineFriends.remove(friend.getUsername());
			friends.remove(friend.getUsername());
		}
		liveChessClientEventListener.onFriendsStatusChanged();
	}

	public void removeFriend(User friend) {
		friends.remove(friend.getUsername());
		onlineFriends.remove(friend.getUsername());
	}

	public void clearOnlineFriends() {
		onlineFriends.clear();
	}

	public String[] getOnlineFriends() {
		final String[] array = new String[]{StaticData.SYMBOL_EMPTY};
		return onlineFriends.size() != 0 ? onlineFriends.keySet().toArray(array) : array;
	}

	public SubscriptionId getSeekListSubscriptionId() {
		return seekListSubscriptionId;
	}

	public void setSeekListSubscriptionId(SubscriptionId seekListSubscriptionId) {
		this.seekListSubscriptionId = seekListSubscriptionId;
	}

	public void putGame(Game lccGame) {
		lccGames.put(lccGame.getId(), lccGame);
	}

	public Game getGame(Long gameId) {
		return lccGames.get(gameId);
	}

	public void clearSeeks() {
		seeks.clear();
	}

	public void clearGames() {
		lccGames.clear();
	}

//	public String[] getGameData(Long gameId, int moveIndex) {
//		Game lccGame = getGame(gameId);
//		String[] gameData = new String[GameItem.GAME_DATA_ELEMENTS_COUNT];
//
//		gameData[0] = String.valueOf(lccGame.getId());  // TODO eliminate string conversion and use Objects
//		gameData[1] = "1";
//		gameData[2] = StaticData.SYMBOL_EMPTY + System.currentTimeMillis(); // todo, resolve GameListItem.TIMESTAMP
//		gameData[3] = StaticData.SYMBOL_EMPTY;
//		gameData[4] = lccGame.getWhitePlayer().getUsername().trim();
//		gameData[5] = lccGame.getBlackPlayer().getUsername().trim();
//		gameData[GameItem.STARTING_FEN_POSITION_NUMB] = StaticData.SYMBOL_EMPTY; // starting_fen_position
//		String moves = StaticData.SYMBOL_EMPTY;
//
//
//		final Iterator movesIterator = lccGame.getMoves().iterator();
//		for (int i = 0; i <= moveIndex; i++) {
//			moves += movesIterator.next() + " ";
//		}
//		if (moveIndex == -1) {
//			moves = StaticData.SYMBOL_EMPTY;
//		}
//
//		gameData[GameItem.MOVE_LIST_NUMB] = moves; // move_list
//		gameData[8] = StaticData.SYMBOL_EMPTY; // user_to_move
//
//		Integer whiteRating = 0;
//		Integer blackRating = 0;
//		switch (lccGame.getGameTimeConfig().getGameTimeClass()) {
//			case BLITZ: {
//				whiteRating = lccGame.getWhitePlayer().getBlitzRating();
//				blackRating = lccGame.getBlackPlayer().getBlitzRating();
//				break;
//			}
//			case LIGHTNING: {
//				whiteRating = lccGame.getWhitePlayer().getQuickRating();
//				blackRating = lccGame.getBlackPlayer().getQuickRating();
//				break;
//			}
//			case STANDARD: {
//				whiteRating = lccGame.getWhitePlayer().getStandardRating();
//				blackRating = lccGame.getBlackPlayer().getStandardRating();
//				break;
//			}
//		}
//		if (whiteRating == null) {
//			whiteRating = 0;
//		}
//		if (blackRating == null) {
//			blackRating = 0;
//		}
//
//		gameData[9] = whiteRating.toString();
//		gameData[10] = blackRating.toString();
//
//		gameData[11] = StaticData.SYMBOL_EMPTY; // todo: encoded_move_string
//		gameData[12] = StaticData.SYMBOL_EMPTY; // has_new_message
//		gameData[13] = StaticData.SYMBOL_EMPTY + (lccGame.getGameTimeConfig().getBaseTime() / 10); // seconds_remaining
//
//		return gameData;
//	}

	public void makeMove(String move, LccGameTaskRunner gameTaskRunner, String debugInfo) {
		Game game = getCurrentGame();
		/*if(chessMove.isCastling())
			{
			  lccMove = chessMove.getWarrenSmithString().substring(0, 4);
			}
			else
			{
			  lccMove = move.getMoveString();
			  lccMove = chessMove.isPromotion() ? lccMove.replaceFirst("=", StaticData.SYMBOL_EMPTY) : lccMove;
			}*/

		Log.d(TAG, "MOVE: making move: gameId=" + game.getId() + ", move=" + move);
		gameTaskRunner.runMakeMoveTask(game, move, debugInfo);

		if (game.getMoveCount() >= 1) // we should start opponent's clock after at least 2-nd ply (moveCount == 1, or moveCount > 1)
		{
			final boolean isWhiteRunning = user.getUsername().equals(game.getWhitePlayer().getUsername());
			final ChessClock clockToBePaused = isWhiteRunning ? whiteClock : blackClock;
			if (game.getMoveCount() >= 2) // we should stop our clock if it was at least 3-rd ply (seq == 2, or seq > 2)
			{
				clockToBePaused.setRunning(false);
			}
		}
	}

	public void rematch() {
		final Game lastGame = getLastGame();

		Log.d("REMATCHTEST", "rematch getLastGame " + lastGame);

		final List<GameResult> gameResults = lastGame.getResults();
		final String whiteUsername = lastGame.getWhitePlayer().getUsername();
		final String blackUsername = lastGame.getBlackPlayer().getUsername();

		boolean switchColor = false;
		if (gameResults != null) {
			final GameResult whitePlayerResult = gameResults.get(0);
			final GameResult blackPlayerResult = gameResults.get(1);
			final GameResult result;
			if (whitePlayerResult == GameResult.WIN) {
				result = blackPlayerResult;
			} else if (blackPlayerResult == GameResult.WIN) {
				result = whitePlayerResult;
			} else {
				result = whitePlayerResult;
			}
			switchColor = result != GameResult.ABORTED;
		}

		String to = null;
		PieceColor color = PieceColor.WHITE;
		final String username = user.getUsername();
		if (whiteUsername.equals(username)) {
			to = blackUsername;
			color = switchColor ? PieceColor.BLACK : PieceColor.WHITE;
		} else if (blackUsername.equals(username)) {
			to = whiteUsername;
			color = switchColor ? PieceColor.WHITE : PieceColor.BLACK;
		}

		final Integer minRating = null;
		final Integer maxRating = null;
		final Integer minMembershipLevel = null;
		final GameType gameType = GameType.Chess;
		final Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
				user, to, gameType, color, lastGame.isRated(),
				lastGame.getGameTimeConfig(), minMembershipLevel, minRating, maxRating);

		challenge.setRematchGameId(lastGameId);

		liveService.runSendChallengeTask(challenge);
	}

	public ChessClock getBlackClock() {
		return blackClock;
	}

	public ChessClock getWhiteClock() {
		return whiteClock;
	}

	public void setWhiteClock(ChessClock whiteClock) {
		this.whiteClock = whiteClock;
	}

	public void setBlackClock(ChessClock blackClock) {
		this.blackClock = blackClock;
	}

	/*private void stopService() {
		// from doc: Note that if a stopped service still has ServiceConnection objects bound to it with
		// the BIND_AUTO_CREATE set, it will not be destroyed until all of these bindings are removed.
		boolean result = context.stopService(new Intent(context, LiveChessService.class));
		Log.d(TAG, "Live stopService: " + result);
		liveService.stopSelf(); // don't work too
	}*/

	public void logout() {
		Log.d(TAG, "USER LOGOUT");
		new AppData(context).setLiveChessMode(false);
		setCurrentGameId(null);
		setUser(null);
		setConnected(false);

		clearGames();
		clearChallenges();
		clearOwnChallenges();
		clearSeeks();
		clearOnlineFriends();
		clearPausedEvents();

		runDisconnectTask(); // disconnect and reset client instance

		cancelServiceNotification();

		//instance = null;
	}

	public boolean isSeekContains(Long id) {
		return seeks.containsKey(id);
	}

	public void removeSeek(Long id) {
		if (seeks.size() > 0) {
			seeks.remove(id);
		}
		ownChallenges.remove(id);
	}

	public boolean isGameActivityPausedMode() {
		return gameActivityPausedMode;
	}

	public void setGameActivityPausedMode(boolean gameActivityPausedMode) { // TODO Unsafe -> replace with save server data holder logic
		this.gameActivityPausedMode = gameActivityPausedMode;
//		pausedActivityGameEvents.clear();
	}

	/*public Map<LiveGameEvent.Event, LiveGameEvent> getPausedActivityGameEvents() {
		return pausedActivityGameEvents;
	}*/

	public Map<LiveEvent.Event, LiveEvent> getPausedActivityLiveEvents() {
		return pausedActivityLiveEvents;
	}

	public void checkAndProcessFullGame() {
		boolean isGameAlreadyPresent = currentGameId != null && getGame(currentGameId) != null;
		if (isGameAlreadyPresent) {
			synchronized(LccHelper.LOCK) {
				processFullGame();
			}
		}
	}

	public void processFullGame() {
//		if (lccEventListener != null) {
//			lccEventListener.onGameRecreate();
//		}

		latestMoveNumber = 0; // it was null before
		ChessBoardLive.resetInstance();

		//int time = game.getGameTimeConfig().getBaseTime() * 100;
		if (whiteClock != null) {
			whiteClock.setRunning(false);
		}
		if (blackClock != null) {
			blackClock.setRunning(false);
		}

		// todo: show actual game over time for ended games
		setWhiteClock(new ChessClock(this, true));
		setBlackClock(new ChessClock(this, false));

		lccEventListener.startGameFromService();
	}

	public Integer getLatestMoveNumber() {
		return latestMoveNumber;
	}

	public void setLatestMoveNumber(Integer latestMoveNumber) {
		this.latestMoveNumber = latestMoveNumber;
	}

	public void doReplayMoves(Game game) {
		Log.d(TAG, "GAME LISTENER: replay moves, gameId " + game.getId());

		latestMoveNumber = game.getMoveCount() - 1;
		User moveMaker = (latestMoveNumber % 2 == 0) ? game.getWhitePlayer() : game.getBlackPlayer();
		lccEventListener.onGameRefresh(new GameLiveItem(game, latestMoveNumber));
		doUpdateClocks(game, moveMaker, latestMoveNumber);
	}

	public void doMoveMade(final Game game, final User moveMaker, int moveIndex) {
		/*if (((latestMoveNumber != null) && (moveIndex < latestMoveNumber)) || (latestMoveNumber == null && moveIndex > 0)) {
			Log.d(TAG, "GAME LISTENER: Extra onMoveMade received (currentMoveIndex=" + moveIndex
					+ ", latestMoveNumber=" + latestMoveNumber + StaticData.SYMBOL_RIGHT_PAR);
			return;
		} else {*/
		latestMoveNumber = moveIndex;
		//}

		if (!isGameActivityPausedMode()) {
			// todo: possible optimization - keep gameLiveItem between moves and just add new move when it comes
			lccEventListener.onGameRefresh(new GameLiveItem(game, moveIndex));
			doUpdateClocks(game, moveMaker, moveIndex); // update clock only for resumed activity?
		} else {
			Log.d(TAG, "paused mode: postpone MOVE processing");
			/*LiveGameEvent moveEvent = new LiveGameEvent();
			moveEvent.setEvent(LiveGameEvent.Event.MOVE);
			moveEvent.setGameId(game.getId());
			//moveEvent.setMoveIndex(moveIndex);
			getPausedActivityGameEvents().put(moveEvent.getEvent(), moveEvent);*/
		}
		// doUpdateClocks(game, moveMaker, moveIndex);
	}

	private void doUpdateClocks(Game game, User moveMaker, int moveIndex) {
		// TODO: This method does NOT support the game observer mode. Redevelop it if necessary.

		// UPDATELCC todo: probably could be simplified - update clock only for latest move/player in order to get rid of moveIndex/moveMaker params
		if (game.getMoveCount() >= 2 && moveIndex == game.getMoveCount() - 1) {

			final boolean isWhiteDone = game.getWhitePlayer().getUsername().equals(moveMaker.getUsername());
			final boolean isBlackDone = game.getBlackPlayer().getUsername().equals(moveMaker.getUsername());

			if (!game.isGameOver()) {
				getWhiteClock().setRunning(isBlackDone);
			}
			if (!game.isGameOver()) {
				getBlackClock().setRunning(isWhiteDone);
			}
		}
	}

	public void setLastGameId(Long lastGameId) {
		this.lastGameId = lastGameId;
		Log.d("REMATCHTEST", "setLastGameId " + lastGameId);
	}

	public void setCurrentGameId(Long gameId) {
		currentGameId = gameId;
	}

	public Long getCurrentGameId() {
		return currentGameId;
	}

	public void putGameChat(Long gameId, Chat chat) {
		gameChats.put(gameId, chat);
	}

	public Chat getGameChat(Long gameId) {
		return gameChats.get(gameId);
	}

	public LinkedHashMap<Chat, LinkedHashMap<Long, ChatMessage>> getReceivedChats() {
		return receivedChatMessages;
	}

	public LinkedHashMap<Long, ChatMessage> getChatMessages(String chatId) {
		for (Chat storedChat : receivedChatMessages.keySet()) {
			if (chatId.equals(storedChat.getId())) {
				return receivedChatMessages.get(storedChat);
			}
		}
		return null;
	}

	public boolean isCurrentGameExist() {
		return currentGameId != null && getGame(currentGameId) != null && !getGame(currentGameId).isGameOver();
	}

	public Boolean isFairPlayRestriction() {
		Game game = getCurrentGame();
		String username = user.getUsername();

		final String whiteUsername = game.getWhitePlayer().getUsername();
		final String blackUsername = game.getBlackPlayer().getUsername();
		if (whiteUsername.equals(username) && !game.isAbortableByPlayer(whiteUsername)) {
			return true;
		} else if (blackUsername.equals(username) && !game.isAbortableByPlayer(blackUsername)) {
			return true;
		}
		return false;
	}

	public Boolean isAbortableBySeq() {
		return getCurrentGame().getMoveCount() < 3;
	}

	public void setOuterChallengeListener(OuterChallengeListener outerChallengeListener) {
		challengeListener.setOuterChallengeListener(outerChallengeListener);
	}


	public void runConnectTask(boolean useCurrentCredentials) {
		new ConnectLiveChessTask(lccConnectUpdateListener, useCurrentCredentials, this).executeTask();
	}

	public void runConnectTask() {
		new ConnectLiveChessTask(lccConnectUpdateListener, this).executeTask();
	}

	public void runDisconnectTask() {
		new LiveDisconnectTask().execute();
	}

	private class LiveDisconnectTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			if (lccClient != null) {
				lccClient.disconnect();
				resetClient();
			}
			return null;
		}
	}

	public void resetClient() {
		lccClient = null;
	}

	public Context getContext() {
		return context;
	}

	// todo: remove after debugging
	public Integer getGamesCount() {
		return lccGames.size();
	}

	public void checkFirstTestMove() {
		if (TESTING_GAME) {
			final Game game = getCurrentGame();
			if (game.isMoveOf(getUsername()) && game.getMoveCount() == 0) {
				//Utils.sleep(5000);
				lccClient.makeMove(game, TEST_MOVES_COORD[game.getMoveCount()].trim());
			}
		}
	}

	public void checkTestMove() {
		if (TESTING_GAME) {
			final Game game = getCurrentGame();
			if (game.isMoveOf(getUsername()) /*&& game.getState() == Game.State.Started*/ && game.getMoveCount() < TEST_MOVES_COORD.length) {
				//Utils.sleep(0.5F);
				lccClient.makeMove(game, TEST_MOVES_COORD[game.getMoveCount()].trim());
			}
		}
	}

	public Boolean isUserColorWhite() {
		return getBlackUserName() == null ? null : !getBlackUserName().equals(getUsername());
	}

	public String getOpponentName() {
		final Boolean isUserColorWhite = isUserColorWhite();
		final Game game = getCurrentGame();
		if (isUserColorWhite == null || game == null) {
			return null;
		} else {
			return isUserColorWhite ? game.getBlackPlayer().getUsername() : game.getWhitePlayer().getUsername();
		}
	}

	public void clearPausedEvents() {
		//pausedActivityGameEvents.clear();
		pausedActivityLiveEvents.clear();
	}

	private void cancelServiceNotification() {
		// http://stackoverflow.com/questions/11387320/notificationmanager-cancel-doesnt-work-for-me
		/*NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(R.id.live_service_notification);*/

		liveService.stopForeground(true); // exit Foreground mode and remove Notification icon
	}

	//	public void createChallenge(String friend) {
	public void createChallenge(LiveGameConfig config) {
		if (getOwnSeeksCount() >= LccHelper.OWN_SEEKS_LIMIT || getUser() == null) {
			return; // TODO throw exception
		}
		boolean rated = config.isRated();

		Integer initialTime = config.getInitialTime();
		Integer bonusTime = config.getBonusTime();

		GameTimeConfig gameTimeConfig = new GameTimeConfig(initialTime * 60 * 10, bonusTime * 10);

		Integer minRating = config.getMinRating();
		Integer maxRating = config.getMaxRating();
		Integer minMembershipLevel = null;
		PieceColor pieceColor = PieceColor.UNDEFINED;  // always random!

		final GameType gameType = GameType.Chess;
		Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
				getUser(),
				config.getOpponentName(),
				gameType,
				pieceColor, rated, gameTimeConfig,
				minMembershipLevel, minRating, maxRating);

		FlurryAgent.logEvent(FlurryData.CHALLENGE_CREATED);
		liveService.runSendChallengeTask(challenge);
	}

	public String getNetworkTypeName() {
		return networkTypeName;
	}

	public void setNetworkTypeName(String networkTypeName) {
		this.networkTypeName = networkTypeName;
	}

	public void checkAndProcessDrawOffer(Game game) {
		final String opponentName = getOpponentName();

		if (opponentName != null && game.isDrawOfferedByPlayer(opponentName)) { // check if game is not ended?
			Log.d(TAG, "GAME LISTENER: Draw offered at the move #" + game.getMoveCount() + ", game.id=" + game.getId()
					+ ", offerer=" + opponentName + ", game=" + game);

			if (!isGameActivityPausedMode()) {
				Log.d(TAG, "DRAW SHOW");
				getLccEventListener().onDrawOffered(opponentName);
			} else {
				Log.d(TAG, "paused mode: postpone DRAW processing");
				/*final LiveGameEvent drawOfferedEvent = new LiveGameEvent();
				drawOfferedEvent.setEvent(LiveGameEvent.Event.DRAW_OFFER);
				drawOfferedEvent.setGameId(game.getId());
				drawOfferedEvent.setDrawOffererUsername(opponentName);
				lccHelper.getPausedActivityGameEvents().put(drawOfferedEvent.getEvent(), drawOfferedEvent);*/
			}
		}
	}

	public void checkAndProcessEndGame(Game game) {
		List<GameResult> gameResults = game.getResults();
		final GameResult whitePlayerResult = gameResults.get(0);
		final GameResult blackPlayerResult = gameResults.get(1);
		final String whiteUsername = game.getWhitePlayer().getUsername();
		final String blackUsername = game.getBlackPlayer().getUsername();

		GameResult result;
		String winnerUsername = null;

		if (whitePlayerResult == WIN) {
			result = blackPlayerResult;
			winnerUsername = whiteUsername;
		} else if (blackPlayerResult == WIN) {
			result = whitePlayerResult;
			winnerUsername = blackUsername;
		} else {
			result = whitePlayerResult;
		}

		String message = StaticData.SYMBOL_EMPTY;
		switch (result) {
			case TIMEOUT:
				message = context.getString(R.string.won_on_time, winnerUsername);
				break;
			case RESIGNED:
				message = context.getString(R.string.won_by_resignation, winnerUsername);
				break;
			case CHECKMATED:
				message = context.getString(R.string.won_by_checkmate, winnerUsername);
				break;
			case DRAW_BY_REPETITION:
				message = context.getString(R.string.game_draw_by_repetition);
				break;
			case DRAW_AGREED:
				message = context.getString(R.string.game_drawn_by_agreement);
				break;
			case STALEMATE:
				message = context.getString(R.string.game_drawn_by_stalemate);
				break;
			case DRAW_BY_INSUFFICIENT_MATERIAL:
				message = context.getString(R.string.game_drawn_insufficient_material);
				break;
			case DRAW_BY_50_MOVE:
				message = context.getString(R.string.game_drawn_by_fifty_move_rule);
				break;
			case ABANDONED:
				message = winnerUsername + StaticData.SYMBOL_SPACE + context.getString(R.string.won_game_abandoned);
				break;
			case ABORTED:
				message = context.getString(R.string.game_aborted);
				break;
		}
		//message = whiteUsername + " vs. " + blackUsername + " - " + message;
		Log.d(TAG, "GAME LISTENER: " + message);

		if (getWhiteClock() != null) {
			getWhiteClock().setRunning(false);
		}
		if (getBlackClock() != null) {
			getBlackClock().setRunning(false);
		}

		String abortedCodeMessage = game.getCodeMessage(); // used only for aborted games
		if (abortedCodeMessage != null) {
			final String messageI18n = AppUtils.getI18nString(context, abortedCodeMessage, game.getAborterUsername());
			if (messageI18n != null) {
				message = messageI18n;
			}
		}

		if (getCurrentGameId() == null) {
			setCurrentGameId(game.getId());
		}

		if (!isGameActivityPausedMode()) {
			getLccEventListener().onGameEnd(message);
		} else {
			Log.d(TAG, "paused mode: postpone GAME END processing");
			/*Log.d(TAG, "ActivityPausedMode = true");
			final LiveGameEvent gameEndedEvent = new LiveGameEvent();
			gameEndedEvent.setGameId(gameId);
			gameEndedEvent.setEvent(LiveGameEvent.Event.END_OF_GAME);
			gameEndedEvent.setGameEndedMessage(message);
			lccHelper.getPausedActivityGameEvents().put(gameEndedEvent.getEvent(), gameEndedEvent);
			if (lccHelper.getLccEventListener() == null) { // if activity is not started yet
				lccHelper.processFullGame(game);
				Log.d(TAG, "processFullGame");
			}*/
		}
	}
}
