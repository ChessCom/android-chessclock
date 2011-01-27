package com.chess.lcc.android;

import com.chess.live.client.*;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: Vova Date: 25.02.2010 Time: 16:25:44 To change this template use File | Settings |
 * File Templates.
 */
public class ChesscomLiveChessClient
{
  private static ChesscomLiveChessClient INSTANCE;

  public static final Logger LOG = Logger.getLogger(ChesscomLiveChessClient.class);
  /*static
  {
    try
    {
      // hack: change HttpClient.setKeyStoreLocation to use InputStream rather than string path
      InputStream myInput = Config.class.getClassLoader().getResourceAsStream("assets/chesscom.pkcs12");
      OutputStream myOutput = new FileOutputStream("/data/data/com.chess/chesscom.pkcs12");
      // transfer bytes from the inputfile to the outputfile
      byte[] buffer = new byte[65535];
      int length;
      while ((length = myInput.read(buffer)) > 0) {
              myOutput.write(buffer, 0, length);
      }
      // Close the streams
      myOutput.flush();
      myOutput.close();
      myInput.close();
    }
    catch (Exception e)
    {
    //
    }
  }*/


  private LccHolder _lccHolder;
  private LiveChessClient _lccClient;
  //private static boolean _exit;

  private ChesscomLiveChessClient() {
    try
    {
      /*for (Provider provider : java.security.Security.getProviders())
      {
        LOG.info("Security provider: " + provider.getName() + " " + provider.getVersion() + " " + provider.getInfo());
        for (Enumeration e = provider.keys(); e.hasMoreElements();)
        {
          LOG.info(" Key " + e.nextElement());
        }
      }*/

      // connecting:
      /*if(CONFIG_AUTH_KEY != null)
      {
        LOG.info("A user is being logged-in: authKey=" + CONFIG_AUTH_KEY);
        _lccHolder = new LccHolder(CONFIG_AUTH_KEY, _lccClient);
        _lccClient.connect(CONFIG_AUTH_KEY, _lccHolder.getConnectionListener());
      }*/
      //Utils.sleep(5000);
      // chat:
      /*if(_lccHolder.isConnected())
      {
        final String message = CONFIG_CHAT_MESSAGE;
        _lccHolder.getClient().sendChatMessage(_lccHolder.getMainChat(), message);
      }*/
      // game:
      //_lccHolder.createGame();
      // exit:
      /*LOG.info("Enter 'exit', 'quit' or just 'q' (without the single quotes) to exit the application...");

      final LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
      while (!_exit) {
          final String cmd = in.readLine().trim().toLowerCase();
          if (cmd != null && (cmd.equals("exit") || cmd.equals("quit") || cmd.equals("q")))
              _exit = true;
          Utils.sleep(0.1F);
      }*/
      /*_lccHolder.getClient().disconnect();
      Utils.sleep(2F);*/
    }
    catch(Exception x)
    {
      x.printStackTrace();
    }
  }

  public static ChesscomLiveChessClient getInstance()
  {
    if (INSTANCE == null)
    {
      INSTANCE = new ChesscomLiveChessClient();
    }
    return INSTANCE;
  }

  public LiveChessClient getLccClient()
  {
    return _lccClient;
  }

  public LccHolder getUserHolder()
  {
    return _lccHolder;
  }

}
