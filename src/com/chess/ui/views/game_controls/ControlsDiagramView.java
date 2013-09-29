package com.chess.ui.views.game_controls;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.ui.interfaces.boards.BoardViewDiagramFace;

import static com.chess.ui.views.game_controls.ControlsBaseView.ButtonIds.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 16:44
 */
public class ControlsDiagramView extends ControlsBaseView {

	private BoardViewDiagramFace boardViewFace;

	public ControlsDiagramView(Context context) {
		super(context);
	}

	public ControlsDiagramView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	void init() {
		super.init();
		Resources resources = getResources();
		if (resources == null) {
			return;
		}

		controlButtonHeight = (int) resources.getDimension(R.dimen.game_controls_button_diagram_height);
		controlIconColor = resources.getColorStateList(R.color.text_controls_icons_diagram);
		controlIconSize = (int) (resources.getDimension(R.dimen.game_controls_icon_diagram_size) / density);

		addControlButton(MAKE_MOVE, R.style.Rect_Bottom_Left_LightGrey);
		addControlButton(BACK_END, R.style.Rect_Bottom_Middle_LightGrey);
		addControlButton(BACK, R.style.Rect_Bottom_Middle_LightGrey);
		addControlButton(FORWARD, R.style.Rect_Bottom_Middle_LightGrey);
		addControlButton(FWD_END, R.style.Rect_Bottom_Middle_LightGrey);
		addControlButton(DOTS_OPTIONS, R.style.Rect_Bottom_Right_LightGrey);

		addView(controlsLayout);
	}

	@Override
	RoboButton getDefaultButton() {
		RoboButton button = new RoboButton(getContext());
		button.setFont(FontsHelper.ICON_FONT);
		button.setTextSize(controlIconSize);
		button.setTextColor(controlIconColor);
		button.setOnClickListener(this);

		return button;
	}

	@Override
	public void enableForwardBtn(boolean enable) {
//		enableGameButton(FORWARD, enable);
	}

	@Override
	public void enableBackBtn(boolean enable) {
//		enableGameButton(BACK, enable);
	}

	@Override
	public void onClick(View view) {  // TODO rework click handles
		if (blocked)
			return;

		if (view.getId() == getButtonId(MAKE_MOVE)) {
			boardViewFace.onPlay();
		} else if (view.getId() == getButtonId(BACK_END)) {
			boardViewFace.onRewindBack();
			// TODO add search ability
		} else if (view.getId() == getButtonId(BACK)) {
			boardViewFace.onMoveBack();
		} else if (view.getId() == getButtonId(FORWARD)) {
			boardViewFace.onMoveForward();
		} else if (view.getId() == getButtonId(FWD_END)) {
			boardViewFace.onRewindForward();
		} else if (view.getId() == getButtonId(DOTS_OPTIONS)) {
			boardViewFace.showOptions();
		}
	}

	public void setBoardViewFace(BoardViewDiagramFace boardViewFace) {
		this.boardViewFace = boardViewFace;
	}

	public void enableGameControls(boolean enable) {
		enableGameButton(MAKE_MOVE, enable);
		enableGameButton(BACK_END, enable);
		enableGameButton(BACK, enable);
		enableGameButton(FORWARD, enable);
		enableGameButton(FWD_END, enable);
		enableGameButton(DOTS_OPTIONS, enable);
	}
}
