package com.chess.utilities;

import android.content.Context;
import android.os.AsyncTask;
import com.chess.backend.LoadItem;
import com.googlecode.jpingy.PingArguments;
import com.googlecode.jpingy.PingRequest;
import com.googlecode.jpingy.PingResult;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 28.10.13
 * Time: 16:40
 */
public class Ping {

	public static final String TAG = "LccLog-Ping";

	public static final String PING_ERROR = "ping error";
	private static final String NO_NETWORK = "no network";
	private static final String NO_PING = "no ping";
	private static final int TIMEOUT = 4000;
	private static final int INTERVAL = 1000;
	private static final int BYTES = 32;
	private static final int TIMER_UPDATE_PERIOD = 5000;
	private static final String HOST_LIVE_CHESS = "live.chess.com";
	private static final int COUNT = 1;
	public static final String TEST_REQUEST_LIVE_URL = "http://live.chess.com/static/testimg.gif";
	public static final String TEST_REQUEST_LIVE_SECURED_URL = "https://live.chess.com/static/testimg.gif";
	public static final String TEST_REQUEST_GOOGLE_URL = "http://www.google.com/favicon.ico";
	public static final String TEST_REQUEST_CHESS_URL = "http://www.chess.com/favicon.ico";
	public static final String TEST_REQUEST_CHESS_SECURED_URL = "https://www.chess.com/favicon.ico";
	public static final String TEST_REQUEST_CHESS3_URL = "http://www.chess-3.com/images/blurbarrow.gif";
	public static final String TEST_REQUEST_AKIBA = "http://akiba.chess.com/static/testimg.gif";
	public static final String TEST_REQUEST_AKIBA_SECURED = "https://akiba.chess.com/static/testimg.gif";
	public static final String TEST_REQUEST_HAMMER = "http://hammer.chess.com/static/testimg.gif";
	public static final String TEST_REQUEST_HAMMER_SECURED = "https://hammer.chess.com/static/testimg.gif";
	/*public static final String TEST_REQUEST_IVANCHUK = "http://ivanchuk.chess.com/static/testimg.gif";
	public static final String TEST_REQUEST_IVANCHUK_SECURED = "https://ivanchuk.chess.com/static/testimg.gif";*/
	public static final String TEST_REQUEST_LIVE_8080 = "http://live.chess.com:8080/static/testimg.gif";
	public static final String TEST_REQUEST_IP = "http://67.201.34.165/static/testimg.gif";
	public static final String TEST_REQUEST_IP_8080 = "http://67.201.34.165:8080/static/testimg.gif";
	public static final String TEST_REQUEST_ZEROLAG = "http://www.zerolag.com/wp-content/uploads/2012/03/zerolag-logo.png";

	private final Context context;

	private Timer pingTimer;
	private Timer requestTimer;

	public Ping(Context context) {
		this.context = context;
	}

	public String ping(String host, int count, String marker) {

		if (!AppUtils.isNetworkAvailable(context)) {
			//LogMe.dl(TAG, NO_NETWORK);
			return NO_NETWORK;
		} else {
			LogMe.dl(TAG, "PING " + host + " START, id=" + marker);
		}

		PingArguments arguments =
				new PingArguments.Builder().url(host).timeout(TIMEOUT).count(count).bytes(BYTES).interval(INTERVAL).build();

		String pingStats = "";

		try {

			PingResult results = com.googlecode.jpingy.Ping.ping(arguments, com.googlecode.jpingy.Ping.Backend.UNIX);

			if (results != null) {

				List<PingRequest> requests = results.getRequests();

				if (count > 1) {
					for (PingRequest request : requests) {
						LogMe.dl(TAG, "request: " + request);
						LogMe.dl(TAG, "TIME: " + request.time());
						LogMe.dl(TAG, "from: " + request.from());
						LogMe.dl(TAG, "bytes: " + request.bytes());
						LogMe.dl(TAG, "fromIP: " + request.fromIP());
						LogMe.dl(TAG, "reqNr: " + request.reqNr());
						LogMe.dl(TAG, "ttl: " + request.ttl());
					}
				}

				int lost = results.transmitted() - results.received();
				pingStats = "lost=" + lost + ", " + results.toString();

				/*LogMe.dl(TAG, "rtt_avg: " + results.rtt_avg());
				LogMe.dl(TAG, "rtt_min: " + results.rtt_min());
				LogMe.dl(TAG, "rtt_max: " + results.rtt_max());
				LogMe.dl(TAG, "ttl: " + results.ttl());
				LogMe.dl(TAG, "received: " + results.received());
				LogMe.dl(TAG, "address: " + results.address());
				LogMe.dl(TAG, "transmitted: " + results.transmitted());
				//LogMe.dl(TAG, "time: " + results.time());
				LogMe.dl(TAG, "payload: " + results.payload());
				//LogMe.dl(TAG, "rtt_mdev: " + results.rtt_mdev());*/
			} else {
				return NO_PING;
			}

		} catch (Exception e) {
			//return PING_ERROR;
		}

		return pingStats;
	}

	public void pingLive() {
		String marker = String.valueOf(System.currentTimeMillis());
		String ping = ping(HOST_LIVE_CHESS, COUNT, marker);
		LogMe.dl(TAG, "ping result: " + ping + ", id=" + marker);
	}

	public void runPingLiveTimer() {

		if (pingTimer != null) {
			return;
		}

		pingTimer = new Timer();
		pingTimer.schedule(new TimerTask() {
			@Override
			public void run() {

				pingLive();

			}
		}, 0, TIMER_UPDATE_PERIOD);

	}

	public void stopPingLiveTimer() {
		if (pingTimer != null) {
			LogMe.dl(TAG, "stop ping timer");
			pingTimer.cancel();
			pingTimer = null;
		}
	}

