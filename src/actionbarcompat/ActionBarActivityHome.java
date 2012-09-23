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
 * actionbarcompat.ActionBarHelper}.
 * <p/>
 * NOTE: dynamically marking menu items as invisible/visible is not currently supported.
 * <p/>
 * NOTE: this may used with the Android Compatibility Package by extending
 * android.support.v4.app.FragmentActivity instead of {@link android.app.Activity}.
 */
public abstract class ActionBarActivityHome extends CommonLogicActivity {
	final ActionBarHelperHome mActionBarHelperMy = ActionBarHelperHome.createInstance(this);

	/**
	 * Returns the {@link actionbarcompat.ActionBarHelper} for this activity.
	 */
	protected ActionBarHelperHome getActionBarHelper() {
		return mActionBarHelperMy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MenuInflater getMenuInflater() {
		return mActionBarHelperMy.getMenuInflater(super.getMenuInflater());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBarHelperMy.onCreate(savedInstanceState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mActionBarHelperMy.onPostCreate(savedInstanceState);
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
		retValue |= mActionBarHelperMy.onCreateOptionsMenu(menu);
		retValue |= super.onCreateOptionsMenu(menu);
		return retValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		mActionBarHelperMy.onTitleChanged(title, color);
		super.onTitleChanged(title, color);
	}
}
