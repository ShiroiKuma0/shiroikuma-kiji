package ru.yanus171.feedexfork.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class WaitDialog extends AsyncTask<Void, Void, Void> {
    private ProgressDialog mDialog;
    private String mMessage;
    private Runnable mRun;
    public WaitDialog(Activity activity, int messageID, Runnable run) {
        mDialog = new ProgressDialog(activity);
        mMessage = activity.getString( messageID );
        mRun = run;
    }

    @Override
    protected void onPreExecute() {
        // The message view is not android.R.id.message on every ROM (EMUI keeps its own), so
        // TintDialog cannot reach it; colour the text itself instead.
        mDialog.setMessage(Theme.ChromeText(mMessage));
        mDialog.show();
        Theme.TintDialog( mDialog ); // black/yellow chrome, like every other dialog
    }

    protected Void doInBackground(Void... args) {
        try {
            mRun.run();
        } catch ( Exception e ) {
            e.printStackTrace();
            DebugApp.SendException( e, mDialog.getContext() );
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        // do UI work here
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
