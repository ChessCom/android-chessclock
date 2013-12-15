package com.chess.backend.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.StaticData;
import com.chess.utilities.AppUtils;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.07.13
 * Time: 13:07
 */
public class SaveImageToSdTask extends AbstractUpdateTask<Bitmap, String> {

	private final Context context;

	public SaveImageToSdTask(TaskUpdateInterface<Bitmap> taskFace, Bitmap imageToSave) {
		super(taskFace);
		context = taskFace.getMeContext();
		item = imageToSave;
	}

	@Override
	protected Integer doTheTask(String... params) {
		String filename = params[0];
		result = StaticData.EMPTY_DATA;
		if (item == null) {
			return StaticData.INTERNAL_ERROR;
		}

		File imgFile;
		try {
			imgFile = AppUtils.openFileByName(context, filename);
		} catch (IOException e) {
			e.printStackTrace();
			result = StaticData.INTERNAL_ERROR;
			return result;
		}

		result  = StaticData.RESULT_OK;
		// save stream to SD
		try {
			OutputStream os = new FileOutputStream(imgFile);
			item.compress(Bitmap.CompressFormat.PNG, 0, os);
			os.flush();
			os.close();

			// release bitmap
			item.recycle();
			item = null;

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
