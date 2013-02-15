package com.chess.backend.tasks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.ClientFeature;
import com.chess.live.client.LiveChessClient;
import com.chess.live.client.LiveChessClientException;
import com.chess.live.client.LiveChessClientFacade;
import com.chess.live.client.impl.HttpClientProvider;
import com.chess.live.util.config.Config;
import org.eclipse.jetty.client.HttpClient;

import java.io.IOException;

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

	public static final String AUTH_URL = RestHelper.LOGIN + "?username=%s&password=%s";
	public static final String CONFIG_BAYEUX_HOST = "live." + RestHelper.HOST;
	final static Config CONFIG = new Config(StaticData.SYMBOL_EMPTY, "assets/my.properties", true);
	public static final String CONFIG_URI =
			Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.bayeux.uri"), "/cometd");

	/*public static final String HOST = "10.0.2.2";
	  public static final String AUTH_URL = "http://" + HOST + "/api/v2/login?username=%s&password=%s";
	  public static final String CONFIG_BAYEUX_HOST = HOST;*/

	//Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.bayeux.host"), "live.chess-4.com");
	public static final Integer CONFIG_PORT = 80;
	/*public static final String CONFIG_AUTH_KEY =
			Config.get(CONFIG.getString("live.chess.client.demo.chat_generator.connection.user1.authKey"),
					"FIXED_PHPSESSID_WEBTIDE_903210957432054387723");*/

	private boolean forceReenterCred;

	private LccHolder lccHolder;

	public ConnectLiveChessTask(TaskUpdateInterface<LiveChessClient> taskFace, boolean forceReenterCred, LccHolder lccHolder) {
		this(taskFace, lccHolder);
		this.forceReenterCred = forceReenterCred;
	}

	public ConnectLiveChessTask(TaskUpdateInterface<LiveChessClient> taskFace, LccHolder lccHolder) {
		super(taskFace);
		this.lccHolder = lccHolder;
	}

	@Override
	protected Integer doTheTask(Void... params) {
        Context context = taskFace.getMeContext();

		try {
			String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;

//			InputStream keyStoreInputStream = context.getAssets().open(LccHolder.KEY_FILE_NAME);
			Log.d(TAG, "Start Chess.Com LCC ");
			Log.d(TAG, "Connecting to: " + CONFIG_BAYEUX_HOST + ":" + CONFIG_PORT);

			item = LiveChessClientFacade.createClient(AUTH_URL, CONFIG_BAYEUX_HOST,
					CONFIG_PORT, CONFIG_URI);
			item.setClientInfo("Android", versionName, "No-Key");

			item.setSupportedClientFeature(ClientFeature.AnnounceService, true);
			item.setSupportedClientFeature(ClientFeature.AdminService, true); // UPDATELCC todo: check

			item.setSupportedClientFeature(ClientFeature.GenericGameSupport, true);
			item.setSupportedClientFeature(ClientFeature.GenericChatSupport, true);

			//PublicChatsBasic
			//PublicChatsFull
			//PrivateChats
			//MultiGames
			//GameObserve
			//MultiGameObserve
			//Tournaments
			//ExamineBoards
			//PingService

			HttpClient httpClient = HttpClientProvider.getHttpClient(HttpClientProvider.DEFAULT_CONFIGURATION, false);

			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
				httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
			} else {
				httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
			}

			httpClient.setMaxConnectionsPerAddress(4);
			httpClient.setSoTimeout(7000);
			httpClient.setConnectTimeout(10000);
			httpClient.setTimeout(7000); //

			/*httpClient.setKeyStoreType(PKCS_12);
			httpClient.setTrustStoreType(PKCS_12);
			httpClient.setKeyManagerPassword(TESTTEST);
			httpClient.setKeyStoreInputStream(keyStoreInputStream);
			httpClient.setKeyStorePassword(TESTTEST);
			httpClient.setTrustStoreInputStream(keyStoreInputStream);
			httpClient.setTrustStorePassword(TESTTEST);*/

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

		lccHolder.setLiveChessClient(item);
		lccHolder.performConnect(forceReenterCred);
		return StaticData.RESULT_OK;
	}
}
