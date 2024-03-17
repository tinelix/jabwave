package dev.tinelix.jabwave;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import dev.tinelix.jabwave.ui.list.items.ThemePreset;

public class Global {

    public static float getScaledDp(Resources res) {
        return res.getDisplayMetrics().scaledDensity;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String generateSHA256Hash(String text) {
        MessageDigest digest;
        String hash = "";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(text.getBytes());
            hash = bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return hash;
    }

    public static String generateMD5Hash(final String text) {
        MessageDigest digest;
        String hash = "";
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(text.getBytes());
            byte[] messageDigest = digest.digest();
            hash = bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    public static void triggerReceiverIntent(Context ctx, int message) {
        if(ctx.getApplicationContext() instanceof JabwaveApp) {
            JabwaveApp app = ((JabwaveApp) ctx.getApplicationContext());
            Intent intent = new Intent();
            if(app.getCurrentNetworkType().equals("telegram")) {
                intent.setAction("dev.tinelix.jabwave.TELEGRAM_RECEIVE");
            } else {
                intent.setAction("dev.tinelix.jabwave.XMPP_RECEIVE");
            }
            intent.putExtra("msg", message);
            ctx.sendBroadcast(intent);
        }
    }

    public static void showLinkOpeningDialog(Context ctx, String url) {
        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(ctx.getResources().getString(R.string.open_link_title))
                .setMessage(ctx.getResources().getString(R.string.open_link_summary, url))
                .setNegativeButton(ctx.getResources().getString(android.R.string.cancel), null)
                .setPositiveButton(
                        ctx.getResources().getString(android.R.string.cancel),
                        (dialog1, which) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            ctx.startActivity(intent);
                        }
                ).create();
        dialog.show();
    }

    public static int getColorAttribute(Context ctx, @AttrRes int attr_res) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = ctx.getTheme();
        theme.resolveAttribute(attr_res, typedValue, true);
        return typedValue.data;
    }

    public static int getEndNumberFromLong(long counter) {
        String str = String.format("%s", counter);
        return Integer.parseInt(str.substring(str.length() - 1));
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateTime(String pattern, Date date) {
        return new SimpleDateFormat(pattern).format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateTime(String pattern, long sec) {
        return new SimpleDateFormat(pattern).format(new Date(sec * 1000));
    }
}