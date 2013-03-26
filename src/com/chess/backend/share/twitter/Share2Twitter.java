package com.chess.backend.share.twitter;

import android.app.Activity;
import android.content.Context;
import com.chess.backend.share.ShareObject;
import com.chess.backend.share.ShareFace;

public class Share2Twitter extends ShareObject {

//    [CourseTitle] [CourseStartDate] [Location.locationname] [Location.locationplacename]
//            [permalink]

//    [PageTitle] [PublishDate]
//            [Ingress]
//
//            [permalink]

	public Share2Twitter(Context context,int imageId, String name) {
		super(context, imageId, name);
	}

	@Override
	public void shareMe(ShareFace msg) {
		TwitterAgent twitterAgent = new TwitterAgent((Activity) context);

		twitterAgent.sendTweet(msg.composeTwitterMessage());
	}

}
