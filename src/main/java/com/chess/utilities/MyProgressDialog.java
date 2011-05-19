package com.chess.utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;

public class MyProgressDialog
{
  private ProgressDialog progressDialog;

  public MyProgressDialog(ProgressDialog progressDialog)
  {
    this.progressDialog = progressDialog;
  }

  public MyProgressDialog(Context context)
  {
    progressDialog = new ProgressDialog(context);
  }

  public void dismiss()
  {
    try
    {
      progressDialog.dismiss();
    }
    catch(Exception e)
    {
      // hack for android's java.lang.IllegalArgumentException: View not attached to window manager
    }
  }

  public void setMessage(CharSequence message) {
	  progressDialog.setMessage(message);
  }

  public void requestWindowFeature(int featureId) {
    progressDialog.requestWindowFeature(featureId);
  }

  public void setOnCancelListener(OnCancelListener listener) {
	  progressDialog.setOnCancelListener(listener);
  }

  public void setCancelable(boolean flag) {
	  progressDialog.setCancelable(flag);
  }

  public void setIndeterminate(boolean indeterminate) {
	  progressDialog.setIndeterminate(indeterminate);
  }

  public void show() {
   progressDialog.show();
  }
}
