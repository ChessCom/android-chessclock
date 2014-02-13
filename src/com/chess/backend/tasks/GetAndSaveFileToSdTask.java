package com.chess.backend.tasks;

import android.net.Uri;
import android.util.Log;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;
import com.chess.utilities.UnZipper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.09.13
 * Time: 19:43
 */
public class GetAndSaveFileToSdTask extends AbstractUpdateTask<String, String> {

	private static final String TAG = "GetAndSaveFileToSdTask";
	private FileReadyListener progressFace;
	private boolean doUnzip;
	private File filePath;

	/**
	 * AsyncTask that receive Url as {@code String} in executeTask argument
	 * and return {@code String} object as path, where file was saved
	 *
	 * @param taskFace to get resources and progressUpdate listener
	 * @param filePath for folder to save
	 */
	public GetAndSaveFileToSdTask(TaskUpdateInterface<String> taskFace, File filePath) {
		super(taskFace, new ArrayList<String>());
		this.filePath = filePath;
		init(taskFace);
	}

	/**
	 * @param taskFace to get resources and progressUpdate listener
	 * @param doUnzip  if we need to unzip received file
	 * @param filePath for folder to save
	 */
	public GetAndSaveFileToSdTask(TaskUpdateInterface<String> taskFace, boolean doUnzip, File filePath) {
		super(taskFace, new ArrayList<String>());
		this.doUnzip = doUnzip;
		this.filePath = filePath;
		init(taskFace);
	}

	private void init(TaskUpdateInterface<String> taskFace) {
		progressFace = (FileReadyListener) taskFace;
	}

	@Override
	protected Integer doTheTask(String... params) {
		for (String param : params) {
			String url = param;
			result = StaticData.EMPTY_DATA;

			Log.d(TAG, "Loading file by url = " + url);
			if (progressFace != null) {
				progressFace.setProgress(-1);
			}
			String filename = Uri.parse(url).getLastPathSegment();

			url = url.replace(" ", "%20");
			if (!url.startsWith(EnhancedImageDownloader.HTTP)) {
				url = EnhancedImageDownloader.HTTP_PREFIX + url;
			}
			try {
				// Start loading
				URLConnection urlConnection = new URL(url).openConnection();
				urlConnection.setConnectTimeout(RestHelper.TIME_OUT);
				urlConnection.setReadTimeout(RestHelper.TIME_OUT);

				int totalSize = urlConnection.getContentLength();
				InputStream is = urlConnection.getInputStream();

				// create descriptor
				File savedFile = new File(filePath, filename);
				// copy stream to imgFile
				OutputStream os = new FileOutputStream(savedFile); // save stream to

				// save img to SD and update progress
				final int buffer_size = 1024;
				int totalRead = 0;
				try {
					byte[] bytes = new byte[buffer_size];
					for (; ; ) {
						int count = is.read(bytes, 0, buffer_size);
						totalRead += count;
						int progress = (int) ((totalRead / (float) totalSize) * 100);

						if (progressFace != null) {
							progressFace.setProgress(progress);
						}
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

				String savedFileName = Symbol.EMPTY;
				if (doUnzip) {
					String zipFilePath = savedFile.getAbsolutePath();
					String unzipPath = filePath.getAbsolutePath() + AppUtils.UNZIPPED;

					UnZipper.unzip(zipFilePath, unzipPath, progressFace);
					if (filename != null) {
						savedFileName = "/" + filename.replace(".zip", "");
					}
				} else {

					if (filename != null) {
						savedFileName = "/" + filename;
					}
				}

				if (!useList) {
					item = savedFileName;
					return StaticData.RESULT_OK;
				}

				itemList.add(savedFileName);
				result = StaticData.RESULT_OK;

			} catch (MalformedURLException e) {
				e.printStackTrace();
				return StaticData.INTERNAL_ERROR;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return StaticData.VALUE_NOT_EXIST;
			} catch (IOException e) {
				e.printStackTrace();
				return StaticData.NO_NETWORK;
			}
		}

		return result;
	}
}