	public void runTestRequestsTimer() {

		if (requestTimer != null) {
			return;
		}

		requestTimer = new Timer();
		requestTimer.schedule(new TimerTask() {
			@Override
			public void run() {

				testRequestGoogleServer();
				testRequestLiveServer();
				testRequestLiveSecuredServer();
				testRequestChessServer();
				testRequestChessSecuredServer();
				testRequestChess3Server();

				testRequestAkiba();
				testRequestAkibaSecured();
				testRequestHammer();
				testRequestHammerSecured();

				testRequestLiveIp();
				testRequestZerolag();

			}
		}, 0, TIMER_UPDATE_PERIOD);

	}

	public void stopTestRequestsTimer() {
		if (requestTimer != null) {
			LogMe.dl(TAG, "stop request timer");
			requestTimer.cancel();
			requestTimer = null;
		}
	}

	public void testRequestGoogleServer() {
		testRequestServer(TEST_REQUEST_GOOGLE_URL);
	}

	public void testRequestLiveServer() {
		testRequestServer(TEST_REQUEST_LIVE_URL);
	}

	public void testRequestLiveSecuredServer() {
		testRequestServer(TEST_REQUEST_LIVE_SECURED_URL);
	}

	public void testRequestChessServer() {
		testRequestServer(TEST_REQUEST_CHESS_URL);
	}

	public void testRequestChessSecuredServer() {
		testRequestServer(TEST_REQUEST_CHESS_SECURED_URL);
	}

	public void testRequestChess3Server() {
		testRequestServer(TEST_REQUEST_CHESS3_URL);
	}

	public void testRequestAkiba() {
		testRequestServer(TEST_REQUEST_AKIBA);
	}

	public void testRequestAkibaSecured() {
		testRequestServer(TEST_REQUEST_AKIBA_SECURED);
	}

	public void testRequestHammer() {
		testRequestServer(TEST_REQUEST_HAMMER);
	}

	public void testRequestHammerSecured() {
		testRequestServer(TEST_REQUEST_HAMMER_SECURED);
	}

	/*public void testRequestIvanchuk() {
		testRequestServer(TEST_REQUEST_IVANCHUK);
	}

	public void testRequestIvanchukSecured() {
		testRequestServer(TEST_REQUEST_IVANCHUK_SECURED);
	}*/

	public void testRequestLive8080() {
		testRequestServer(TEST_REQUEST_LIVE_8080);
	}

	public void testRequestLiveIp() {
		testRequestServer(TEST_REQUEST_IP);
	}

	public void testRequestLiveIp_8080() {
		testRequestServer(TEST_REQUEST_IP_8080);
	}

	public void testRequestZerolag() {
		testRequestServer(TEST_REQUEST_ZEROLAG);
	}

	public void testRequestServer(String url) {
		long marker = System.currentTimeMillis();
		LogMe.dl(TAG, "REQUEST " + url + ", id=" + marker);
		String response = requestUrl(url);

		if (response != null) {
			String truncatedResponse = response.length() > 0 ? response.substring(0, 10) : response;
			LogMe.dl(TAG, "     OK " + url + ", id=" + marker + ", response=" + truncatedResponse); // do not flood logs, just print first 10 symbols
		} else {
			LogMe.dl(TAG, "     ERROR " + url + ", id=" + marker);
		}
	}

	public String requestUrl(String url) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(url);
		return request(loadItem);
	}

	private String request(LoadItem loadItem) {
		String url = loadItem.getLoadPath();

		String resultString;
		Long time;
		HttpURLConnection connection = null;

		try {

			HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
			SSLContext context = SSLContext.getInstance("TLS");

			context.init(null, new X509TrustManager[]{new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			}}, new SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

			URL urlObj = new URL(url);
			connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);

			/*if (IS_TEST_SERVER_MODE) {
				connection.setRequestProperty("Authorization", getBasicAuth());
			}*/

			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + HTTP.UTF_8);

			int statusCode;
			statusCode = connection.getResponseCode();

			if (statusCode != HttpStatus.SC_OK) {
				LogMe.dl(TAG, "ERROR " + statusCode + " while retrieving data from " + url);
				InputStream inputStream = connection.getErrorStream();
				resultString = convertStreamToString(inputStream);
				LogMe.dl(TAG, "server ERROR response: " + resultString);
				return null;
			}

			InputStream inputStream = null;
			try {
				inputStream = connection.getInputStream();
				resultString = convertStreamToString(inputStream);

				return resultString;

			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}

		} catch (Exception e) {
			LogMe.dl(TAG, e.getMessage());
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		//return true;
	}

	private static String convertStreamToString(InputStream is) {
		Scanner scanner = new Scanner(is).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}

	public void runPingLiveTask() {
			new PingLiveTask().execute();
		}

	private class PingLiveTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {
			pingLive();
			return null;
		}
	}

	public void runTestRequestLiveServerTask() {
			new TestRequestLiveServerTask().execute();
	}

	private class TestRequestLiveServerTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... voids) {

			testRequestGoogleServer();
			testRequestLiveServer();
			testRequestLiveSecuredServer();
			testRequestChessServer();
			testRequestChessSecuredServer();
			testRequestChess3Server();

			testRequestAkiba();
			testRequestAkibaSecured();
			testRequestHammer();
			testRequestHammerSecured();

			testRequestLiveIp();
			testRequestZerolag();

			return null;
		}
	}

	class NullHostNameVerifier implements HostnameVerifier {

		public boolean verify(String hostname, SSLSession session) {
			//LogMe.dl(TAG, "Approving certificate for " + hostname);
			return true;
		}
	}
}