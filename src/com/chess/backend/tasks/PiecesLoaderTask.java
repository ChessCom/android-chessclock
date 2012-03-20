package com.chess.backend.tasks;

import android.content.res.Resources;
import android.graphics.Bitmap;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.utilities.BitmapLoader;

/**
 * BitmapLoaderTask class
 *
 * @author alien_roger
 * @created at: 20.03.12 5:16
 */
public class PiecesLoaderTask extends AbstractUpdateTask<Bitmap[][],String>{
	private Resources resources;

	public PiecesLoaderTask(TaskUpdateInterface<Bitmap[][], String> taskFace) {
		super(taskFace);
		resources = taskFace.getMeContext().getResources();
	}

	@Override
	protected Integer doTheTask(String... params) {
		item = new Bitmap[2][6];
		item[0][0] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_wp", "drawable", "com.chess"));
		item[0][1] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_wn", "drawable", "com.chess"));
		item[0][2] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_wb", "drawable", "com.chess"));
		item[0][3] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_wr", "drawable", "com.chess"));
		item[0][4] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_wq", "drawable", "com.chess"));
		item[0][5] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_wk", "drawable", "com.chess"));
		item[1][0] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_bp", "drawable", "com.chess"));
		item[1][1] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_bn", "drawable", "com.chess"));
		item[1][2] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_bb", "drawable", "com.chess"));
		item[1][3] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_br", "drawable", "com.chess"));
		item[1][4] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_bq", "drawable", "com.chess"));
		item[1][5] = BitmapLoader.loadFromResource(resources, resources.getIdentifier(params[0] + "_bk", "drawable", "com.chess"));

		return StaticData.RESULT_OK;
	}
}
