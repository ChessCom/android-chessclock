package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DbScheme;
import com.chess.ui.interfaces.ItemClickListenerFace;

// TODO: adjust

public class ExplorerMovesCursorAdapter extends ItemsCursorAdapter {

	private final ItemClickListenerFace clickListenerFace;

	public ExplorerMovesCursorAdapter(ItemClickListenerFace clickListenerFace, Cursor cursor) {
		super(clickListenerFace.getMeContext(), cursor);
		this.clickListenerFace = clickListenerFace;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.explorer_moves_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.explorerMoveTxt = (TextView) view.findViewById(R.id.explorerMoveTxt);

		View friendListItemView = view.findViewById(R.id.explorerMovesListItemView);
		friendListItemView.setOnClickListener(clickListenerFace);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		view.setTag(R.id.list_item_id, cursor.getPosition());

		// just test content
		String explorerMove = getString(cursor, DbScheme.V_FEN) + getString(cursor, DbScheme.V_NUM_GAMES) +
				getString(cursor, DbScheme.V_WHITE_WON_PERCENT) + getString(cursor, DbScheme.V_DRAW_PERCENT) +
				getString(cursor, DbScheme.V_BLACK_WON_PERCENT);

		holder.explorerMoveTxt.setText(explorerMove);
	}

	private class ViewHolder {
		public TextView explorerMoveTxt;
	}
}
