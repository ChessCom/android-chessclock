/*
 * Copyright 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package actionbarcompat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import com.chess.ui.activities.CommonLogicActivity;

/**
 * A base activity that defers common functionality across app activities to an {@link
 * ActionBarHelper}.
 * <p/>
 * NOTE: dynamically marking menu items as invisible/visible is not currently supported.
 * <p/>
 * NOTE: this may used with the Android Compatibility Package by extending
 * android.support.v4.app.FragmentActivity instead of {@link android.app.Activity}.
 */
public abstract class ActionBarActivity extends CommonLogicActivity {
	final ActionBarHelper mActionBarHelper = ActionBarHelper.createInstance(this);

	/**
	 * Returns the {@link ActionBarHelper} for this activity.
	 */
	protected ActionBarHelper getActionBarHelper() {
		return mActionBarHelper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MenuInflater getMenuInflater() {
		return mActionBarHelper.getMenuInflater(super.getMenuInflater());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mActionBarHelper.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mActionBarHelper.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (HONEYCOMB_PLUS_API) {
			invalidateOptionsMenu();
		}
	}

	/**
	 * Base action bar-aware implementation for
	 * {@link android.app.Activity#onCreateOptionsMenu(android.view.Menu)}.
	 * <p/>
	 * Note: marking menu items as invisible/visible is not currently supported.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean retValue = false;
		retValue |= mActionBarHelper.onCreateOptionsMenu(menu);
		retValue |= super.onCreateOptionsMenu(menu);
		return retValue;
	}

	/**
	 * This method invoked from CustomSearchView textChanged or SearchButton hit
	 *
	 * @param query that was filled in search field
	 */
	protected void onSearchAutoCompleteQuery(String query) {

	}

	/**
	 * This method invoked from CustomSearchView on Enter/Search Button hit
	 *
	 * @param query that was filled in search field
	 */
	protected void onSearchQuery(String query) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		mActionBarHelper.onTitleChanged(title, color);
		super.onTitleChanged(title, color);
	}
}
