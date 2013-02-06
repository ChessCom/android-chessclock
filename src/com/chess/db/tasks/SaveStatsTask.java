package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.ArticleItem;
import com.chess.backend.entity.new_api.StatsItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBConstants;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.02.13
 * Time: 19:32
 */
public class SaveStatsTask extends AbstractUpdateTask<StatsItem.Data, Long> {

	private List<StatsItem.Data> currentItems;
	private ContentResolver resolver;
	protected static String[] arguments = new String[2];

	public SaveStatsTask(TaskUpdateInterface<StatsItem.Data> taskFace, List<StatsItem.Data> currentItems,
						 ContentResolver resolver) {
		super(taskFace);
		this.currentItems = currentItems;
		this.resolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... params) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);
		for (StatsItem.Data currentItem : currentItems) {
			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(userName);
//			arguments2[1] = String.valueOf(currentItem.getGameId());
//
//			Uri uri = DBConstants.STA
//
//			Cursor cursor = resolver.query()

		}


		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
