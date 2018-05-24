package com.example.hoangdang.diemdanh.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by trungtrucnguyen on 3/8/18.
 */

public class DialogUtils {
    public static void showMessageDialog(Context context, String tittle, String message, DialogInterface.OnClickListener clickOk, DialogInterface.OnClickListener clickCancel){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        if(tittle!=null)dialog.setTitle(tittle);
        if(message!=null)dialog.setMessage(message);
        if(clickOk!=null)dialog.setPositiveButton(android.R.string.ok, clickOk);
        if(clickCancel!=null)dialog.setNegativeButton(android.R.string.cancel, clickCancel);

        dialog.create().show();
    }
}
