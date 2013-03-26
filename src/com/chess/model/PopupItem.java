package com.chess.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.backend.statics.StaticData;

/**
 * PopupItem class
 *
 * @author alien_roger
 * @created at: 07.04.12 7:14
 */
public class PopupItem implements Parcelable {

	private int titleId;
    private int messageId;
    private String title;
    private String message;
    private int positiveBtnId;
    private int neutralBtnId;
    private int negativeBtnId;
    private LinearLayout customView;
	private int buttons;

	public PopupItem() {
        this.positiveBtnId = R.string.ok;
        this.negativeBtnId = R.string.cancel;
        title = StaticData.SYMBOL_EMPTY;
        message = StaticData.SYMBOL_EMPTY;
    }

    public int getPositiveBtnId() {
        return positiveBtnId;
    }

    public void setPositiveBtnId(int leftBtnId) {
        this.positiveBtnId = leftBtnId;
    }

	public void setNeutralBtnId(int neutralBtnId) {
        this.neutralBtnId = neutralBtnId;
    }

	public int getNeutralBtnId() {
		return neutralBtnId;
	}

    public String getMessage(Context context) {
        if(message.equals(StaticData.SYMBOL_EMPTY) && messageId != 0){
            return context.getString(messageId);
        }else{
            return message;
        }
    }

    public void setMessage(String message) {
        this.message = message;
		messageId = 0;
    }

    public int getNegativeBtnId() {
        return negativeBtnId;
    }

    public void setNegativeBtnId(int rightBtnId) {
        this.negativeBtnId = rightBtnId;
    }

    public String getTitle(Context context) {
        if(title.equals(StaticData.SYMBOL_EMPTY) && titleId != 0){
            return context.getString(titleId);
        }else{
            return title;
        }
    }

    public void setTitle(String title) {
        this.title = title;
		titleId = 0;
    }

    public void setTitle(int titleId) {
        this.titleId = titleId;
		title = StaticData.SYMBOL_EMPTY;
    }

    public void setMessage(int messageId) {
        this.messageId = messageId;
		message = StaticData.SYMBOL_EMPTY;
    }

    public View getCustomView() {
        return customView;
    }

    public void setCustomView(LinearLayout customView) {
        this.customView = customView;
    }

	public int getButtons() {
		return buttons;
	}

	public void setButtons(int buttons) {
		this.buttons = buttons;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeInt(titleId);
		parcel.writeInt(messageId);
		parcel.writeString(title);
		parcel.writeString(message);
		parcel.writeInt(positiveBtnId);
		parcel.writeInt(neutralBtnId);
		parcel.writeInt(negativeBtnId);
		parcel.writeInt(buttons);
	}

	public static final Parcelable.Creator<PopupItem> CREATOR = new Parcelable.Creator<PopupItem>() {
		@Override
		public PopupItem createFromParcel(Parcel in) {
			return new PopupItem(in);
		}

		@Override
		public PopupItem[] newArray(int size) {
			return new PopupItem[size];
		}
	};

	private PopupItem(Parcel in) {
		titleId = in.readInt();
		messageId = in.readInt();
		title = in.readString();
		message = in.readString();
		positiveBtnId = in.readInt();
		neutralBtnId = in.readInt();
		negativeBtnId = in.readInt();
		buttons = in.readInt();
	}
}
