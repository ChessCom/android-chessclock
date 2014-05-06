package com.chess.clock.util;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Utilities for handling multiple selection in list views. Contains functionality similar to
 * {@link AbsListView#CHOICE_MODE_MULTIPLE_MODAL} but that works with {@link ActionBarActivity} and
 * backward-compatible action bars.
 */
public class MultiSelectionUtil {

	public static Controller attachMultiSelectionController(final ListView listView,
															final ActionBarActivity activity,
															final MultiChoiceModeListener listener) {
		return Controller.attach(listView, activity, listener);
	}

	/**
	 * @see android.widget.AbsListView.MultiChoiceModeListener
	 */
	public static interface MultiChoiceModeListener extends ActionMode.Callback {
		/**
		 * @see android.widget.AbsListView.MultiChoiceModeListener#onItemCheckedStateChanged(
		 *android.view.ActionMode, int, long, boolean)
		 */
		public void onItemCheckedStateChanged(ActionMode mode,
											  int position, boolean checked);
	}

	public static class Controller implements ActionMode.Callback, AdapterView.OnItemClickListener {

		private ActionMode mActionMode;
		private ListView mListView = null;
		private ActionBarActivity mActivity = null;
		private MultiChoiceModeListener mListener = null;
		private ArrayList<Integer> mItemsToCheck;
		private AdapterView.OnItemClickListener mOldItemClickListener;

		private Controller() {

		}

		public static Controller attach(ListView listView, ActionBarActivity activity,
										MultiChoiceModeListener listener) {
			Controller controller = new Controller();
			controller.mListView = listView;
			controller.mActivity = activity;
			controller.mListener = listener;
			return controller;
		}

		private void readInstanceState(Bundle savedInstanceState) {
			if (savedInstanceState != null) {

				ArrayList<Integer> checkedPos = savedInstanceState.getIntegerArrayList(getStateKey());
				if (checkedPos != null && checkedPos.size() > 0) {
					mItemsToCheck = new ArrayList<Integer>();
					for (int pos : checkedPos) {
						mItemsToCheck.add(pos);
					}
				}
			}
		}

		public void tryRestoreInstanceState(Bundle savedInstanceState) {
			if (mListView.getAdapter() == null) {
				return;
			}
			readInstanceState(savedInstanceState);
			mActionMode = mActivity.startSupportActionMode(Controller.this);
		}

		public void finish() {
			if (mActionMode != null) {
				mActionMode.finish();
			}
		}

		public boolean saveInstanceState(Bundle outBundle) {
			SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions();
			if (mActionMode != null && checkedPositions != null) {
				ArrayList<Integer> positions = new ArrayList<Integer>();
				for (int i = 0; i < checkedPositions.size(); i++) {
					if (checkedPositions.valueAt(i)) {
						int position = checkedPositions.keyAt(i);
						positions.add(position);
					}
				}

				outBundle.putIntegerArrayList(getStateKey(), positions);
				return true;
			}
			return false;
		}

		private String getStateKey() {
			return MultiSelectionUtil.class.getSimpleName() + "_" + mListView.getId();
		}

		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

			if (mListener.onCreateActionMode(actionMode, menu)) {
				mActionMode = actionMode;
				mOldItemClickListener = mListView.getOnItemClickListener();
				mListView.setOnItemClickListener(Controller.this);

				if (mItemsToCheck != null) {

					for (Integer pos : mItemsToCheck) {
						mListView.setItemChecked(pos, true);
						mListener.onItemCheckedStateChanged(mActionMode, pos, true);
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {

			if (mListener.onPrepareActionMode(actionMode, menu)) {
				mActionMode = actionMode;
				return true;
			}
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
			return mListener.onActionItemClicked(actionMode, menuItem);
		}

		@Override
		public void onDestroyActionMode(ActionMode actionMode) {
			mListener.onDestroyActionMode(actionMode);
			mListView.setOnItemClickListener(mOldItemClickListener);
			mActionMode = null;
		}

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			boolean checked = mListView.isItemChecked(position);
			mListener.onItemCheckedStateChanged(mActionMode, position, checked);
		}

		public boolean startActionMode() {

			if (mActionMode != null) {
				return false;
			}

			mItemsToCheck = new ArrayList<Integer>();
			mActionMode = mActivity.startSupportActionMode(Controller.this);
			return true;
		}
	}
}
