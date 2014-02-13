package com.chess.backend.tasks;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.utilities.AppUtils;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.14
 * Time: 7:40
 */
public class SaveTextFileToSDTask extends AbstractUpdateTask<String, String> {

	private final Context context;
	private String customPath;

	public SaveTextFileToSDTask(TaskUpdateInterface<String> taskFace, String textToSave) {
		super(taskFace);
		context = taskFace.getMeContext();
		item = textToSave;
	}

	public SaveTextFileToSDTask(TaskUpdateInterface<String> taskFace, String textToSave, String customPath) {
		super(taskFace);
		this.customPath = customPath;
		context = taskFace.getMeContext();
		item = textToSave;
	}

	@Override
	protected Integer doTheTask(String... params) {
		String filename = params[0];
		result = StaticData.EMPTY_DATA;
		if (item == null) {
			return StaticData.INTERNAL_ERROR;
		}

		File fileToSave;
		try {
			if (TextUtils.isEmpty(customPath)) {
				fileToSave = AppUtils.openFileByName(context, filename);
			} else {
				File cacheDir;
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					cacheDir = new File(Environment.getExternalStorageDirectory(), customPath);
				} else {
					cacheDir = context.getCacheDir();
				}

				if (cacheDir != null && !cacheDir.exists()) {
					if (!cacheDir.mkdirs()) {
						throw new IOException("Can't use cacheDir");
					}
				}

				fileToSave = new File(cacheDir, filename);
			}
		} catch (IOException e) {
			e.printStackTrace();
			result = StaticData.INTERNAL_ERROR;
			return result;
		}

		result = StaticData.RESULT_OK;
		// save stream to SD
		try {
			OutputStream os = new FileOutputStream(fileToSave);
			os.write(item.getBytes());
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			result = StaticData.VALUE_NOT_EXIST;
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			result = StaticData.INTERNAL_ERROR;
		}
		return result;
	}
}
