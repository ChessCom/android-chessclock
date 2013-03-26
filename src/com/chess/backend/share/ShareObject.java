package com.chess.backend.share;

import android.content.Context;
import com.chess.backend.share.ShareFace;
import com.chess.backend.statics.StaticData;

import java.text.SimpleDateFormat;

public abstract class ShareObject {
	private int imageId;
	private String name;
	protected Context context;

    protected final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

    protected SimpleDateFormat inputTimeFormat = new SimpleDateFormat(StaticData.INPUT_DATE_FORMAT); // 31.01.2012 15:06:00


	protected ShareObject(Context context, int imageId, String name) {
		super();
		this.imageId = imageId;
		this.name = name;
		this.context = context;
	}

	public int getImageId() {
		return imageId;
	}

	public String getName() {
		return name;
	}

	public abstract void shareMe(ShareFace item);

}
