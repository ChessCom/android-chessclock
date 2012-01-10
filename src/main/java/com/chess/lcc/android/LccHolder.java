package com.chess.lcc.android;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.chess.R;
import com.chess.live.client.*;
import com.chess.live.client.impl.HttpClientProvider;
import com.chess.live.util.GameTimeConfig;
import com.chess.live.util.config.Config;
import com.chess.model.GameListElement;

import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

public class LccHolder
{
  final static Config CONFIG = new Config("", "assets/my.properties", true);

  //static MemoryUsageMonitor muMonitor = new MemoryUsageMonitor(15);

	public static final String HOST = "chess.com";
	public static final String AUTH_URL = "http://www." + HOST + "/api/v2/login?username=%s&password=%s";
	public static final String CONFIG_BAYEUX_HOST = "live." + HOST;

  /*public static final String HOST = "10.0.2.2";
  public static final String AUTH_URL = "http://" + HOST + "/api/v2/login?username=%s&password=%s";
  public static final String CONFIG_BAYEUX_HOST = HOST;*/

  //Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.bayeux.host"), "live.chess-4.com");
  public static final Integer CONFIG_PORT = 80;
  public static final String CONFIG_URI =
    Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.bayeux.uri"), "/cometd");
  public static final String CONFIG_AUTH_KEY =
    Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.user1.authKey"),
               "FIXED_PHPSESSID_WEBTIDE_903210957432054387723");
  public static final String CONFIG_CHAT_MESSAGE =
    Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.message"), "test!");
  private static Integer MATCHED_COLOR = 1;
  private ChatListenerImpl _chatListener;
  private ConnectionListenerImpl _connectionListener;
  private LccGameListener _gameListener;
  private LiveChessClient _lccClient;
  private User _user;
  private UserSettings _settings;
  private ServerStats _serverStats;
  private Chat _mainChat;
  private static LccHolder INSTANCE;
  public static final Logger LOG = Logger.getLogger(LccHolder.class);
  private AndroidStuff android = new AndroidStuff(this);
  public static final int OWN_SEEKS_LIMIT = 3;
  private HashMap<Long, Challenge> challenges = new HashMap<Long, Challenge>();
  private final Hashtable<Long, Challenge> seeks = new Hashtable<Long, Challenge>();
  private HashMap<Long, Challenge> ownChallenges = new HashMap<Long, Challenge>();
  private Collection<? extends User> blockedUsers = new HashSet<User>();
  private Collection<? extends User> blockingUsers = new HashSet<User>();
  private final LccChallengeListener challengeListener;
  private final LccSeekListListener seekListListener;
  private final LccFriendStatusListener friendStatusListener;
  private final Hashtable<Long, Game> lccGames = new Hashtable<Long, com.chess.live.client.Game>();

  private final Map<String, User> friends = new HashMap<String, User>();
  private final Map<String, User> onlineFriends = new HashMap<String, User>();
  private SubscriptionId seekListSubscriptionId;
  private boolean connected;
  private boolean nextOpponentMoveStillNotMade;
  private final Object opponentClockStartSync = new Object();
  private Timer opponentClockDelayTimer = new Timer("OpponentClockDelayTimer", true);
  private ChessClock whiteClock;
  private ChessClock blackClock;
  private boolean connectingInProgress;
  private boolean activityPausedMode = true;
  private Map<GameEvent.Event, GameEvent> pausedActivityGameEvents = new HashMap<GameEvent.Event, GameEvent>();
  private Integer latestMoveNumber;
  private Long currentGameId;
  private final HashMap<Long, Chat> gameChats = new HashMap<Long, Chat>();
  private LinkedHashMap<Chat, LinkedHashMap<Long, ChatMessage>> receivedChatMessages =
    new LinkedHashMap<Chat, LinkedHashMap<Long, ChatMessage>>();
  public String networkTypeName;

  public LccHolder(InputStream keyStoreInputStream, String versionName)
  {
    Log.d("Chess.Com", "Start Chess.Com LCC App @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    //System.setProperty("java.net.preferIPv6Addresses", "false");
    LOG.info("Connecting to: " + CONFIG_BAYEUX_HOST + ":" + CONFIG_PORT);
    //InputStream keyStoreInputStream = null;
    /*try
    {
      keyStoreInputStream = new FileInputStream("/data/data/com.chess/chesscom.pkcs12");
    }
    catch(FileNotFoundException e)
    {
      e.printStackTrace();
    }*/

    _lccClient = LiveChessClientFacade.createClient(AUTH_URL, CONFIG_BAYEUX_HOST, CONFIG_PORT, CONFIG_URI);
    _lccClient.setClientInfo("Android", versionName, "No-Key");
    _lccClient.setSupportedClientFeatures(false, false);
    //HttpClient httpClient = _lccClient.setHttpClientConfiguration(HttpClientProvider.DEFAULT_CONFIGURATION);
    HttpClient httpClient = HttpClientProvider.getHttpClient(HttpClientProvider.DEFAULT_CONFIGURATION, false);
    //httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
    httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
    httpClient.setMaxConnectionsPerAddress(4);
    httpClient.setSoTimeout(7000);
    httpClient.setConnectTimeout(10000);
    httpClient.setTimeout(7000); //

    httpClient.setKeyStoreType("PKCS12");
    httpClient.setTrustStoreType("PKCS12");
    httpClient.setKeyManagerPassword("testtest");
    httpClient.setKeyStoreInputStream(keyStoreInputStream);
    httpClient.setKeyStorePassword("testtest");
    httpClient.setTrustStoreInputStream(keyStoreInputStream);
    httpClient.setTrustStorePassword("testtest");

    _lccClient.setHttpClient(httpClient);
    try
    {
      httpClient.start();
    }
    catch(Exception e)
    {
      throw new LiveChessClientException("Unable to initialize HttpClient", e);
    }

    _chatListener = new ChatListenerImpl(this);
    _connectionListener = new ConnectionListenerImpl(this);
    _gameListener = new LccGameListener(this);
    challengeListener = new LccChallengeListener(this);
    seekListListener = new LccSeekListListener(this);
    friendStatusListener = new LccFriendStatusListener(this);
  }

  public LccGameListener getGameListener()
  {
    return _gameListener;
  }

  public ChatListenerImpl getChatListener()
  {
    return _chatListener;
  }

  public ConnectionListenerImpl getConnectionListener()
  {
    return _connectionListener;
  }

  public Chat getMainChat()
  {
    return _mainChat;
  }

  public void setMainChat(Chat mainChat)
  {
    _mainChat = mainChat;
  }

  public User getUser()
  {
    return _user;
  }

  public void setUser(User user)
  {
    _user = user;
  }

  public LiveChessClient getClient()
  {
    return _lccClient;
  }

  public void setSettings(UserSettings settings)
  {
    _settings = settings;
  }

  public void setServerStats(ServerStats serverStats)
  {
    _serverStats = serverStats;
  }

  public boolean isConnected()
  {
    return connected;
  }

  public void setConnected(boolean connected)
  {
    this.connected = connected;
  }

  public long previousFGTime;
  public long currentFGTime;
  public long currentFGGameId;
  public long previousFGGameId;

  public static LccHolder getInstance(InputStream keyStoreInputStream, String versionName)
  {
    if(INSTANCE == null)
    {
      INSTANCE = new LccHolder(keyStoreInputStream, versionName);
    }
    return INSTANCE;
  }

  public AndroidStuff getAndroid()
  {
    return android;
  }

  public void clearChallenges()
  {
    challenges.clear();
    android.updateChallengesList();
  }

  public Challenge getChallenge(String challengeId)
  {
    return challenges.get(new Long(challengeId));
  }

  public void addOwnChallenge(Challenge challenge)
  {
    for(Challenge oldChallenge : ownChallenges.values())
    {
      if(challenge.getGameTimeConfig().getBaseTime().equals(oldChallenge.getGameTimeConfig().getBaseTime())
         && challenge.getGameTimeConfig().getTimeIncrement().equals(oldChallenge.getGameTimeConfig().getTimeIncrement())
         && challenge.isRated() == oldChallenge.isRated()
         && ((challenge.getTo() == null && oldChallenge.getTo() == null) ||
             (challenge.getTo() != null && challenge.getTo().equals(oldChallenge.getTo()))))
      {
        LOG.info("Check for doubled challenges: cancel challenge: " + oldChallenge);
        _lccClient.cancelChallenge(oldChallenge);
      }
    }
    ownChallenges.put(challenge.getId(), challenge);
  }

  public void storeBlockedUsers(Collection<? extends User> blockedUsers, Collection<? extends User> blockingUsers)
  {
    this.blockedUsers = blockedUsers;
    this.blockingUsers = blockingUsers;
  }

  public LccChallengeListener getChallengeListener()
  {
    return challengeListener;
  }

  public LccSeekListListener getSeekListListener()
  {
    return seekListListener;
  }

  public LccFriendStatusListener getFriendStatusListener()
  {
    return friendStatusListener;
  }

  public boolean isUserBlocked(String username)
  {
    if(blockedUsers != null)
    {
      for(User user : blockedUsers)
      {
        if(user.getUsername().equals(username)
           && user.isModerator() != null && !user.isModerator() && user.isStaff() != null && !user.isStaff())
        {
          return true;
        }
      }
    }
    if(blockingUsers != null)
    {
      for(User user : blockingUsers)
      {
        if(user.getUsername().equals(username) && !user.isModerator() && !user.isStaff())
        {
          return true;
        }
      }
    }
    return false;
  }

  /*public void removeOwnChallenge(Long challengeId)
  {
    //ownSeeksCount--;
    ownChallenges.remove(challengeId);
  }*/

  public int getOwnSeeksCount()
  {
    int ownSeeksCount = 0;
    for(Challenge challenge : ownChallenges.values())
    {
      if(challenge.isSeek())
      {
        ownSeeksCount++;
      }
    }
    return ownSeeksCount;
  }

  public void clearOwnChallenges()
  {
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
    LccUser.LOG.info("SeekConnection issue seek: " + seek);
    Challenge challenge = mapJinSeekToLccChallenge(seek);
    //outgoingLccSeeks.add(challenge);
    lccUser.getClient().sendChallenge(challenge, lccUser.getChallengeListener());
  }*/

  public boolean isUserPlaying()
  {
    for(com.chess.live.client.Game game : lccGames.values())
    {
      if(!game.isEnded())
      {
        return true;
      }
    }
    return false;
  }

  public boolean isUserPlayingAnotherGame(Long currentGameId)
  {
    for(com.chess.live.client.Game game : lccGames.values())
    {
      if(!game.getId().equals(currentGameId) && !game.isEnded())
      {
        return true;
      }
    }
    return false;
  }

  public void putChallenge(Long challengeId, Challenge lccChallenge)
  {
    challenges.put(challengeId, lccChallenge);
    android.updateChallengesList();
  }

  public void removeChallenge(String challengeId)
  {
    removeChallenge(new Long(challengeId));
  }

  public void removeChallenge(Long challengeId)
  {
    challenges.remove(challengeId);
    ownChallenges.remove(challengeId);
    android.updateChallengesList();
  }

  public void putSeek(Challenge challenge)
  {
    seeks.put(challenge.getId(), challenge);
    android.updateChallengesList();
  }

  public void setFriends(Collection<? extends User> friends)
  {
    LOG.info("CONNECTION: get friends list: " + friends);
    if(friends == null)
    {
      return;
    }
    for(User friend : friends)
    {
      putFriend(friend);
    }
  }

  public void putFriend(User friend)
  {
    if(friend.getStatus() != com.chess.live.client.User.Status.OFFLINE)
    {
      onlineFriends.put(friend.getUsername(), friend);
      this.friends.put(friend.getUsername(), friend);
    }
    else
    {
      onlineFriends.remove(friend.getUsername());
      this.friends.remove(friend.getUsername());
    }
  }

  public void removeFriend(User friend)
  {
    friends.remove(friend.getUsername());
    onlineFriends.remove(friend.getUsername());
  }

  public String[] getOnlineFriends()
  {
    final String[] array = new String[]{""};
    return onlineFriends.size() !=0 ? onlineFriends.keySet().toArray(array) : array;
  }

  public SubscriptionId getSeekListSubscriptionId()
  {
    return seekListSubscriptionId;
  }

  public void setSeekListSubscriptionId(SubscriptionId seekListSubscriptionId)
  {
    this.seekListSubscriptionId = seekListSubscriptionId;
  }

  public void putGame(Game lccGame)
  {
    lccGames.put(lccGame.getId(), lccGame);
  }

  public Game getGame(String gameId)
  {
    return getGame(new Long(gameId));
  }

  public Game getGame(Long gameId)
  {
    return lccGames.get(gameId);
  }

  public void clearSeeks()
  {
    seeks.clear();
    android.updateChallengesList();
  }

  /*public void removeGame(Long id)
  {
    lccGames.remove(id);
  }*/

  public void clearGames()
  {
    lccGames.clear();
  }

  public ArrayList<GameListElement> getChallengesAndSeeksData()
  {
    ArrayList<GameListElement> output = new ArrayList<GameListElement>();

    final Collection<Challenge> challengesAndSeeks = new ArrayList<Challenge>();
    challengesAndSeeks.addAll(challenges.values());
    challengesAndSeeks.addAll(seeks.values());

    boolean isReleasedByMe;
    for(Challenge challenge : challengesAndSeeks)
    {
      String[] challengeData = new String[10];
      final User challenger = challenge.getFrom();
      isReleasedByMe = challenger.getUsername().equals(_user.getUsername());
      final GameTimeConfig challengerTimeConfig = challenge.getGameTimeConfig();
      challengeData[0] = "" + challenge.getId();
      challengeData[1] = isReleasedByMe ? challenge.getTo() : challenger.getUsername();
      Integer challengerRating = 0;
      if (!isReleasedByMe)
      {
        switch(challengerTimeConfig.getGameTimeClass())
        {
          case BLITZ:
          {
            challengerRating = challenger.getBlitzRating();
            break;
          }
          case LIGHTNING:
          {
            challengerRating = challenger.getQuickRating();
            break;
          }
          case STANDARD:
          {
            challengerRating = challenger.getStandardRating();
            break;
          }
        }
        if(challengerRating == null)
        {
          challengerRating = 0;
        }
      }
      challengeData[2] = "" + challengerRating;
      final String challengerChessTitle =
        challenger.getChessTitle() != null && !isReleasedByMe  ? "(" + challenger.getChessTitle() + ")" : "";
      challengeData[3] = challengerChessTitle;
      String color = null;
      switch(challenge.getColor())
      {
        case UNDEFINED:
          color = "0";
          break;
        case WHITE:
          color = "1";
          break;
        case BLACK:
          color = "2";
          break;
        default:
          color = "0";
          break;
      }
      challengeData[4] = color;
      challengeData[5] = challenge.isRated() ? "" : "Unrated"; // is_rated

      /*int time = challengerTimeConfig.getBaseTime() * 100;
      int hours = time / (1000 * 60 * 60);
      time -= hours * 1000 * 60 * 60;
      int minutes = time / (1000 * 60);*/
      challengeData[6] = (challengerTimeConfig.getBaseTime() / 10 / 60) + "min"; // base_time

      //challengeData[6] = (challengerTimeConfig.getBaseTime() / 10) + "sec"; // base_time
      challengeData[7] = challengerTimeConfig.getTimeIncrement() != 0 ?
                         "+" + (challengerTimeConfig.getTimeIncrement() / 10) + "sec" : ""; // time_increment
      challengeData[8] = challenge.getTo() != null ? "1" : "0"; // is_direct_challenge
      challengeData[9] = isReleasedByMe ? "1" : "0";
      output.add(new GameListElement(0, challengeData, true));
    }
    return output;
  }

  public String[] getGameData(String gameId, int moveIndex)
  {
    final com.chess.live.client.Game lccGame = getGame(gameId);
    final String[] gameData = new String[com.chess.model.Game.GAME_DATA_ELEMENTS_COUNT];
    gameData[0] = lccGame.getId().toString();
    gameData[1] = "1";
    gameData[2] = "" + System.currentTimeMillis(); // todo, resolve "timestamp"
    gameData[3] = "";
    gameData[4] = lccGame.getWhitePlayer().getUsername().trim();
    gameData[5] = lccGame.getBlackPlayer().getUsername().trim();
    gameData[6] = ""; // starting_fen_position
    String moves = new String();
    /*int j = 0;
    int latest = 0;
    for (int i=0; j <= moveIndex; i++)
    {
      if (lccGame.getMovesInSanNotation().charAt(i) == ' ')
      {
        j++;
        latest = i;
      }
    }
    if (j!=0)
    {
      moves = lccGame.getMovesInSanNotation().substring(0, latest);
    }
    else
    {
      moves = lccGame.getMovesInSanNotation();
    }*/

    /*String [] movesArray = lccGame.getMovesInSanNotation().split(" ");
    for (int i=0; i<=moveIndex; i++)
    {
      moves += movesArray[i]+" ";
    }*/

    final Iterator movesIterator = lccGame.getMoves().iterator();
    for (int i=0; i<=moveIndex; i++)
    {
      moves += movesIterator.next() + " ";
    }
    if (moveIndex==-1)
    {
      moves = "";
    }
    gameData[7] = moves; // move_list

    gameData[8] = ""; // user_to_move

    Integer whiteRating = 0;
    Integer blackRating = 0;
    switch(lccGame.getGameTimeConfig().getGameTimeClass())
    {
      case BLITZ:
      {
        whiteRating = lccGame.getWhitePlayer().getBlitzRating();
        blackRating = lccGame.getBlackPlayer().getBlitzRating();
        break;
      }
      case LIGHTNING:
      {
        whiteRating = lccGame.getWhitePlayer().getQuickRating();
        blackRating = lccGame.getBlackPlayer().getQuickRating();
        break;
      }
      case STANDARD:
      {
        whiteRating = lccGame.getWhitePlayer().getStandardRating();
        blackRating = lccGame.getBlackPlayer().getStandardRating();
        break;
      }
    }
    if(whiteRating == null)
    {
      whiteRating = 0;
    }
    if(blackRating == null)
    {
      blackRating = 0;
    }

    gameData[9] = whiteRating.toString();
    gameData[10] = blackRating.toString();

    gameData[11] = ""; // todo: encoded_move_string
    gameData[12] = ""; // has_new_message
    gameData[13] = "" + (lccGame.getGameTimeConfig().getBaseTime() / 10); // seconds_remaining

    return gameData;
  }

  public void makeMove(String gameId, String move)
  {
    final Game game = getGame(gameId);
    /*if(chessMove.isCastling())
    {
      lccMove = chessMove.getWarrenSmithString().substring(0, 4);
    }
    else
    {
      lccMove = move.getMoveString();
      lccMove = chessMove.isPromotion() ? lccMove.replaceFirst("=", "") : lccMove;
    }*/
    final long delay = game.getOpponentClockDelay() * 100;
    synchronized(opponentClockStartSync)
    {
      nextOpponentMoveStillNotMade = true;
    }
    try
    {
      LOG.info("MOVE: making move: gameId=" + game.getId() + ", move=" + move + ", delay=" + delay);
      _lccClient.makeMove(game, move);
      if(game.getSeq() >= 1) // we should start opponent's clock after at least 2-nd ply (seq == 1, or seq > 1)
      {
        final boolean isWhiteRunning =
          _user.getUsername().equals(game.getWhitePlayer().getUsername());
        final ChessClock clockToBePaused = isWhiteRunning ? whiteClock : blackClock;
        final ChessClock clockToBeStarted = isWhiteRunning ? blackClock : whiteClock;
        if(game.getSeq() >= 2) // we should stop our clock if it was at least 3-rd ply (seq == 2, or seq > 2)
        {
          clockToBePaused.setRunning(false);
        }
        synchronized(opponentClockStartSync)
        {
          if(nextOpponentMoveStillNotMade)
          {
            opponentClockDelayTimer.schedule(new TimerTask()
            {
              public void run()
              {
                synchronized(opponentClockStartSync)
                {
                  if(nextOpponentMoveStillNotMade)
                  {
                    clockToBeStarted.setRunning(true);
                  }
                }
              }
            }, delay);
          }
        }
      }
    }
    catch(IllegalArgumentException e)
    {
      //fireGameEvent(new IllegalMoveEvent(this, null, game, move, IllegalMoveEvent.ILLEGAL_MOVE));
    }
  }

  public void setNextOpponentMoveStillNotMade(boolean nextOpponentMoveStillNotMade)
  {
    this.nextOpponentMoveStillNotMade = nextOpponentMoveStillNotMade;
  }

  public ChessClock getBlackClock()
  {
    return blackClock;
  }

  public ChessClock getWhiteClock()
  {
    return whiteClock;
  }

  public void setWhiteClock(ChessClock whiteClock)
  {
    this.whiteClock = whiteClock;
  }

  public void setBlackClock(ChessClock blackClock)
  {
    this.blackClock = blackClock;
  }

  public void logout()
  {
    LOG.info("USER LOGOUT");
    android.getContext().setLiveChess(false);
    setCurrentGameId(null);
    setUser(null);
    android.closeLoggingInIndicator();
    android.closeReconnectingIndicator();
    getAndroid().runDisconnectTask();
    setConnected(false);
    setConnectingInProgress(false);
    clearGames();
    clearChallenges();
    clearOwnChallenges();
    clearSeeks();
    setNetworkTypeName(null);
  }

  public boolean isConnectingInProgress()
  {
    return connectingInProgress;
  }

  public void setConnectingInProgress(boolean connectingInProgress)
  {
    this.connectingInProgress = connectingInProgress;
  }

  public boolean isSeekContains(Long id)
  {
    return seeks.containsKey(id);
  }

  public void removeSeek(Long id)
  {
    if(seeks.size() > 0)
    {
      seeks.remove(id);
    }
    ownChallenges.remove(id);
    android.updateChallengesList();
  }

  public void removeSeek(String id)
  {
    removeSeek(new Long(id));
  }

  public Challenge getSeek(String id)
  {
    return seeks.get(new Long(id));
  }

  public boolean isActivityPausedMode()
  {
    return activityPausedMode;
  }

  public void setActivityPausedMode(boolean activityPausedMode)
  {
    this.activityPausedMode = activityPausedMode;
  }

  public Map<GameEvent.Event, GameEvent> getPausedActivityGameEvents()
  {
    return pausedActivityGameEvents;
  }

  public void processFullGame(Game game)
  {
    latestMoveNumber = null;
    putGame(game);
    int time = game.getGameTimeConfig().getBaseTime() * 100;
    if (whiteClock != null /*&& game.getWhitePlayer().getUsername().equals(game.getWhitePlayer().getUsername())*/
        && whiteClock.isRunning())
    {
      whiteClock.setRunning(false);
    }
    if (blackClock != null /*&& game.getBlackPlayer().getUsername().equals(game.getBlackPlayer().getUsername())*/
        && blackClock.isRunning())
    {
      blackClock.setRunning(false);
    }
    setWhiteClock(new ChessClock(this, true, time));
    setBlackClock(new ChessClock(this, false, time));
    final Activity activity = getAndroid().getGameActivity();
    if (activity != null)
    {
      activity.finish();
    }
    final ContextWrapper androidContext = android.getContext();
    final Intent intent = new Intent(androidContext, com.chess.activities.Game.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra("mode", 4);
    intent.putExtra("game_id", "" + game.getId());
    androidContext.startActivity(intent);
    /*final Game currentGame = game;
    if(game.getSeq() > 0)
    {
      android.getUpdateBoardHandler().postDelayed(new Runnable()
      {
        public void run()
        {
          doReplayMoves(currentGame);
        }
      }, 2000); // todo: remove delay, change logic to use SerializableExtra() probably, process moves replay on the Game activity
    }*/
  }

  public void doReplayMoves(Game game)
  {
    LOG.info("GAME LISTENER: replay moves,  gameId " + game.getId());
    //final String[] sanMoves = game.getMovesInSanNotation().trim().split(" ");
    final List<String> coordMoves = new ArrayList<String>(game.getMoves());
    User whitePlayer = game.getWhitePlayer();
    User blackPlayer = game.getBlackPlayer();
    User moveMaker;
    for(int i = 0; i < coordMoves.size(); i++)
    {
      moveMaker = (i % 2 == 0) ? whitePlayer : blackPlayer;
      doMoveMade(game, moveMaker, coordMoves.get(i), i);
    }
  }

  public void doMoveMade(final Game game, final User moveMaker, String move, /*boolean isNew,*/ int moveIndex)
  {
    /*if(move.length() == 5)
    {
      final String promotionSign = move.substring(4, 5);
      promotion = game.getWhitePlayer().getUsername().equals(moveMaker.getUsername()) ?
                  variant.parsePiece(promotionSign.toUpperCase()) : variant.parsePiece(promotionSign);
    }
    else
    {
      promotion = null;
    }*/
    //final String sanMove = game.getMovesInSanNotation().trim().split(" ")[moveIndex];
    // todo

    if(((latestMoveNumber != null) && (moveIndex < latestMoveNumber)) || (latestMoveNumber == null && moveIndex > 0))
    {
      LOG.info("GAME LISTENER: Extra onMoveMade received (currentMoveIndex=" + moveIndex + ", latestMoveNumber=" + latestMoveNumber + ")");
      return;
    }
    else
    {
      latestMoveNumber = moveIndex;
    }
    if (isActivityPausedMode())
    {
      final GameEvent moveEvent = new GameEvent();
      moveEvent.event = GameEvent.Event.Move;
      moveEvent.gameId = game.getId();
      moveEvent.moveIndex = moveIndex;
      getPausedActivityGameEvents().put(moveEvent.event, moveEvent);
    }
    else
    {
      android.processMove(game.getId(), moveIndex);
    }
    doUpdateClocks(game, moveMaker, moveIndex);
  }

  private void doUpdateClocks(Game game, User moveMaker, int moveIndex)
  {
    // TODO: This method does NOT support the game observer mode. Redevelop it if necessary.
    setClockDrawPointer(!game.getWhitePlayer().getUsername().equals(moveMaker.getUsername()));
    if(game.getSeq() >= 2 && moveIndex == game.getSeq() - 1)
    {
      final boolean isOpponentMoveDone = !_user.getUsername().equals(moveMaker.getUsername());
      if(isOpponentMoveDone)
      {
        synchronized(opponentClockStartSync)
        {
          setNextOpponentMoveStillNotMade(false);
        }
      }
      //final boolean amIWhite = _user.getUsername().equals(game.getWhitePlayer().getUsername());
      /*final boolean updateWhite = isOpponentMoveDone || amIWhite;
      final boolean updateBlack = isOpponentMoveDone || !amIWhite;*/
      final boolean isWhiteDone = game.getWhitePlayer().getUsername().equals(moveMaker.getUsername());
      final boolean isBlackDone = game.getBlackPlayer().getUsername().equals(moveMaker.getUsername());
      final int whitePlayerTime = game.getActualClockForPlayer(game.getWhitePlayer()).intValue() * 100;
      final int blackPlayerTime = game.getActualClockForPlayer(game.getBlackPlayer()).intValue() * 100;
      /*if(updateWhite)
      {*/
      final ChessClock whiteClock = getWhiteClock();
      /*System.out.println("@@@@@@@@@@@@@@@@@@@@ whitePlayerTime " + whitePlayerTime);
      System.out.println("@@@@@@@@@@@@@@@@@@@@ " + whiteClock.createTimeString(whitePlayerTime));*/
        whiteClock.setTime(whitePlayerTime);
        if (!game.isEnded())
        {
          whiteClock.setRunning(isBlackDone);
        }
      //}
      /*if(updateBlack)
      {*/
      final ChessClock blackClock = getBlackClock();
      /*System.out.println("@@@@@@@@@@@@@@@@@@@@ blackPlayerTime " + blackPlayerTime);
      System.out.println("@@@@@@@@@@@@@@@@@@@@ " + blackClock.createTimeString(blackPlayerTime));*/
        blackClock.setTime(blackPlayerTime);
        if (!game.isEnded())
        {
          blackClock.setRunning(isWhiteDone);
        }
      //}
    }
  }

  public void updateClockTime(Game game)
  {
    /*int whitePlayerTime = game.getActualClockForPlayer(game.getWhitePlayer()).intValue() * 100;
    int blackPlayerTime = game.getActualClockForPlayer(game.getBlackPlayer()).intValue() * 100;
    System.out.println("!!!!!!!!!!!!!!!!!!!! WHITE TIME " + getWhiteClock().createTimeString(whitePlayerTime));
    System.out.println("!!!!!!!!!!!!!!!!!!!! BLACK TIME " + getBlackClock().createTimeString(blackPlayerTime));
    getWhiteClock().setTime(whitePlayerTime);
    getBlackClock().setTime(blackPlayerTime);*/
  }

  public void setClockDrawPointer(final Boolean isWhite)
  {
    if (getAndroid().getGameActivity() == null)
    {
      /*throw new NullPointerException("lastFG=" + (System.currentTimeMillis()-currentFGTime)/1000 + ", " +
                                     "t2-t1=" + (previousFGTime-currentFGTime)/1000 + ", " +
                                     "id1=" + previousFGGameId + ", " +
                                     "id2=" + currentFGGameId);*/
      return;
    }
    getAndroid().getGameActivity().runOnUiThread(new Runnable()
    {
      public void run()
      {
        if (isWhite == null)
        {
          getAndroid().getGameActivity().getWhiteClockView().
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
          getAndroid().getGameActivity().getBlackClockView().
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
          return;
        }
        int leftDrawableForBlack = 0;
        int rightDrawableForBlack = 0;
        final Configuration configuration = getAndroid().getContext().getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
        {
          leftDrawableForBlack = R.drawable.blackmove;
          rightDrawableForBlack = 0;
        }
        else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
          leftDrawableForBlack = 0;
          rightDrawableForBlack = R.drawable.blackmove;
        }
        if(getAndroid().getGameActivity() != null)
        {
          if(isWhite)
          {
            getAndroid().getGameActivity().getWhiteClockView().
              setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.whitemove, 0);
            getAndroid().getGameActivity().getBlackClockView().
              setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
          }
          else
          {
            getAndroid().getGameActivity().getWhiteClockView().
              setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            getAndroid().getGameActivity().getBlackClockView().
              setCompoundDrawablesWithIntrinsicBounds(leftDrawableForBlack, 0, rightDrawableForBlack, 0);
          }
        }
      }
    });
  }

  public void setCurrentGameId(Long gameId)
  {
    currentGameId = gameId;
  }

  public Long getCurrentGameId()
  {
    return currentGameId;
  }

  public void putGameChat(Long gameId, Chat chat)
  {
    gameChats.put(gameId, chat);
  }

  public Chat getGameChat(Long gameId)
  {
    return gameChats.get(gameId);
  }

  public LinkedHashMap<Chat, LinkedHashMap<Long, ChatMessage>> getReceivedChats()
  {
    return receivedChatMessages;
  }

  public LinkedHashMap<Long, ChatMessage> getChatMessages(String chatId)
  {
    for(Chat storedChat : receivedChatMessages.keySet())
    {
      if(chatId.equals(storedChat.getId()))
      {
        return receivedChatMessages.get(storedChat);
      }
    }
    return null;
  }

  public void setNetworkTypeName(String networkTypeName)
  {
    this.networkTypeName = networkTypeName;
  }

  public String getNetworkTypeName()
  {
    return networkTypeName;
  }

	public Boolean isFairPlayRestriction(String gameId)
	{
		final com.chess.live.client.Game game = getGame(gameId);
		if (game.getWhitePlayer().getUsername().equals(_user.getUsername()) && !game.isAbortableByWhitePlayer())
		{
			return true;
		}
		if (game.getBlackPlayer().getUsername().equals(_user.getUsername()) && !game.isAbortableByBlackPlayer())
		{
			return true;
		}
		return false;
	}
	
	public Boolean isAbortableBySeq(String gameId)
	{
		return getGame(gameId).getSeq() < 3;
	}
}
