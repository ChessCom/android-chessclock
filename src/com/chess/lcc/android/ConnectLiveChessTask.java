package com.chess.lcc.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.live.client.*;
import com.chess.live.client.impl.HttpClientProvider;
import com.chess.live.util.config.Config;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.utilities.LogMe;
import org.eclipse.jetty.client.HttpClient;

import java.io.IOException;
import java.util.LinkedHashSet;

/**
 * ConnectLiveChessTask class
 *
 * @author alien_roger
 * @created at: 11.06.12 20:35
 */
public class ConnectLiveChessTask extends AbstractUpdateTask<LiveChessClient, Void> {

	private static final String TAG = "LCCLOG-ConnectLiveChessTask";
	/*public static final String PKCS_12 = "PKCS12";
	public static final String TESTTEST = "testtest";
	public static final String KEY_FILE_NAME = "chesscom.pkcs12";*/

	//static MemoryUsageMonitor muMonitor = new MemoryUsageMonitor(15);

	public static final String LIVE_HOST_PRODUCTION = "chess.com";
	public static final String LIVE_HOST_TEST = "chess-2.com";

	//	public static final String AUTH_URL = RestHelper.CMD_LOGIN + "?username=%s&password=%s";
	final static Config CONFIG = new Config(Symbol.EMPTY, "assets/my.properties", true);
	public static final String CONFIG_URI =
			Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.bayeux.uri"), "/cometd");

	/*public static final String HOST = "10.0.2.2";
	  public static final String AUTH_URL = "http://" + HOST + "/api/v2/login?username=%s&password=%s";
	  public static final String CONFIG_BAYEUX_HOST = HOST;*/

	//Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.bayeux.host"), "live.chess-4.com");

	public static Integer PORT = 80;
	public static Integer PORT_SECURED = 443;

	/*public static final String CONFIG_AUTH_KEY =
			Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.user1.authKey"),
					"FIXED_PHPSESSID_WEBTIDE_903210957432054387723");*/

	public static final int MAX_BACKOFF_INTERVAL = 10000;
	public static final int WEB_SOCKET_MAX_MESSAGE_SIZE = 1048576; // 1mb

	private boolean useCurrentCredentials;

	private LccHelper lccHelper;

	public ConnectLiveChessTask(TaskUpdateInterface <LiveChessClient> taskFace, boolean useCurrentCredentials, LccHelper lccHelper) {
		this(taskFace, lccHelper);
		this.useCurrentCredentials = useCurrentCredentials;
	}

	public ConnectLiveChessTask(TaskUpdateInterface<LiveChessClient> taskFace, LccHelper lccHelper) {
		super(taskFace);
		this.lccHelper = lccHelper;

	}

	@Override
	protected Integer doTheTask(Void... params) {
		Context context = getTaskFace().getMeContext();

		try {

			String versionName = null;
			try {
				versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			} catch (PackageManager.NameNotFoundException e) {
				result = StaticData.UNKNOWN_ERROR;
			}
			versionName += ", OS: " + android.os.Build.VERSION.RELEASE + ", " + android.os.Build.MODEL;

//			InputStream keyStoreInputStream = context.getAssets().open(LccHelper.KEY_FILE_NAME);

			LinkedHashSet<ClientTransport> transports = new LinkedHashSet<ClientTransport>();
			int port = PORT;

			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO) { // Android 2.2
				LogMe.dl(TAG, "Support HTTP Live transport");
				transports.add(ClientTransport.HTTP);

			} else {
				LogMe.dl(TAG, "Support WS and HTTP Live transports");
				transports.add(ClientTransport.WS);
				transports.add(ClientTransport.HTTP);
			}

			LogMe.dl(TAG, "Start Chess.Com LCC ");
			LogMe.dl(TAG, "Connecting to: " + getConfigBayeuxHost() + ":" + port);

			item = LiveChessClientFacade.createClient(getAuthUrl(), getConfigBayeuxHost(),
					port, CONFIG_URI); // todo: check incorrect port connection failure
			item.setClientInfo("Android", versionName, "No-Key");

			item.setSupportedClientFeature(ClientFeature.AnnounceService, true);
			item.setSupportedClientFeature(ClientFeature.AdminService, true); // UPDATELCC todo: check

			item.setSupportedClientFeature(ClientFeature.GenericGameSupport, true);
			item.setSupportedClientFeature(ClientFeature.GenericChatSupport, true);
			item.setSupportedClientFeature(ClientFeature.GameObserve, true);

			item.setMaxBackoffInterval(MAX_BACKOFF_INTERVAL);
			item.setWebSocketMaxMessageSize(WEB_SOCKET_MAX_MESSAGE_SIZE);

			//PublicChatsBasic
			//PublicChatsFull
			//PrivateChats
			//MultiGames
			//MultiGameObserve
			//Tournaments
			//ExamineBoards
			//PingService

			HttpClient httpClient = HttpClientProvider.getHttpClient(HttpClientProvider.DEFAULT_CONFIGURATION, false);

			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
				httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
			} else {
				httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET); // Android 2.2
			}

			/*LogMe.dl(TAG, "INITIAL httpClient.getTimeout() = " + httpClient.getTimeout());
			LogMe.dl(TAG, "INITIAL httpClient.getSoTimeout() = " + httpClient.getSoTimeout());
			LogMe.dl(TAG, "INITIAL getIdleTimeout = " + httpClient.getIdleTimeout());
			LogMe.dl(TAG, "INITIAL httpClient.getConnectTimeout() = " + httpClient.getConnectTimeout());*/

			httpClient.setMaxConnectionsPerAddress(2);
			//httpClient.setSoTimeout(11000);
			httpClient.setConnectTimeout(10000); // 75000 is default
			httpClient.setTimeout(10000); // 320000 is default

			/*httpClient.setKeyStoreType(PKCS_12);
			httpClient.setTrustStoreType(PKCS_12);
			httpClient.setKeyManagerPassword(TESTTEST);
			httpClient.setKeyStoreInputStream(keyStoreInputStream);
			httpClient.setKeyStorePassword(TESTTEST);
			httpClient.setTrustStoreInputStream(keyStoreInputStream);
			httpClient.setTrustStorePassword(TESTTEST);*/

			//transports.add(ClientTransport.HTTP); // test
			//transports.add(ClientTransport.WS); // test

			item.setClientTransports(transports);

			item.setHttpClient(httpClient);

			httpClient.start();
		} catch (PackageManager.NameNotFoundException e) {
			result = StaticData.UNKNOWN_ERROR;
//            e.printStackTrace();
			Log.e(TAG, "Probably can't find pks12 file in assets" + e.toString());
		} catch (IOException e) {
//            e.printStackTrace();
			result = StaticData.NO_NETWORK;
			Log.e(TAG, e.toString());
		} catch (Exception e) {
			result = StaticData.UNKNOWN_ERROR;
			throw new LiveChessClientException("Unable to initialize HttpClient", e);
		}

		//lccHelper.setNetworkTypeName(null); // todo: probably reset networkTypeName somewhere else
		lccHelper.setLiveChessClient(item);
		lccHelper.performConnect(useCurrentCredentials);
		return StaticData.RESULT_OK;
	}

	private String getLiveHost() {
		String liveHost = RestHelper.getInstance().HOST.equals(RestHelper.HOST_PRODUCTION) ? LIVE_HOST_PRODUCTION : LIVE_HOST_TEST;
		return liveHost;
	}

	private String getAuthUrl() {
		return "http://www." + getLiveHost() + "/api/v2/login" + "?username=%s&password=%s";
	}

	private String getConfigBayeuxHost() {
		return "live." + getLiveHost();
	}

}