package com.chess.ui.fragments.popup_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.model.PopupItem;


/**
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupProgressFragment extends BasePopupDialogFragment {

//    private TextView titleTxt;
//    private TextView messageTxt;
    private boolean cancelable = true;

	public static PopupProgressFragment newInstance(PopupItem popupItem) {
		PopupProgressFragment frag = new PopupProgressFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NORMAL,  R.style.NoTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.popup_progress, container, false);

//		titleTxt = (TextView) view.findViewById(R.id.popupTitle);
//		messageTxt = (TextView) view.findViewById(R.id.popupMessage);
		return view;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!cancelable)
            setCancelable(false);
    }

	@Override
	public void onResume() {
		super.onResume();
//		String message = popupItem.getMessage(getActivity());
//		if(message.equals(StaticData.SYMBOL_EMPTY)){
//			messageTxt.setVisibility(View.GONE);
//		}else{
//			if(message.contains(StaticData.SYMBOL_TAG)){
//				messageTxt.setText(Html.fromHtml(message));
//			}else{
//				messageTxt.setText(message);
//			}
//			messageTxt.setVisibility(View.VISIBLE);
//		}
//		titleTxt.setText(popupItem.getTitle(getActivity()));
	}

	public void setNotCancelable(){
        cancelable = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelable = true;
    }



}
