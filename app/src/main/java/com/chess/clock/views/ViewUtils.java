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

    /**
     * call before dialog `show()`
     */
    public static void setUpConfirmationPopup(Dialog dialog, Resources resources) {
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_popup);
        dialog.setOnShowListener(dialogInterface -> {
            TextView messageTv = dialog.findViewById(android.R.id.message);
            if (messageTv != null) {
                messageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_dialog_title));
            }
        });
    }
}
