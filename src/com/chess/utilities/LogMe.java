package com.chess.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.10.13
 * Time: 17:48
 */
public class LogMe {
	                                                                            // 10-23 18:36:16.963
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd' 'HH:mm:ss.SSS");

	public static void d(String tag, String text) {
		File logFile = new File("sdcard/log.txt");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			String timestamp = dateFormatter.format(new Date(System.currentTimeMillis()));

			buf.append(timestamp).append(" ").append(tag).append(" ").append(text);
			buf.newLine();
			buf.flush();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
