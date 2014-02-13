package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.08.13
 * Time: 16:55
 */
public class PostCommentItem extends BaseResponseItem<PostCommentItem.Data> {
/*
 "status": "success",
    "data": {
        "comment_id": 2872
    }
*/

	public class Data {
		private long comment_id;

		public long getCommentId() {
			return comment_id;
		}
	}

}
