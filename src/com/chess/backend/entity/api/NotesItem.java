package com.chess.backend.entity.api;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 21.01.14
 * Time: 7:04
 */
public class NotesItem extends BaseResponseItem<NotesItem.Data> {

/*
"status": "success",
    "data": {
        "notes": "I just edited my notes..."
    }
*/

	public class Data {

		private String notes;
		private long note_id;

		public String getNotes() {
			return notes;
		}

		public void setNotes(String notes) {
			this.notes = notes;
		}

		public long getNote_id() {
			return note_id;
		}

		public void setNote_id(long note_id) {
			this.note_id = note_id;
		}
	}
}
