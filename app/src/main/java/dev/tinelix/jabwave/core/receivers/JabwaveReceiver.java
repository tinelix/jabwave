package dev.tinelix.jabwave.core.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import dev.tinelix.jabwave.core.activities.AppActivity;
import dev.tinelix.jabwave.core.activities.AuthActivity;
import dev.tinelix.jabwave.core.activities.MessengerActivity;

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
        Log.d(
                TAG,
                String.format("Received message code %s from %s.",
                    intent.getIntExtra("msg", 0),
                    intent.getAction().contains("TELEGRAM") ? "Telegram" : "XMPP instance"
                )
        );
        if(ctx instanceof AppActivity) {
            AppActivity activity = (AppActivity) ctx;
            activity.receiveState(
                    intent.getIntExtra("msg", 0),
                    intent.getBundleExtra("data")
            );
        } else if(ctx instanceof AuthActivity) {
            AuthActivity activity = (AuthActivity) ctx;
            activity.receiveState(
                    intent.getIntExtra("msg", 0),
                    intent.getBundleExtra("data")
            );
        } else if(ctx instanceof MessengerActivity) {
            MessengerActivity activity = (MessengerActivity) ctx;
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