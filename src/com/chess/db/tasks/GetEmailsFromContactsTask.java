package com.chess.db.tasks;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;
import com.chess.backend.entity.ContactItem;
import com.chess.backend.interfaces.MultiTypeGetFace;
import com.chess.backend.statics.StaticData;
import com.chess.db.QueryParams;

import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Phone;


public class GetEmailsFromContactsTask extends QueryForCursorTask {

	int emailType = Email.TYPE_WORK;
	private static final String TAG = "GetEmailsTask";
	private List<ContactItem> contacts;
	private MultiTypeGetFace<ContactItem, Cursor> contactsUpdateFace;

	public GetEmailsFromContactsTask(MultiTypeGetFace<ContactItem, Cursor> taskFace, QueryParams params,
									 List<ContactItem> contacts) {
		super(taskFace, params);
		this.contacts = contacts;
		contactsUpdateFace = taskFace;
	}

	@Override
	protected int doAdditionToCursor(Cursor cursor) {
		while (cursor.moveToNext()) {
			String id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
			String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			contentResolver.query(Phone.CONTENT_URI,
					null,
					Phone.CONTACT_ID + " = ?", new String[]{id}, null);

			Cursor emailsCursor = contentResolver.query(Email.CONTENT_URI, null,
					Email.CONTACT_ID + " = " + id, null, null);

			while (emailsCursor != null && emailsCursor.moveToNext()) {
				String contactEmail = emailsCursor.getString(emailsCursor.getColumnIndex(Email.DATA));

				emailType = emailsCursor.getInt(emailsCursor.getColumnIndex(Phone.TYPE));
				Log.d(TAG, "Contact Name = " + contactName
						+ " contact mail = " + contactEmail
						+ " email type = " + emailType
				);
				ContactItem contactItem = new ContactItem();
				contactItem.setEmail(contactEmail);
				contactItem.setName(contactName);

				contacts.add(contactItem);
			}

		}
		if (contacts.size() > 0) {
			result = StaticData.RESULT_OK;
		} else {
			result = StaticData.VALUE_NOT_EXIST;
		}


		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		getTaskFace().showProgress(false);
		if(isCancelled())
			return;

		if (result == StaticData.RESULT_OK) {
			contactsUpdateFace.updateContacts(contacts);
		}else {
			getTaskFace().errorHandle(result);
		}
	}
}
