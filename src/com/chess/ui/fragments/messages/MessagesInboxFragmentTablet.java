package com.chess.ui.fragments.messages;

import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.ui.interfaces.FragmentParentFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.11.13
 * Time: 18:16
 */
public class MessagesInboxFragmentTablet extends MessagesInboxFragment {

	private FragmentParentFace parentFace;

	public MessagesInboxFragmentTablet() {
	}

	public static MessagesInboxFragmentTablet createInstance(FragmentParentFace parentFace) {
		MessagesInboxFragmentTablet fragment = new MessagesInboxFragmentTablet();
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long conversationId = DbDataManager.getLong(cursor, DbScheme.V_ID);
		String otherUserName = DbDataManager.getString(cursor, DbScheme.V_OTHER_USER_USERNAME);

		parentFace.changeFragment(MessagesConversationFragment.createInstance(conversationId, otherUserName));
	}


}
