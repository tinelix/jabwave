package dev.tinelix.jabwave.xmpp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import dev.tinelix.jabwave.core.ui.activities.AppActivity;

public class JabwaveReceiver extends BroadcastReceiver {

    static final String TAG = "JabwaveReceiver";

    private static JabwaveReceiver receiver;
    private static int networkType = -1;
    private Context ctx;

    public JabwaveReceiver(Context ctx) {
        this.ctx = ctx;
        receiver = this;
    }

    public JabwaveReceiver() {
        receiver = this;
    }

    public static JabwaveReceiver getInstance() {
        if(receiver == null) {
            receiver = new JabwaveReceiver(null);
        }
        return receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent);
        if(ctx instanceof AppActivity) {
            AppActivity activity = (AppActivity) ctx;
            activity.receiveState(
                    intent.getIntExtra("msg", 0),
                    intent.getBundleExtra("data")
            );
        }
    }

    // From yaxim XMPP client (GPLv3)

    public void startService(Context ctx, Intent i) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(i);
        else
            ctx.startService(i);
    }

}