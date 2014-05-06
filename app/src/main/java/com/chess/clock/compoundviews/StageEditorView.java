package com.chess.clock.compoundviews;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.chess.clock.R;

public class StageEditorView extends TimePickerView {

	/**
	 * state
	 */
	private int mCurrentMoves = 0;
	private boolean mMovesVisible;

	/**
	 * UI components
	 */
	private EditText mMovesEditText;

	TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			String text = s.toString();
			if (text.length() != 0 && !text.equals("")) {
				mCurrentMoves = Integer.valueOf(s.toString());
			}
		}
	};

	/**
	 * Constructors
	 */
	public StageEditorView(Context context, Type type, boolean movesVisible) {
		this(context, type, null, movesVisible);
	}

	public StageEditorView(Context context, Type type, AttributeSet attrs, boolean movesVisible) {
		this(context, type, attrs, 0, movesVisible);
	}

	public StageEditorView(Context context, Type type, AttributeSet attrs, int defStyle, boolean movesVisible) {
		super(context, type, attrs, defStyle, R.layout.widget_stage_editor);

		mMovesVisible = movesVisible;

		// set moves visibility
		setupMovesEditText(mMovesVisible);
	}

	/**
	 * @return current moves number.
	 */
	public int getCurrentMoves() {
		return mCurrentMoves;
	}

	/**
	 * Set the current moves.
	 */
	public void setCurrentMoves(Integer currentHour) {
		if (mMovesVisible) {
			mCurrentMoves = currentHour;
			updateMovesDisplay();
		}
	}

	protected void setupMovesEditText(boolean visible) {

		mMovesEditText = (EditText) findViewById(R.id.stage_moves_edit_text);

		// Set focus marker at the end of the number digits
		mMovesEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mMovesEditText.setSelection(mMovesEditText.getText().length());
				}
			}
		});

		if (visible) {
			mMovesEditText.addTextChangedListener(mTextWatcher);
		} else {
			findViewById(R.id.stage_moves_container).setVisibility(View.GONE);
		}
	}

	public void updateMovesDisplay() {
		mMovesEditText.setText(String.valueOf(mCurrentMoves));
	}
}
