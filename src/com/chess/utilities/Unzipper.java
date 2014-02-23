package com.chess.utilities;

import android.util.Log;
import com.chess.backend.interfaces.FileReadyListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.09.13
 * Time: 20:36
 */
public class UnZipper {
	public static final String TAG = "UnZipper";

	public static List<String> unzip(String zipFile, String filePath, FileReadyListener progressFace) {
		List<String> filesList = new ArrayList<String>();
		try {
			FileInputStream inputStream = new FileInputStream(zipFile);
			ZipInputStream zipInputStream = new ZipInputStream(inputStream);
			ZipEntry zipEntry;

			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				Log.v(TAG, "Unzipping " + zipEntry.getName());
				filesList.add(zipEntry.getName());

				if (zipEntry.isDirectory()) {
					dirChecker(filePath, zipEntry.getName());
				} else {
					FileOutputStream outputStream = new FileOutputStream(filePath + zipEntry.getName());
					long totalSize = zipEntry.getSize();
					int totalRead = 0;
					progressFace.changeTitle(zipEntry.getName());
					progressFace.setProgress(0);
					final int buffer_size = 1024;

					byte[] buffer = new byte[buffer_size];
					for (; ; ) {
						int count = zipInputStream.read(buffer, 0, buffer_size);
						totalRead += count;
						int progress = (int) ((totalRead / (float) totalSize) * 100);

						progressFace.setProgress(progress);
						if (count == -1) {
							break;
						}
						outputStream.write(buffer, 0, count);
					}

					zipInputStream.closeEntry();
					outputStream.close();
				}

			}
			zipInputStream.close();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return filesList;
	}

	private static void dirChecker(String filePath, String dir) {
		File file = new File(filePath + dir);

		if (!file.isDirectory()) {
			boolean dirCreated = file.mkdirs();
			Log.d(TAG, "dir created = " + dirCreated);
		}
	}
}