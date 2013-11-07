package com.chess.ui.fragments.welcome;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import com.chess.widgets.MultiDirectionSlidingDrawer;
import com.chess.R;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.interfaces.FragmentTabsFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.game_ui.GameCompFace;
import com.chess.ui.views.drawables.IconDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.11.13
 * Time: 10:02
 */
public class GameWelcomeCompFragmentTablet extends GameWelcomeCompFragment implements GameCompFace,
		PopupListSelectionFace, AdapterView.OnItemClickListener, MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {

	public GameWelcomeCompFragmentTablet() {
		CompGameConfig config = new CompGameConfig.Builder().build();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		setArguments(bundle);
	}

	public static GameWelcomeCompFragmentTablet createInstance(FragmentTabsFace parentFace, CompGameConfig config) {
		GameWelcomeCompFragmentTablet fragment = new GameWelcomeCompFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putParcelable(CONFIG, config);
		fragment.setArguments(bundle);
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_welcome_comp_frame, container, false);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.whatIsChessComTxt) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.WELCOME_FRAGMENT);
		} else if (view.getId() == R.id.loginBtn) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.SIGN_IN_FRAGMENT);
		} else if (view.getId() == R.id.signUpBtn) {
			parentFace.changeInternalFragment(WelcomeTabsFragment.SIGN_UP_FRAGMENT);
		}
	}

	@Override
	protected void widgetsInit(View view) {
		super.widgetsInit(view);

		TextView whatIsChessComTxt = (TextView) view.findViewById(R.id.whatIsChessComTxt);
		Drawable icon = new IconDrawable(getActivity(), R.string.ic_round_right,  R.color.semitransparent_white_75,
				R.dimen.glyph_icon_big);

		whatIsChessComTxt.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.glyph_icon_padding));
		whatIsChessComTxt.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
		whatIsChessComTxt.setOnClickListener(this);

		view.findViewById(R.id.loginBtn).setOnClickListener(this);
		view.findViewById(R.id.signUpBtn).setOnClickListener(this);
	}

	@Override
	public void newGame() {
		getActivityFace().changeRightFragment(WelcomeCompGameOptionsFragment.createInstance(parentFace));
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				getActivityFace().toggleRightMenu();
			}
		}, 100);
	}

}
