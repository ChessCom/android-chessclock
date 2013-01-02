/*
 * Copyright 2011 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package actionbarcompat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.ui.views.BadgeDrawable;

/**
 * An extension of {@link ActionBarHelper} that provides Android 3.0-specific
 * functionality for Honeycomb tablets. It thus requires API level 11.
 */
public class ActionBarHelperHoneycomb extends ActionBarHelper {
	private Menu mOptionsMenu;
	private View mRefreshIndeterminateProgressView = null;
	private boolean showActionRefresh;

	protected ActionBarHelperHoneycomb(ActionBarActivity activity) {
		super(activity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mOptionsMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void setRefreshActionItemState(boolean refreshing) {
		// On Honeycomb, we can set the state of the refresh button by giving it
// a custom
		// action view.
		if (mOptionsMenu == null) {
			return;
		}

		if(mActivity == null) {
			return;
		}

		final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
		if (refreshItem != null) {
			if (refreshing) {
				if (mRefreshIndeterminateProgressView == null) {
					Context context = getActionBarThemedContext();
					if (context != null) {
						LayoutInflater inflater = (LayoutInflater) context.getSystemService(
								Context.LAYOUT_INFLATER_SERVICE);
						mRefreshIndeterminateProgressView = inflater.inflate(R.layout.actionbar_indeterminate_progress,
								null);

					}
				}

				refreshItem.setActionView(mRefreshIndeterminateProgressView);
			} else {
				refreshItem.setActionView(null);
			}
			hideRefreshIcon(refreshItem, refreshing);
		}
	}

	/**
	 * Free space in actionBar for title. Don't change visibility if we have visible button.
	 * @param refreshItem contains menuItem with icon
	 * @param refreshing currently refreshing state
	 */
	private void hideRefreshIcon(MenuItem refreshItem, boolean refreshing){
		if (!showActionRefresh) {
			refreshItem.setVisible(refreshing);
		}
	}

	@Override
	public void showSearchPanel(boolean show) {
	}

	@Override
	public void showMenuItemById(int id, boolean show) {
		if(mActivity != null)
			mActivity.invalidateOptionsMenu();
	}

	@Override
	public void showMenuItemById(int itemId, boolean visible, Menu menu) {
		if(itemId == R.id.menu_refresh){
			showActionRefresh = visible;
			menu.findItem(itemId).setIcon(visible? R.drawable.ic_action_refresh
					:R.drawable.empty);
		}else {
			menu.findItem(itemId).setVisible(visible);
			menu.findItem(itemId).setEnabled(visible);
		}
	}

	@Override
	public void setBadgeValueForId(int menuId, int value) {
		if(mActivity != null)
			mActivity.invalidateOptionsMenu();
	}

	@Override
	public void setBadgeValueForId(int menuId, int value, Menu menu) {
		MenuItem item = menu.findItem(menuId);
		Drawable icon = item.getIcon();
		if (icon instanceof BadgeDrawable) {
			((BadgeDrawable)icon).setValue(value);
		} else if (value != 0) {
			item.setIcon(new BadgeDrawable(getActionBarThemedContext(), icon, value));
		}
	}


	/**
	 * Returns a {@link android.content.Context} suitable for inflating layouts
	 * for the action bar. The implementation for this method in
	 * {@link ActionBarHelperICS} asks the action bar for a themed coreContext.
	 */
	protected Context getActionBarThemedContext() {
		return mActivity;
	}
}
