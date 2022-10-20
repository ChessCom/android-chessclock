package com.chess.clock.views;

import android.app.Dialog;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.chess.clock.R;

public class ViewUtils {
    public static void showView(View v, Boolean show) {
        if (show) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    public static void isInvisible(View v, boolean invisible) {
        if (invisible) {
            v.setVisibility(View.INVISIBLE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * call before dialog `show()`
     */
    public static void setLargePopupMessageTextSize(Dialog dialog, Resources resources) {
        dialog.setOnShowListener(dialogInterface -> {
            TextView messageTv = dialog.findViewById(android.R.id.message);
            if (messageTv != null) {
                messageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_dialog_title));
            }
        });
    }
}
