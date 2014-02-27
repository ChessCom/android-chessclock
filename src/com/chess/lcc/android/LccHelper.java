package com.chess.lcc.android;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.chess.R;
import com.chess.backend.entity.api.ChatItem;
import com.chess.backend.image_load.bitmapfun.AsyncTask;
import com.chess.lcc.android.interfaces.LccChatMessageListener;
import com.chess.lcc.android.interfaces.LccEventListener;
import com.chess.live.client.*;
import com.chess.live.rules.GameResult;
import com.chess.live.util.GameRatingClass;
import com.chess.live.util.GameTimeConfig;
import com.chess.live.util.GameType;
import com.chess.model.GameLiveItem;
import com.chess.statics.AppConstants;
import com.chess.statics.FlurryData;
import com.chess.statics.IntentConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.interfaces.MakeMoveFace;
import com.chess.utilities.AppUtils;
import com.chess.utilities.LogMe;
import com.flurry.android.FlurryAgent;

import java.util.*;

import static com.chess.live.rules.GameResult.WIN;

public class LccHelper {

	private static final String TAG = "LccLog-LccHelper";

	public static final boolean TESTING_GAME = false;
	public static final String[] TEST_MOVES_COORD = {"a2a3", "h7h6", "b2b3", "g7g6", "c2c3", "f7f6", "d2d4", "d7d6",
			"e2e4", "c8h3", "f2f4", "b8a6", "d4d5", "e7e6", "d5e6", "d8d7", "e4e5", "d7h7", "a3a4", "e8c8", "c3c4",
			"d8d7", "a1a2", "d7g7", "b1d2", "c8b8", "e6e7", "a6c5", "g2h3", "h6h5", "e7e8q"};
	public static final long TEST_MOVES_DELAY = 10 * 1000;
	public static final long TEST_FIRST_MOVE_DELAY = 3 * 1000;

	public static final int OWN_SEEKS_LIMIT = 3;
	public static final Object GAME_SYNC_LOCK = new Object();

	private final LccChatListener chatListener;
	private final LccGameListener gameListener;
	private final LccChallengeListener challengeListener;
	//private final LccSeekListListener seekListListener;
	private final LccFriendStatusListener friendStatusListener;
	private final LccUserListListener userListListener;
	private final LccAnnouncementListener announcementListener;
	private final LccAdminEventListener adminEventListener;
	private final LiveConnectionHelper liveConnectionHelper;
	private final Handler handler;
	private LiveChessClient lccClient;
	private User user;

	private HashMap<Long, Challenge> challenges = new HashMap<Long, Challenge>();
	private final Hashtable<Long, Challenge> seeks = new Hashtable<Long, Challenge>();
	private HashMap<Long, Challenge> ownChallenges = new HashMap<Long, Challenge>();
	private Collection<? extends User> blockedUsers = new HashSet<User>();
	private Collection<? extends User> blockingUsers = new HashSet<User>();
	private final Hashtable<Long, Game> lccGames = new Hashtable<Long, Game>();
	private final Map<String, User> onlineFriends = new HashMap<String, User>();
	private final HashMap<Long, Chat> gameChats = new HashMap<Long, Chat>();
	private LinkedHashMap<Chat, LinkedHashMap<Long, ChatMessage>> receivedChatMessages =
			new LinkedHashMap<Chat, LinkedHashMap<Long, ChatMessage>>();

	private SubscriptionId seekListSubscriptionId;
	private ChessClock whiteClock;
	private ChessClock blackClock;
	private boolean gameActivityPausedMode = true;
	private Integer latestMoveNumber;
	private Long currentGameId;
	private Game lastGame;
	private Long currentObservedGameId;
	private Context context;
	private List<String> pendingWarnings;

	private LccEventListener lccEventListener;
	private LccEventListener lccObserveEventListener;
	private LccChatMessageListener lccChatMessageListener;
	private MoveInfo latestMoveInfo;


