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

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * An abstract class that handles some common action bar-related functionality
 * in the app. This class provides functionality useful for both phones and
 * tablets, and does not require any Android 3.0-specific features, although it
 * uses them if available.
 * <p/>
 * Two implementations of this class are {@link ActionBarHelperBase} for a
 * pre-Honeycomb version of the action bar, and {@link ActionBarHelperHoneycomb}
 * , which uses the built-in ActionBar features in Android 3.0 and later.
 */
public abstract class ActionBarHelper {
	protected ActionBarActivity mActivity;

	/**
	 * Factory method for creating {@link ActionBarHelper} objects for a given
	 * activity. Depending on which device the app is running, either a basic
	 * helper or Honeycomb-specific helper will be returned.
	 * @param activity
	 * @return
	 */
	public static ActionBarHelper createInstance(ActionBarActivity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new ActionBarHelperICS(activity);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new ActionBarHelperHoneycomb(activity);
		} else {
			return new ActionBarHelperBase(activity);
		}
	}

	protected ActionBarHelper(ActionBarActivity activity) {
		mActivity = activity;
	}

	/**
	 * Action bar helper code to be run in
	 * {@link android.app.Activity#onCreate(android.os.Bundle)}.
	 */
	public void onCreate(Bundle savedInstanceState) {
	}

	/**
	 * Action bar helper code to be run in
	 * {@link android.app.Activity#onPostCreate(android.os.Bundle)}.
	 */
	public void onPostCreate(Bundle savedInstanceState) {
	}

	/**
	 * Action bar helper code to be run in
	 * {@link android.app.Activity#onCreateOptionsMenu(android.view.Menu)}.
	 * <p/>
	 * NOTE: Setting the visibility of menu items in <em>menu</em> is not
	 * currently supported.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	/**
	 * Action bar helper code to be run in
	 * {@link android.app.Activity#onTitleChanged(CharSequence, int)}.
	 */
	protected void onTitleChanged(CharSequence title, int color) {
	}

	/**
	 * Sets the indeterminate loading state of the item with ID
	 * {@link R.id.menu_refresh}. (where the item ID was menu_refresh).
	 */
	public abstract void setRefreshActionItemState(boolean refreshing);

	/**
	 * Show searchPanel instead of view collapsing the item with ID
	 * {@link R.id.menu_search}. (where the item ID was menu_search).
	 * @param show
	 */
	public abstract void showSearchPanel(boolean show);

	/**
	 * Show/Hide actionbar item at specified id
	 * @param id of item to show/hide
	 * @param show
	 */
	public abstract void showMenuItemById(int id, boolean show);

	/**
	 * Show/Hide actionbar item at specified id. Used for HomeyComb+ API
	 * @param itemId
	 * @param show
	 * @param menu
	 */
	public abstract void showMenuItemById(int itemId, boolean show, Menu menu);

	/**
	 * Set badge with specified value for itemId element in actionBar
	 * @param menuId contains id of menu Item
	 * @param value value to be changed
	 */
	public abstract void setBadgeValueForId(int menuId, int value);

	/**
	 * Set badge with specified value for itemId element in actionBar. Used for HomeyComb+ API
	 * @param menuId contains id of menu Item
	 * @param value value to be changed
	 */
	public abstract void setBadgeValueForId(int menuId, int value, Menu menu);
	/**
	 * Returns a {@link android.view.MenuInflater} for use when inflating menus.
	 * The implementation of this method in {@link ActionBarHelperBase} returns
	 * a wrapped menu inflater that can read action bar metadata from a menu
	 * resource pre-Honeycomb.
	 */
	public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
		return superMenuInflater;
	}
}
