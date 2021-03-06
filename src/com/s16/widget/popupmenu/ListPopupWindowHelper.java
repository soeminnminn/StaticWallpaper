package com.s16.widget.popupmenu;

import android.content.Context;
import android.os.Build;

/**
 * User: mcxiaoke
 * Date: 13-10-3
 * Time: ä¸‹å�ˆ2:08
 */
class ListPopupWindowHelper {

    public static ListPopupWindow newListPopupWindow(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new ListPopupWindowCompat(context);
        } else {
            return new ListPopupWindowNative(context);
        }
    }
}
