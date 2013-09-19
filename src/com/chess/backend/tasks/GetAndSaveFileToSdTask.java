package com.chess.backend.tasks;

import android.content.Context;
import android.net.Uri;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.utilities.AppUtils;
import com.chess.utilities.UnZipper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.09.13
 * Time: 19:43
 */
public class GetAndSaveFileToSdTask extends AbstractUpdateTask<String, String> {

	private final Context context;
	private final FileReadyListener progressFace;

	public GetAndSaveFileToSdTask(TaskUpdateInterface<String> taskFace) {
		super(taskFace);
		context = taskFace.getMeContext();
		progressFace = (FileReadyListener) taskFace;
	}

	@Override
	protected Integer doTheTask(String... params) {
		String url = params[0];
		result = StaticData.EMPTY_DATA;

		String filename = Uri.parse(url).getLastPathSegment();

		url = url.replace(" ", "%20");
		if (!url.startsWith(EnhancedImageDownloader.HTTP)) {
			url = EnhancedImageDownloader.HTTP_PREFIX + url;
		}
		try {
			// Start loading
			URLConnection urlConnection = new URL(url).openConnection();
			int totalSize = urlConnection.getContentLength();

			InputStream is = urlConnection.getInputStream();

			File soundsPath = AppUtils.getLocalDirForSounds(context);

			// create descriptor
			File soundPackFile = new File(soundsPath, filename);
			// copy stream to imgFile
			OutputStream os = new FileOutputStream(soundPackFile); // save stream to

			// save img to SD and update progress
			final int buffer_size = 1024;
			int totalRead = 0;
			try {
				byte[] bytes = new byte[buffer_size];
				for (; ; ) {
					int count = is.read(bytes, 0, buffer_size);
					totalRead += count;
					int progress = (int) ((totalRead / (float) totalSize) * 100);

					progressFace.setProgress(progress);
					if (count == -1) {
						break;
					}
					os.write(bytes, 0, count);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}

			os.close();

			String zipFilePath = soundPackFile.getAbsolutePath();
			String unzipPath = soundsPath.getAbsolutePath() + AppUtils.UNZIPPED;


			UnZipper.unzip(zipFilePath, unzipPath, progressFace);
			if (filename != null) {
				item = "/" + filename.replace(".zip", "");
			}

			result = StaticData.RESULT_OK;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
}