	public LccHelper(LiveConnectionHelper liveConnectionHelper) {
		this.context = liveConnectionHelper.getContext();
		this.liveConnectionHelper = liveConnectionHelper;
		chatListener = new LccChatListener(this);
		gameListener = new LccGameListener(this);
		challengeListener = new LccChallengeListener(this);
		//seekListListener = new LccSeekListListener(this);
		friendStatusListener = new LccFriendStatusListener(this);
		userListListener = new LccUserListListener(this);
		announcementListener = new LccAnnouncementListener(this);
		adminEventListener = new LccAdminEventListener();

		pendingWarnings = new ArrayList<String>();
		handler = new Handler();
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

	public void updatePlayersClock() {
		if (whiteClock != null && blackClock != null) {
			whiteClock.updatePlayerTimer();
			blackClock.updatePlayerTimer();
		}
	}

	public void requestTimeForPlayers() {
		if (whiteClock != null && blackClock != null) {
			whiteClock.requestTimeForPlayers();
			blackClock.requestTimeForPlayers();
		}
	}

	public void setLccEventListener(LccEventListener lccEventListener) {
		this.lccEventListener = lccEventListener;
	}

	public void setLccObserveEventListener(LccEventListener lccObserveEventListener) {
		this.lccObserveEventListener = lccObserveEventListener;
	}

	private GameLiveItem getGameItem(Long gameId) {
		if (gameId == null) {
			return null;
		} else {
			Game game = getGame(gameId);
			if (game == null) {
				return null;
			}
			return new GameLiveItem(game, game.getMoveCount() - 1);
		}
	}

	public GameLiveItem getGameItem() {
		return getGameItem(currentGameId);
	}

	public GameLiveItem getObservedGameItem() {
		return getGameItem(currentObservedGameId);
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
					User author = message.getAuthor();
					ChatItem chatItem = new ChatItem();
					chatItem.setContent(message.getMessage());
					chatItem.setIsMine(author.getUsername().equals(getUser().getUsername()));
					chatItem.setTimestamp(message.getDateTime().getTime());

					if (author.isAvatarPresent()) {
						chatItem.setAvatar(author.getAvatarUrl());
					}

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
		LogMe.dl(TAG, "warning = " + warning);
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

	public LccEventListener getLccEventListener() {
		return lccEventListener;
	}

	public LccEventListener getLccObserveEventListener() {
		return lccObserveEventListener;
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

	public Game getCurrentObservedGame() {
		return currentObservedGameId == null ? null : lccGames.get(currentObservedGameId);
	}

	public Game getLastGame() {
		return lastGame;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void clearChallenges() {
		challenges.clear();
	}

	public void clearChallengesData() {
		clearChallenges();
		clearOwnChallenges();
		clearSeeks();
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
				LogMe.dl(TAG, "Check for doubled challenges: cancel challenge: " + oldChallenge);
				lccClient.cancelChallenge(oldChallenge); // todo: use task
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

	public HashMap<Long, Challenge> getOwnChallenges() {
		return ownChallenges;
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
		LccUser.LogMe.dl(TAG, "SeekConnection issue seek: " + seek);
		Challenge challenge = mapJinSeekToLccChallenge(seek);
		//outgoingLccSeeks.add(challenge);
		lccUser.getClient().sendChallenge(challenge, lccUser.getChallengeListener());
	  }*/

	public boolean isObservedGame(Game game) {
		return !isMyGame(game);
	}

	public boolean isMyGame(Game game) {
		return game.isPlayer(getUsername());
	}

	public boolean isUserPlaying() {
		for (Game game : lccGames.values()) {
			if (!game.isGameOver() && isMyGame(game)) {
				return true;
			}
		}
		return false;
	}

	public boolean isUserPlayingAnotherGame(Long currentGameId) {
		for (Game game : lccGames.values()) {
			if (!game.getId().equals(currentGameId) && !game.isGameOver() && isMyGame(game)) {
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
		} else {
			onlineFriends.remove(friend.getUsername());
		}
		if (lccEventListener != null) {
			lccEventListener.onFriendsStatusChanged();
		}
	}

	public void removeFriend(User friend) {
		onlineFriends.remove(friend.getUsername());
	}

	public void clearOnlineFriends() {
		onlineFriends.clear();
	}

	public String[] getOnlineFriends() {
		final String[] array = new String[]{};
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

//	public String[] getGameData(Long gameId, int moveIndex) {   // todo: vm: do we need it? please remove all commented code that we will not need further
//		Game lccGame = getGame(gameId);
//		String[] gameData = new String[GameItem.GAME_DATA_ELEMENTS_COUNT];
//
//		gameData[0] = String.valueOf(lccGame.getId());  // TODO eliminate string conversion and use Objects
//		gameData[1] = "1";
//		gameData[2] = StaticData.EMPTY + System.currentTimeMillis(); // todo, resolve GameListItem.TIMESTAMP
//		gameData[3] = StaticData.EMPTY;
//		gameData[4] = lccGame.getWhitePlayer().getUsername().trim();
//		gameData[5] = lccGame.getBlackPlayer().getUsername().trim();
//		gameData[GameItem.STARTING_FEN_POSITION_NUMB] = StaticData.EMPTY; // starting_fen_position
//		String moves = StaticData.EMPTY;
//
//
//		final Iterator movesIterator = lccGame.getMoves().iterator();
//		for (int i = 0; i <= moveIndex; i++) {
//			moves += movesIterator.next() + " ";
//		}
//		if (moveIndex == -1) {
//			moves = StaticData.EMPTY;
//		}
//
//		gameData[GameItem.MOVE_LIST_NUMB] = moves; // move_list
//		gameData[8] = StaticData.EMPTY; // user_to_move
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
//		gameData[11] = StaticData.EMPTY; // todo: encoded_move_string
//		gameData[12] = StaticData.EMPTY; // has_new_message
//		gameData[13] = StaticData.EMPTY + (lccGame.getGameTimeConfig().getBaseTime() / 10); // seconds_remaining
//
//		return gameData;
//	}

	public void makeMove(String move, LccGameTaskRunner gameTaskRunner, String debugInfo, MakeMoveFace makeMoveFace) {
		Game game = getCurrentGame();
		/*if(chessMove.isCastling())
			{
			  lccMove = chessMove.getWarrenSmithString().substring(0, 4);
			}
			else
			{
			  lccMove = move.getMoveString();
			  lccMove = chessMove.isPromotion() ? lccMove.replaceFirst("=", StaticData.EMPTY) : lccMove;
			}*/

		LogMe.dl(TAG, "MOVE: making move: gameId=" + game.getId() + ", move=" + move);

		if (LiveConnectionHelper.THREAD_MONITORING_ENABLED) {
			long threadId = Thread.currentThread().getId();
			setLatestMoveInfo(new MoveInfo(game.getId(), move, threadId));
		}

		gameTaskRunner.runMakeMoveTask(game, move, debugInfo, makeMoveFace);
	}

	public void rematch() {
		final Game lastGame = getLastGame();

//		LogMe.dl("REMATCHTEST", "rematch getLastGame " + lastGame);

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

		challenge.setRematchGameId(lastGame.getId());

		liveConnectionHelper.runSendChallengeTask(challenge);
	}

	public void initClocks() {
		stopClocks();

		whiteClock = new ChessClock(this, true, isActiveGamePresent());
		blackClock = new ChessClock(this, false, isActiveGamePresent());
	}

	public void stopClocks() {
		if (whiteClock != null) {
			whiteClock.setRunning(false);
		}
		if (blackClock != null) {
			blackClock.setRunning(false);
		}
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

	public void processFullGame() {
		latestMoveNumber = 0;

		LogMe.dl(TAG, "processFullGame lccEventListener=" + lccEventListener);

		if (lccEventListener == null) { // if we restart app and connected to service, but no live game screens opened
			context.sendBroadcast(new Intent(IntentConstants.START_LIVE_GAME));
		} else {
			lccEventListener.startGameFromService();
		}
	}

	public Integer getLatestMoveNumber() {
		return latestMoveNumber;
	}

	public void doReplayMoves(Game game) {
		LogMe.dl(TAG, "GAME LISTENER: replay moves, gameId " + game.getId());

		latestMoveNumber = game.getMoveCount() - 1;
		lccEventListener.onGameRefresh(new GameLiveItem(game, latestMoveNumber));
	}

	public void doMoveMade(final Game game, int moveIndex) {
		latestMoveNumber = moveIndex;

		LogMe.dl(TAG, "doMoveMade isGameActivityPausedMode()=" + isGameActivityPausedMode());

		if (!isGameActivityPausedMode()) {
			// todo: possible optimization - keep gameLiveItem between moves and just add new move when it comes

			synchronized (LccHelper.GAME_SYNC_LOCK) {
				lccEventListener.onGameRefresh(new GameLiveItem(game, moveIndex));
			}
		}
	}

	public void setLastGame(Game lastGame) {
		this.lastGame = lastGame;
	}

	public void setCurrentGameId(Long gameId) {
		currentGameId = gameId;
	}

	public Long getCurrentGameId() {
		return currentGameId;
	}

	public void setCurrentObservedGameId(Long gameId) {
		currentObservedGameId = gameId;
	}

	public Long getCurrentObservedGameId() {
		return currentObservedGameId;
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

	public boolean isActiveGamePresent() {
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

	public void onChallengeRejected(String by) {
		lccEventListener.onChallengeRejected(by);
	}

	public void observeTopGame() {
		LogMe.dl(TAG, "observe top game: listener=" + gameListener);
		lccClient.observeTopGame(GameRatingClass.Blitz, gameListener);
	}

	public void unObserveGame(Long gameId) {
		lccClient.unobserveGame(gameId);
	}

	public Context getContext() {
		return context;
	}

	// todo: remove after debugging
	public Integer getGamesCount() {
		return lccGames.size();
	}

	public void checkFirstTestMove() {

		final Game game = getCurrentGame();

		if (TESTING_GAME && isMyGame(game)) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (game.isMoveOf(getUsername()) && game.getMoveCount() == 0) {
						if (game.isMoveOf(getUsername()) /*&& game.getState() == Game.State.Started*/ && game.getMoveCount() < TEST_MOVES_COORD.length) {
							liveConnectionHelper.makeMove(TEST_MOVES_COORD[game.getMoveCount()].trim(), "", null);
						}
					}
				}
			}, TEST_FIRST_MOVE_DELAY);
		}
	}

	public void checkTestMove() {
		final Game game = getCurrentGame();
		if (TESTING_GAME && isMyGame(game)) {

			long delay = game.getMoveCount() > 2 ? TEST_MOVES_DELAY : 0;

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {

					/*LogMe.dl("game.isMoveOf(getUsername()) " + game.isMoveOf(getUsername()));
					LogMe.dl("game.getMoveCount() " + game.getMoveCount());
					LogMe.dl("new move TEST_MOVES_COORD[latestMoveNumber] " + TEST_MOVES_COORD[latestMoveNumber]);*/

					if (game.isMoveOf(getUsername()) /*&& game.getState() == Game.State.Started*/ && game.getMoveCount() < TEST_MOVES_COORD.length) {
						liveConnectionHelper.makeMove(TEST_MOVES_COORD[game.getMoveCount()].trim(), "", null);
					}
				}
			}, delay);
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

	public void createChallenge(LiveGameConfig config) {
		LogMe.dl(TAG, "createChallenge");

		if (getOwnSeeksCount() >= LccHelper.OWN_SEEKS_LIMIT || getUser() == null) {
			return; // TODO throw exception
		}
		boolean rated = config.isRated();

		Integer initialTime = config.getInitialTime();
		Integer bonusTime = config.getBonusTime();

		GameTimeConfig gameTimeConfig = new GameTimeConfig(initialTime * 60 * 10, bonusTime * 10);

		Integer minMembershipLevel = null;
		PieceColor pieceColor = PieceColor.UNDEFINED;  // always random!

		// known opponent challenge ratings should be null
		Integer minRating = null;
		Integer maxRating = null;

		String opponentName;
		if (config.getOpponentName() == null || config.getOpponentName().equalsIgnoreCase(AppConstants.RANDOM)) {
			opponentName = null;
			minRating = config.getMinRating() == 0 ? null : config.getMinRating();
			maxRating = config.getMaxRating() == 0 ? null : config.getMaxRating();
		} else {
			opponentName = config.getOpponentName().toLowerCase();
		}

		final GameType gameType = GameType.Chess;
		Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
				getUser(),
				opponentName,
				gameType,
				pieceColor, rated, gameTimeConfig,
				minMembershipLevel, minRating, maxRating);

		FlurryAgent.logEvent(FlurryData.CHALLENGE_CREATED);
		liveConnectionHelper.runSendChallengeTask(challenge);
	}

	public void checkAndProcessDrawOffer(Game game) {

		if (isObservedGame(game)) {
			return;
		}

		final String opponentName = getOpponentName();

		if (opponentName != null && game.isDrawOfferedByPlayer(opponentName)) { // check if game is not ended?
			LogMe.dl(TAG, "GAME LISTENER: Draw offered at the move #" + game.getMoveCount() + ", game.id=" + game.getId()
					+ ", offerer=" + opponentName + ", game=" + game);

			if (!isGameActivityPausedMode()) {
				LogMe.dl(TAG, "DRAW SHOW");
				getLccEventListener().onDrawOffered(opponentName);
			}
		}
	}

	public void checkAndProcessEndGame(Game game) {
		List<GameResult> gameResults = game.getResults();
		LogMe.dl(TAG, "DEBUG: checkAndProcessEndGame game.getResults().size()=" + game.getResults().size() + ", game.isGameOver()=" + game.isGameOver());

		if (gameResults == null || gameResults.isEmpty()) {
			if (lccEventListener != null) {
				lccEventListener.expireGame();
			}
			return;
		}

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

		String message = Symbol.EMPTY;
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
				message = context.getString(R.string.won_by_resignation, winnerUsername);
				break;
			case ABORTED:
				message = context.getString(R.string.game_aborted);
				break;
		}
		//message = whiteUsername + " vs. " + blackUsername + " - " + message;
		LogMe.dl(TAG, "GAME LISTENER: " + message);

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
			getLccEventListener().onGameEnd(game, message);
		}

		if (TESTING_GAME && isMyGame(game)) {
			rematch();
		}
	}

	public void unObserveCurrentObservingGame() {
		if (getCurrentObservedGameId() != null) {
			runUnObserveGameTask(getCurrentObservedGameId());
		}
	}

	public void runUnObserveGameTask(Long gameId) {
		new UnObserveGameTask().execute(gameId);
	}

	private class UnObserveGameTask extends AsyncTask<Long, Void, Void> {

		@Override
		protected Void doInBackground(Long... params) {
			unObserveGame(params[0]);
			return null;
		}
	}

	public void runUnObserveOldTopGamesTask(Long exceptGameId) {
		new UnObserveOldTopGamesTask().execute(exceptGameId);
	}

	private class UnObserveOldTopGamesTask extends AsyncTask<Long, Void, Void> {

		@Override
		protected Void doInBackground(Long... params) {
			for (Game game : lccGames.values()) {
			   if (isObservedGame(game) && !game.getId().equals(params[0])) {
					LogMe.dl(TAG, "UnObserveOldTopGamesTask unobserve gameId=" + game.getId());
					unObserveGame(game.getId());
					lccClient.exitGame(game); // check, probably can avoid this
					lccGames.remove(game);
			   }
			}
			return null;
		}
	}

	public void subscribeToLccListeners() {
		lccClient.subscribeToChallengeEvents(challengeListener);
		lccClient.subscribeToGameEvents(gameListener);
		lccClient.subscribeToChatEvents(chatListener);
		lccClient.subscribeToFriendStatusEvents(friendStatusListener);
		lccClient.subscribeToUserList(LiveChessClient.UserListOrderBy.Username, 1, userListListener);
		lccClient.subscribeToAdminEvents(adminEventListener);
		lccClient.subscribeToAnnounces(announcementListener);
	}

	public LiveChessClient getClient() {
		return lccClient;
	}

	public LiveConnectionHelper getLiveConnectionHelper() {
		return liveConnectionHelper;
	}

	public MoveInfo getLatestMoveInfo() {
		return latestMoveInfo;
	}

	public void setLatestMoveInfo(MoveInfo setLatestMoveInfo) {
		this.latestMoveInfo = setLatestMoveInfo;
	}
}

class MoveInfo {
	private Long gameId;
	private String move;
	private long moveFirstThreadId = -1;
	private long moveSecondThreadId = -1;

	MoveInfo(Long gameId, String move, long moveFirstThreadId) {
		this.gameId = gameId;
		this.move = move;
		this.moveFirstThreadId = moveFirstThreadId;
	}

	public long getMoveFirstThreadId() {
		return moveFirstThreadId;
	}

	public void setMoveFirstThreadId(long moveFirstThreadId) {
		this.moveFirstThreadId = moveFirstThreadId;
	}

	public long getMoveSecondThreadId() {
		return moveSecondThreadId;
	}

	public void setMoveSecondThreadId(long moveSecondThreadId) {
		this.moveSecondThreadId = moveSecondThreadId;
	}

	public String getMove() {
		return move;
	}

	@Override
	public String toString() {
		return "move: " + move + ", game: " + gameId + ", firstThreadId: " + moveFirstThreadId + ", secondThreadId: " + moveSecondThreadId;
	}
}