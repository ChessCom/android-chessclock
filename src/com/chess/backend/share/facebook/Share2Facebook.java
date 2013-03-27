package com.chess.backend.share.facebook;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.chess.backend.share.ShareFace;
import com.chess.backend.share.ShareObject;


public class Share2Facebook extends ShareObject {

	public Share2Facebook(Context context,int imageId, String name) {
		super(context, imageId, name);
	}

	@Override
	public void shareMe(ShareFace msg) {
		FacebookConnector facebookConnector = new FacebookConnector((FragmentActivity) context);

        Bundle params = new Bundle();
        params.putString("caption", msg.getCaption());
        params.putString("description", msg.getDescription());
        params.putString("picture", msg.getPicture());
        params.putString("link", msg.getLink());
        params.putString("name", msg.getName());

        facebookConnector.updateStatus(params);
	}

}
