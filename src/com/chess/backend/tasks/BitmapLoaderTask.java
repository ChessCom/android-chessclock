package com.chess.backend.tasks;

import android.content.res.Resources;
import android.graphics.Bitmap;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;

/**
 * BitmapLoaderTask class
 *
 * @author alien_roger
 * @created at: 20.03.12 5:16
 */
public class BitmapLoaderTask extends AbstractUpdateTask<Bitmap, String> {
	private Resources resources;

	public BitmapLoaderTask(TaskUpdateInterface<Bitmap, String> taskFace) {
		super(taskFace);
		resources = taskFace.getMeContext().getResources();
	}

	@Override
	protected Integer doTheTask(String... params) {

//		item = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0], "drawable", "com.chess"));
		item = taskFace.backgroundMethod(params[0]);

		return StaticData.RESULT_OK;
	}
}
