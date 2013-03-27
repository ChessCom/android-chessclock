package com.chess.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.share.twitter.TwitterAgent;
import com.chess.backend.statics.StaticData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.03.13
 * Time: 19:40
 */
public class TweetPreviewFragment extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener, TextWatcher, View.OnTouchListener, View.OnFocusChangeListener {

	private EditText postEdt;
	private TextView charsLeft;
	private String tweet;

	public static TweetPreviewFragment newInstance(String tweet) {
		TweetPreviewFragment fragment = new TweetPreviewFragment();
		fragment.tweet = tweet;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tweet_preview_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.okBtn).setOnClickListener(this);
		view.findViewById(R.id.cancelBtn).setOnClickListener(this);

		charsLeft = (TextView) view.findViewById(R.id.charsLeft);

		postEdt = (EditText) view.findViewById(R.id.postEdt);
		postEdt.setOnEditorActionListener(this);
		postEdt.addTextChangedListener(this);
		postEdt.setOnTouchListener(this);
		postEdt.setOnFocusChangeListener(this);
		postEdt.requestFocus();

		if (!tweet.equals(StaticData.SYMBOL_EMPTY)) {
			postEdt.setText(tweet);
			postEdt.setSelection(postEdt.getText().length());
		}

		checkField();
	}

	private boolean checkField() {
		if (postEdt.getText().toString().length() > 140) {
			postEdt.setError(getString(R.string.too_much_chars));
			charsLeft.setText("" + (140 - tweet.length()));
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.okBtn) {
			if (checkField()) {
				makeTweet();
				getDialog().dismiss();
			}
		} else if (view.getId() == R.id.cancelBtn) {
			getDialog().dismiss();
		}
	}

	@Override
	public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
		if (actionId == EditorInfo.IME_ACTION_SEND) {
			makeTweet();
			getDialog().dismiss();
			return true;
		}
		return false;
	}

	private void makeTweet() {
		TwitterAgent twitterAgent = new TwitterAgent(getActivity());
		twitterAgent.sendTweet(postEdt.getText().toString());
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (hasFocus) {
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		} else
			hideKeyBoard();
	}

	void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(postEdt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		if (postEdt.getText().toString().length() > 140) {
			charsLeft.setText(StaticData.SYMBOL_EMPTY + (140 - postEdt.getText().toString().length()));
			charsLeft.setTextColor(getResources().getColor(R.color.red));
		} else {
			charsLeft.setText(StaticData.SYMBOL_EMPTY + (postEdt.getText().toString().length()));
			charsLeft.setTextColor(getResources().getColor(R.color.dark_grey));
		}
	}

	@Override
	public void afterTextChanged(Editable editable) {
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (view.getId() == R.id.postEdt) {
			postEdt.setError(null);
		}
		return false;
	}
}
