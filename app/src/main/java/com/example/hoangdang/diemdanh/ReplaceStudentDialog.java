package com.example.hoangdang.diemdanh;

import android.app.AlertDialog;
import android.content.Context;

public class ReplaceStudentDialog extends AlertDialog {

    protected ReplaceStudentDialog(Context context) {
        super(context);
    }

    protected ReplaceStudentDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected ReplaceStudentDialog(Context context, int themeResId) {
        super(context, themeResId);
    }


}
