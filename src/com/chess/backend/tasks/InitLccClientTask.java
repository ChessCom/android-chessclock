package com.chess.backend.tasks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.LiveChessClient;
import com.chess.live.client.LiveChessClientException;
import com.chess.live.client.LiveChessClientFacade;
import com.chess.live.client.impl.HttpClientProvider;
import org.eclipse.jetty.client.HttpClient;

import java.io.IOException;
import java.io.InputStream;

public class InitLccClientTask extends AbstractUpdateTask<LiveChessClient, Void> {
    private static final String TAG = "InitLccClientTask";

    private Context context;

    public InitLccClientTask(TaskUpdateInterface<LiveChessClient> taskFace) {
        super(taskFace);
        context = taskFace.getMeContext();
    }

    @Override
    protected Integer doTheTask(Void... loadItems) {

        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;

            InputStream keyStoreInputStream = context.getAssets().open(LccHolder.KEY_FILE_NAME);
            Log.d(TAG, "Start Chess.Com LCC ");
            Log.d(TAG, "Connecting to: " + LccHolder.CONFIG_BAYEUX_HOST + ":" + LccHolder.CONFIG_PORT);


            item = LiveChessClientFacade.createClient(LccHolder.AUTH_URL, LccHolder.CONFIG_BAYEUX_HOST, LccHolder.CONFIG_PORT, LccHolder.CONFIG_URI);
            item.setClientInfo("Android", versionName, "No-Key");
            item.setSupportedClientFeatures(false, false);
            HttpClient httpClient = HttpClientProvider.getHttpClient(HttpClientProvider.DEFAULT_CONFIGURATION, false);
            httpClient.setConnectorType(HttpClient.CONNECTOR_SOCKET);
            httpClient.setMaxConnectionsPerAddress(4);
            httpClient.setSoTimeout(7000);
            httpClient.setConnectTimeout(10000);
            httpClient.setTimeout(7000); //

            httpClient.setKeyStoreType(LccHolder.PKCS_12);
            httpClient.setTrustStoreType(LccHolder.PKCS_12);
            httpClient.setKeyManagerPassword(LccHolder.TESTTEST);
            httpClient.setKeyStoreInputStream(keyStoreInputStream);
            httpClient.setKeyStorePassword(LccHolder.TESTTEST);
            httpClient.setTrustStoreInputStream(keyStoreInputStream);
            httpClient.setTrustStorePassword(LccHolder.TESTTEST);

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

        return StaticData.RESULT_OK;
    }


}
